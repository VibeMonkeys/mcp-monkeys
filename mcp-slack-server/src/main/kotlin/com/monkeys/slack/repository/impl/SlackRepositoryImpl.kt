package com.monkeys.slack.repository.impl

import com.monkeys.shared.dto.*
import com.monkeys.slack.repository.SlackRepository
import com.slack.api.Slack
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import org.slf4j.LoggerFactory
import kotlinx.coroutines.delay

/**
 * 실제 Slack 채널에서 메시지를 읽어오는 Repository
 * 채널의 모든 메시지와 스레드를 읽어서 Q&A 검색
 */
@Repository
class SlackRepositoryImpl(
    @Value("\${slack.bot-token}") private val botToken: String
) : SlackRepository {
    
    private val logger = LoggerFactory.getLogger(SlackRepositoryImpl::class.java)
    private val slack = Slack.getInstance()
    
    // 채널 메시지 캐시 (성능 향상용)
    private val channelMessagesCache = mutableMapOf<String, List<SlackQAEntry>>()
    private val cacheExpiry = mutableMapOf<String, Long>()
    private val cacheTimeout = 2 * 60 * 1000L // 2분 캐시
    
    override suspend fun getChannelQAHistory(channel: String, limit: Int): List<SlackQAEntry> {
        logger.info("실제 Slack 채널 메시지 읽기: channel=$channel")
        return getChannelMessages(channel, limit)
    }
    
    override suspend fun searchSimilarQuestions(question: String, channel: String, threshold: Double): List<SlackQAMatch> {
        logger.info("실제 채널에서 유사 질문 검색: question='$question', channel=$channel")
        
        try {
            // 채널의 모든 메시지 가져오기 (스레드 포함)
            val messages = getChannelMessages(channel, 1000) // 최대 1000개 메시지
            
            logger.info("채널 메시지 ${messages.size}개에서 검색 중...")
            
            // 유사도 계산
            val matches = messages.mapNotNull { message ->
                val similarity = calculateSimilarity(question, message.question)
                if (similarity >= threshold) {
                    SlackQAMatch(message, similarity)
                } else null
            }.sortedByDescending { it.similarity }
            
            logger.info("유사한 질문 ${matches.size}개 발견")
            
            return matches
            
        } catch (e: Exception) {
            logger.error("채널 검색 실패: channel=$channel", e)
            return emptyList()
        }
    }
    
    override suspend fun addQAEntry(entry: SlackQAEntry): Boolean {
        // 캐시 무효화
        invalidateChannelCache(entry.channel)
        return true
    }
    
    override suspend fun getChannelStats(channel: String): SlackChannelStats {
        val messages = getChannelMessages(channel, 1000)
        
        return SlackChannelStats(
            channel = channel,
            totalQuestions = messages.size,
            uniqueAuthors = messages.map { it.author }.distinct().size,
            oldestQuestion = messages.minByOrNull { it.timestamp }?.timestamp ?: 0,
            newestQuestion = messages.maxByOrNull { it.timestamp }?.timestamp ?: 0
        )
    }
    
    /**
     * 채널 메시지 가져오기 (스레드 포함)
     */
    private suspend fun getChannelMessages(channelName: String, limit: Int): List<SlackQAEntry> {
        // 캐시 확인
        val cacheKey = "$channelName:$limit"
        val cached = getCachedMessages(cacheKey)
        if (cached != null) {
            logger.info("캐시에서 메시지 반환: ${cached.size}개")
            return cached
        }
        
        try {
            // 채널 ID 찾기
            val channelId = findChannelId(channelName)
            if (channelId == null) {
                logger.warn("채널을 찾을 수 없음: $channelName")
                return emptyList()
            }
            
            logger.info("채널 메시지 가져오는 중... channelId=$channelId")
            
            val allMessages = mutableListOf<SlackQAEntry>()
            var cursor: String? = null
            var fetched = 0
            
            // 페이지네이션으로 모든 메시지 가져오기
            while (fetched < limit) {
                val response = slack.methods(botToken).conversationsHistory { req ->
                    req.channel(channelId)
                        .limit(200) // 한 번에 200개씩
                        .cursor(cursor)
                }
                
                if (!response.isOk) {
                    logger.error("메시지 가져오기 실패: ${response.error}")
                    break
                }
                
                val messages = response.messages ?: break
                logger.info("메시지 ${messages.size}개 가져옴")
                
                // 메시지를 Q&A 형태로 변환
                for (message in messages) {
                    if (fetched >= limit) break
                    
                    val messageText = message.text ?: continue
                    val user = message.user ?: continue
                    val ts = message.ts ?: continue
                    
                    // 봇 메시지는 제외
                    if (message.botId != null) continue
                    
                    // 스레드 답변들도 가져오기
                    val threadMessages = getThreadMessages(channelId, ts)
                    
                    // 메인 메시지
                    if (messageText.length > 5) { // 너무 짧은 메시지 제외
                        val bestAnswer = findBestAnswer(threadMessages, messageText)
                        allMessages.add(
                            SlackQAEntry(
                                id = ts,
                                question = messageText,
                                answer = bestAnswer,
                                channel = channelName,
                                author = user,
                                timestamp = parseTimestamp(ts),
                                threadId = null
                            )
                        )
                        fetched++
                    }
                    
                    // 스레드 메시지들도 추가
                    for (threadMsg in threadMessages) {
                        if (fetched >= limit) break
                        if (threadMsg.text?.length ?: 0 > 10) {
                            allMessages.add(
                                SlackQAEntry(
                                    id = threadMsg.ts ?: "",
                                    question = threadMsg.text ?: "",
                                    answer = "",
                                    channel = channelName,
                                    author = threadMsg.user ?: "",
                                    timestamp = parseTimestamp(threadMsg.ts),
                                    threadId = ts
                                )
                            )
                            fetched++
                        }
                    }
                }
                
                cursor = response.responseMetadata?.nextCursor
                if (cursor.isNullOrEmpty()) break
                
                delay(100) // API 호출 제한 방지
            }
            
            logger.info("총 ${allMessages.size}개 메시지 수집 완료")
            
            // 캐시에 저장
            setCachedMessages(cacheKey, allMessages)
            
            return allMessages
            
        } catch (e: Exception) {
            logger.error("채널 메시지 가져오기 실패", e)
            return emptyList()
        }
    }
    
    /**
     * 스레드 메시지들 가져오기
     */
    private suspend fun getThreadMessages(channelId: String, threadTs: String): List<com.slack.api.model.Message> {
        return try {
            val response = slack.methods(botToken).conversationsReplies { req ->
                req.channel(channelId).ts(threadTs)
            }
            
            if (response.isOk) {
                response.messages?.drop(1) ?: emptyList() // 첫 번째는 원본 메시지라 제외
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            logger.debug("스레드 메시지 가져오기 실패: $threadTs", e)
            emptyList()
        }
    }
    
    /**
     * 가장 적절한 답변 찾기 (스레드에서)
     */
    private fun findBestAnswer(threadMessages: List<com.slack.api.model.Message>, question: String): String {
        if (threadMessages.isEmpty()) {
            logger.debug("스레드 메시지가 없음")
            return ""
        }
        
        logger.debug("스레드에서 답변 찾기: ${threadMessages.size}개 메시지")
        
        // 모든 답변 후보 찾기 (조건 완화)
        val answerCandidates = threadMessages
            .filter { it.botId == null } // 봇 메시지 제외
            .mapNotNull { it.text }
            .filter { it.trim().isNotEmpty() }
            .filter { it.length >= 3 } // 최소 3글자 이상
            
        logger.debug("답변 후보 ${answerCandidates.size}개 발견")
        
        if (answerCandidates.isNotEmpty()) {
            // 질문이 아닌 답변 우선 선택
            val nonQuestionAnswers = answerCandidates.filter { !isQuestion(it) }
            
            if (nonQuestionAnswers.isNotEmpty()) {
                val bestAnswer = nonQuestionAnswers.maxByOrNull { it.length } ?: nonQuestionAnswers.first()
                logger.debug("최종 답변 선택: \"$bestAnswer\"")
                return bestAnswer
            }
            
            // 질문이 아닌 답변이 없으면 가장 긴 답변 선택
            val fallbackAnswer = answerCandidates.maxByOrNull { it.length } ?: answerCandidates.first()
            logger.debug("대체 답변 선택: \"$fallbackAnswer\"")
            return fallbackAnswer
        }
        
        logger.debug("적절한 답변을 찾지 못함")
        return ""
    }
    
    /**
     * 질문인지 판단 (Repository용)
     */
    private fun isQuestion(text: String): Boolean {
        val questionKeywords = listOf("?", "？", "무엇", "뭐", "어떻게", "왜", "언제", "어디서", "누가", "방법", "어떤")
        val lowerText = text.lowercase()
        return questionKeywords.any { lowerText.contains(it) }
    }
    
    /**
     * 채널 ID 찾기
     */
    private suspend fun findChannelId(channelName: String): String? {
        return try {
            val response = slack.methods(botToken).conversationsList { req ->
                req.limit(1000)
            }
            
            if (response.isOk) {
                response.channels?.find { it.name == channelName }?.id
            } else {
                logger.error("채널 목록 가져오기 실패: ${response.error}")
                null
            }
        } catch (e: Exception) {
            logger.error("채널 ID 찾기 실패", e)
            null
        }
    }
    
    /**
     * 타임스탬프 파싱
     */
    private fun parseTimestamp(ts: String?): Long {
        return try {
            ts?.toDouble()?.toLong()?.times(1000) ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    /**
     * 유사도 계산 (키워드 기반 관대한 버전)
     */
    private fun calculateSimilarity(question1: String, question2: String): Double {
        val words1 = tokenize(question1)
        val words2 = tokenize(question2)
        
        if (words1.isEmpty() || words2.isEmpty()) return 0.0
        
        // 핵심 키워드 추출 (2글자 이상)
        val keywords1 = words1.filter { it.length >= 2 }
        val keywords2 = words2.filter { it.length >= 2 }
        
        if (keywords1.isEmpty() || keywords2.isEmpty()) return 0.0
        
        // 키워드 매칭 점수 계산
        val exactMatches = keywords1.intersect(keywords2.toSet()).size
        val partialMatches = keywords1.count { k1 ->
            keywords2.any { k2 -> 
                k1.contains(k2) || k2.contains(k1) || 
                // 유사한 키워드 판정 (편집거리 기반)
                calculateEditDistance(k1, k2) <= 1
            }
        }
        
        // 가중치 점수: 정확 매칭 x2 + 부분 매칭 x1
        val totalScore = (exactMatches * 2.0) + (partialMatches * 1.0)
        val maxPossibleScore = maxOf(keywords1.size, keywords2.size) * 2.0
        
        return (totalScore / maxPossibleScore).coerceAtMost(1.0)
    }
    
    /**
     * 편집 거리 계산 (유사한 단어 찾기용)
     */
    private fun calculateEditDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i-1] == s2[j-1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i-1][j] + 1,      // 삭제
                    dp[i][j-1] + 1,      // 삽입
                    dp[i-1][j-1] + cost  // 치환
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    /**
     * 텍스트 토큰화 (개선된 버전)
     */
    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^가-힣a-z0-9\\s]"), " ") // 특수문자 제거
            .split(Regex("\\s+"))
            .filter { it.length > 1 } // 한 글자 단어 제외
    }
    
    /**
     * 캐시 관련 메서드들
     */
    private fun getCachedMessages(key: String): List<SlackQAEntry>? {
        val expiry = cacheExpiry[key] ?: return null
        if (System.currentTimeMillis() > expiry) {
            channelMessagesCache.remove(key)
            cacheExpiry.remove(key)
            return null
        }
        return channelMessagesCache[key]
    }
    
    private fun setCachedMessages(key: String, messages: List<SlackQAEntry>) {
        channelMessagesCache[key] = messages
        cacheExpiry[key] = System.currentTimeMillis() + cacheTimeout
    }
    
    private fun invalidateChannelCache(channel: String) {
        val keysToRemove = channelMessagesCache.keys.filter { it.startsWith(channel) }
        keysToRemove.forEach { key ->
            channelMessagesCache.remove(key)
            cacheExpiry.remove(key)
        }
    }
}