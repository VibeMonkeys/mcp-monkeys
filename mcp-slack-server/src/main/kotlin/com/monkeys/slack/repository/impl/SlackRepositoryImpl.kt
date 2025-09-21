package com.monkeys.slack.repository.impl

import com.monkeys.shared.dto.*
import com.monkeys.slack.repository.SlackRepository
import com.monkeys.slack.client.IntentAnalyzerClient
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
    @Value("\${slack.bot-token}") private val botToken: String,
    @Value("\${intent-analyzer.enabled:true}") private val intentAnalyzerEnabled: Boolean,
    @Value("\${intent-analyzer.fallback-threshold:0.5}") private val fallbackThreshold: Double,
    private val intentAnalyzerClient: IntentAnalyzerClient
) : SlackRepository {
    
    private val logger = LoggerFactory.getLogger(SlackRepositoryImpl::class.java)
    private val slack = Slack.getInstance()
    
    // 채널 메시지 캐시 (성능 향상용)
    private val channelMessagesCache = mutableMapOf<String, List<SlackQAEntry>>()
    private val cacheExpiry = mutableMapOf<String, Long>()
    private val cacheTimeout = 2 * 60 * 1000L // 2분 캐시
    
    // 봇 User ID 캐시 (초기화 시 한 번만 조회)
    private var botUserId: String? = null
    
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
            
            // 유사도 계산 (suspend 함수 호출)
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
                    
                    // 봇 메시지는 제외 (botId 또는 봇 User ID로 확인)
                    if (botUserId == null) {
                        botUserId = getBotUserId() // 초기 로드
                    }
                    if (message.botId != null || user == botUserId) continue
                    
                    // 스레드 답변들도 가져오기
                    val threadMessages = getThreadMessages(channelId, ts)
                    
                    // 메인 메시지
                    if (messageText.length > 5) { // 너무 짧은 메시지 제외
                        val bestAnswer = findBestAnswer(threadMessages)
                        logger.debug("메인 메시지 처리: question='$messageText', answer='$bestAnswer'")
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
                    
                    // 스레드 메시지는 별도 검색 대상에서 제외
                    // (이미 메인 메시지의 답변으로 활용됨)
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
    private fun findBestAnswer(threadMessages: List<com.slack.api.model.Message>): String {
        if (threadMessages.isEmpty()) {
            logger.debug("스레드 메시지가 없음")
            return ""
        }
        
        logger.debug("스레드에서 답변 찾기: ${threadMessages.size}개 메시지")
        
        // 모든 답변 후보 찾기 (사람의 답변만)
        val botId = botUserId // 캐시된 봇 ID 사용
        val answerCandidates = threadMessages
            .filter { it.botId == null && it.user != botId } // 봇 메시지 완전 제외
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
     * 유사도 계산 (Intent Analyzer + 키워드 기반 폴백)
     */
    private suspend fun calculateSimilarity(question1: String, question2: String): Double {
        // Intent Analyzer 사용 (활성화된 경우)
        if (intentAnalyzerEnabled) {
            return calculateSimilarityWithIntentAnalyzer(question1, question2)
        }
        
        // 기존 키워드 기반 계산
        return calculateSimilarityKeywordBased(question1, question2)
    }
    
    /**
     * Intent Analyzer를 사용한 유사도 계산
     */
    private suspend fun calculateSimilarityWithIntentAnalyzer(question1: String, question2: String): Double {
        return try {
            logger.debug("Intent Analyzer로 유사도 계산: '$question1' vs '$question2'")
            
            // 두 질문의 의도 분석
            val intent1 = intentAnalyzerClient.analyzeIntent(question1, "slack")
            val intent2 = intentAnalyzerClient.analyzeIntent(question2, "slack")
            
            if (intent1 == null || intent2 == null) {
                logger.warn("Intent 분석 실패, 키워드 기반 계산으로 폴백")
                return calculateSimilarityKeywordBased(question1, question2)
            }
            
            var similarity = 0.0
            
            // 1. Intent 타입 유사도 (25% 가중치) - 유사한 intent도 고려
            val intentSimilarity = calculateIntentSimilarity(intent1.intentType, intent2.intentType)
            similarity += intentSimilarity * 0.25
            
            // 2. 키워드 유사도 (60% 가중치) - 키워드 가중치 더 증가
            val keywordSimilarity = calculateKeywordSimilarity(intent1.keywords, intent2.keywords)
            similarity += keywordSimilarity * 0.6
            
            // 키워드 유사도가 너무 낮으면 매칭하지 않음 (핵심 키워드가 다른 경우)
            if (keywordSimilarity < 0.5) {
                logger.debug("키워드 유사도가 너무 낮음 ($keywordSimilarity), 매칭 제외")
                return 0.0
            }
            
            // 3. 우선순위 유사도 (10% 가중치)
            val prioritySimilarity = calculatePrioritySimilarity(intent1.priority, intent2.priority)
            similarity += prioritySimilarity * 0.1
            
            // 4. 감정 톤 유사도 (5% 가중치) - 톤은 덜 중요하게
            val toneSimilarity = if (intent1.emotionalTone == intent2.emotionalTone) 1.0 else 0.7
            similarity += toneSimilarity * 0.05
            
            logger.debug("Intent 기반 유사도: $similarity (intent=$intentSimilarity, keyword=$keywordSimilarity, priority=$prioritySimilarity, tone=$toneSimilarity)")
            
            // 낮은 신뢰도인 경우 키워드 기반 계산과 혼합
            val confidence = minOf(intent1.confidence, intent2.confidence)
            if (confidence < fallbackThreshold) {
                val keywordBased = calculateSimilarityKeywordBased(question1, question2)
                similarity = (similarity * confidence) + (keywordBased * (1 - confidence))
                logger.debug("낮은 신뢰도($confidence), 키워드 기반과 혼합: $similarity")
            }
            
            similarity.coerceIn(0.0, 1.0)
            
        } catch (e: Exception) {
            logger.warn("Intent Analyzer 유사도 계산 실패, 키워드 기반으로 폴백", e)
            calculateSimilarityKeywordBased(question1, question2)
        }
    }
    
    /**
     * 키워드 유사도 계산
     */
    private fun calculateKeywordSimilarity(keywords1: List<com.monkeys.slack.client.Keyword>, 
                                          keywords2: List<com.monkeys.slack.client.Keyword>): Double {
        if (keywords1.isEmpty() || keywords2.isEmpty()) return 0.0
        
        val texts1 = keywords1.map { it.text }.toSet()
        val texts2 = keywords2.map { it.text }.toSet()
        
        val intersection = texts1.intersect(texts2).size
        val union = texts1.union(texts2).size
        
        return if (union > 0) intersection.toDouble() / union else 0.0
    }
    
    /**
     * 우선순위 유사도 계산
     */
    private fun calculatePrioritySimilarity(priority1: com.monkeys.slack.client.Priority, 
                                           priority2: com.monkeys.slack.client.Priority): Double {
        val p1 = priority1.ordinal
        val p2 = priority2.ordinal
        val maxDiff = 4 // P0~P4
        
        return 1.0 - (kotlin.math.abs(p1 - p2).toDouble() / maxDiff)
    }
    
    /**
     * Intent 타입 유사도 계산 (유사한 Intent도 고려)
     */
    private fun calculateIntentSimilarity(intent1: String, intent2: String): Double {
        return when {
            intent1 == intent2 -> 1.0  // 완전 동일
            
            // 질문 관련 intent들은 서로 유사하게 처리
            (intent1.startsWith("question_") && intent2.startsWith("question_")) -> 0.9
            
            // 요청 관련 intent들도 유사하게 처리
            (intent1.startsWith("request_") && intent2.startsWith("request_")) -> 0.9
            
            // 정보 요청 관련 intent들도 유사하게 처리
            (intent1.startsWith("information_") && intent2.startsWith("information_")) -> 0.9
            
            // 도움 요청 관련 intent들도 유사하게 처리
            (intent1.startsWith("help_") && intent2.startsWith("help_")) -> 0.9
            
            // 완전히 다른 카테고리여도 일부 유사도 부여 (더 관대한 매칭)
            else -> 0.3
        }
    }
    
    /**
     * 기존 키워드 기반 유사도 계산
     */
    private fun calculateSimilarityKeywordBased(question1: String, question2: String): Double {
        val words1 = tokenize(question1)
        val words2 = tokenize(question2)
        
        if (words1.isEmpty() || words2.isEmpty()) return 0.0
        
        // 핵심 키워드 추출 (1글자 이상으로 완화)
        val keywords1 = words1.filter { it.length >= 1 }
        val keywords2 = words2.filter { it.length >= 1 }
        
        if (keywords1.isEmpty() || keywords2.isEmpty()) return 0.0
        
        // 키워드 매칭 점수 계산
        val exactMatches = keywords1.intersect(keywords2.toSet()).size
        val partialMatches = keywords1.count { k1 ->
            keywords2.any { k2 -> 
                k1.contains(k2) || k2.contains(k1) || 
                // 유사한 키워드 판정 (편집거리 기반 - 더 관대하게)
                calculateEditDistance(k1, k2) <= 2
            }
        }
        
        // 의미상 유사한 단어들 추가 매칭
        val semanticMatches = calculateSemanticSimilarity(keywords1, keywords2)
        
        // 가중치 점수: 정확 매칭 x3 + 부분 매칭 x2 + 의미 매칭 x1
        val totalScore = (exactMatches * 3.0) + (partialMatches * 2.0) + (semanticMatches * 1.0)
        val maxPossibleScore = maxOf(keywords1.size, keywords2.size) * 3.0
        
        return (totalScore / maxPossibleScore).coerceAtMost(1.0)
    }
    
    /**
     * 의미상 유사한 단어들 매칭
     */
    private fun calculateSemanticSimilarity(keywords1: List<String>, keywords2: List<String>): Int {
        // 한국어 업무 관련 동의어 매핑
        val synonymGroups = mapOf(
            setOf("휴가", "연차", "연가", "휴무") to "vacation",
            setOf("출근", "근무", "업무", "일") to "work",
            setOf("퇴근", "퇴사", "끝", "마무리") to "finish",
            setOf("급여", "월급", "임금", "돈", "페이") to "salary",
            setOf("회의", "미팅", "모임", "만남") to "meeting",
            setOf("프로젝트", "과제", "업무", "작업") to "project",
            setOf("문의", "질문", "궁금", "물어") to "question",
            setOf("신청", "요청", "부탁", "요구") to "request",
            setOf("승인", "허가", "확인", "검토") to "approval",
            setOf("시간", "일정", "스케줄", "날짜") to "schedule"
        )
        
        val group1 = keywords1.mapNotNull { word ->
            synonymGroups.entries.find { (synonyms, _) -> 
                synonyms.any { synonym -> word.contains(synonym) || synonym.contains(word) }
            }?.value
        }.toSet()
        
        val group2 = keywords2.mapNotNull { word ->
            synonymGroups.entries.find { (synonyms, _) -> 
                synonyms.any { synonym -> word.contains(synonym) || synonym.contains(word) }
            }?.value
        }.toSet()
        
        return group1.intersect(group2).size
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
    
    override fun invalidateCache(channel: String) {
        invalidateChannelCache(channel)
    }
    
    private fun invalidateChannelCache(channel: String) {
        val keysToRemove = channelMessagesCache.keys.filter { it.startsWith(channel) }
        keysToRemove.forEach { key ->
            channelMessagesCache.remove(key)
            cacheExpiry.remove(key)
        }
    }
    
    /**
     * 봇의 User ID 조회 (캐시됨)
     */
    private suspend fun getBotUserId(): String? {
        if (botUserId != null) return botUserId
        
        return try {
            val response = slack.methods(botToken).authTest { req -> req }
            if (response.isOk) {
                botUserId = response.userId
                logger.debug("봇 User ID 획득: $botUserId")
                botUserId
            } else {
                logger.warn("봇 User ID 조회 실패: ${response.error}")
                null
            }
        } catch (e: Exception) {
            logger.warn("봇 User ID 조회 중 오류", e)
            null
        }
    }
    
    override suspend fun analyzeIntentForAnswer(prompt: String): com.monkeys.slack.client.IntentAnalysisResult? {
        return try {
            if (intentAnalyzerEnabled) {
                logger.debug("Intent Analyzer로 답변 선택 분석: '$prompt'")
                intentAnalyzerClient.analyzeIntent(prompt, "slack")
            } else {
                logger.debug("Intent Analyzer 비활성화됨")
                null
            }
        } catch (e: Exception) {
            logger.warn("Intent 분석 실패", e)
            null
        }
    }
    
    override suspend fun reformatAnswerWithGemini(prompt: String): String {
        return try {
            logger.debug("Gemini API로 답변 재가공: '$prompt'")
            
            // Intent Analyzer 클라이언트를 통해 Gemini API 호출
            // 하지만 결과는 의도분석이 아닌 텍스트 생성으로 사용
            val result = intentAnalyzerClient.analyzeIntent(prompt, "answer_reformat")
            
            // 키워드에서 재작성된 답변 추출
            val reformattedText = result?.keywords
                ?.mapNotNull { keyword ->
                    val text = keyword.text.trim()
                    // 재작성된 답변 패턴 찾기
                    when {
                        text.contains("재작성된 답변:") -> {
                            text.substringAfter("재작성된 답변:").trim()
                        }
                        text.contains("답변:") -> {
                            text.substringAfter("답변:").trim()
                        }
                        text.length > 10 && !text.contains("요구사항") && !text.contains("분석") -> {
                            text // 의미있는 긴 텍스트
                        }
                        else -> null
                    }
                }
                ?.filter { it.isNotEmpty() }
                ?.maxByOrNull { it.length } // 가장 긴 것 선택
            
            if (!reformattedText.isNullOrEmpty()) {
                logger.debug("Gemini 재가공 성공: '$reformattedText'")
                reformattedText
            } else {
                logger.debug("Gemini 재가공 결과 없음")
                ""
            }
            
        } catch (e: Exception) {
            logger.warn("Gemini API 답변 재가공 실패", e)
            ""
        }
    }
}