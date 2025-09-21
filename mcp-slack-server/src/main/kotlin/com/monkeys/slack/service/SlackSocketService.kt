package com.monkeys.slack.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.monkeys.shared.dto.*
import com.slack.api.Slack
import com.slack.api.methods.SlackApiException
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.io.IOException
import java.net.URI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.util.concurrent.atomic.AtomicLong

/**
 * 실제 Slack Socket Mode 연결 서비스
 * WebSocket을 통해 Slack과 실시간 연결
 */
@Service
@ConditionalOnProperty(name = ["slack.socket-mode.enabled"], havingValue = "true")
class SlackSocketService(
    private val slackService: SlackService,
    private val slackRepository: com.monkeys.slack.repository.SlackRepository,
    @Value("\${slack.bot-token}") private val botToken: String,
    @Value("\${slack.app-token}") private val appToken: String,
    @Value("\${qa-bot.matching.similarity-threshold:0.3}") private val similarityThreshold: Double
) {
    private val logger = LoggerFactory.getLogger(SlackSocketService::class.java)
    private val slack = Slack.getInstance()
    private val objectMapper = jacksonObjectMapper()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocketClient: WebSocketClient? = null
    private val envelopeId = AtomicLong(1)
    private val processedMessages = mutableSetOf<String>() // 처리된 메시지 추적
    private var botUserId: String? = null // 봇 User ID 캐시

    @PostConstruct
    fun initialize() {
        logger.info("=== Slack Socket Mode 실제 연결 시작 ===")
        
        scope.launch {
            try {
                connectToSlack()
            } catch (e: Exception) {
                logger.error("Slack Socket Mode 연결 실패", e)
            }
        }
    }

    @PreDestroy
    fun cleanup() {
        try {
            scope.cancel()
            webSocketClient?.close()
            logger.info("Slack Socket Mode 연결 종료")
        } catch (e: Exception) {
            logger.error("정리 중 오류", e)
        }
    }

    /**
     * Slack Socket Mode 연결
     */
    private suspend fun connectToSlack() {
        try {
            // Socket Mode 연결 URL 요청
            val response = slack.methods().appsConnectionsOpen { req ->
                req.token(appToken)
            }
            
            if (!response.isOk) {
                logger.error("Socket Mode 연결 URL 요청 실패: 응답 오류")
                return
            }
            
            val socketUrl = response.url
            logger.info("Socket Mode URL 획득: $socketUrl")
            
            // WebSocket 연결
            connectWebSocket(socketUrl)
            
        } catch (e: SlackApiException) {
            logger.error("Slack API 오류", e)
        } catch (e: IOException) {
            logger.error("네트워크 오류", e)
        }
    }

    /**
     * WebSocket 연결 및 이벤트 처리
     */
    private fun connectWebSocket(url: String) {
        webSocketClient = object : WebSocketClient(URI.create(url)) {
            
            override fun onOpen(handshake: ServerHandshake) {
                logger.info("✅ Slack Socket Mode 연결 성공!")
                logger.info("🤖 Q&A 봇이 활성화되었습니다. Slack에서 질문해보세요!")
            }
            
            override fun onMessage(message: String) {
                scope.launch {
                    try {
                        handleSlackMessage(message)
                    } catch (e: Exception) {
                        logger.error("메시지 처리 오류", e)
                    }
                }
            }
            
            override fun onClose(code: Int, reason: String, remote: Boolean) {
                logger.warn("Socket Mode 연결 종료: code=$code, reason=$reason")
                
                // 재연결 시도
                scope.launch {
                    delay(5000) // 5초 후 재연결
                    logger.info("Socket Mode 재연결 시도...")
                    connectToSlack()
                }
            }
            
            override fun onError(ex: Exception) {
                logger.error("Socket Mode 오류", ex)
            }
        }
        
        webSocketClient?.connect()
    }

    /**
     * Slack 메시지 처리
     */
    private suspend fun handleSlackMessage(message: String) {
        try {
            val json = objectMapper.readTree(message)
            val type = json.get("type")?.asText()
            
            when (type) {
                "hello" -> {
                    logger.info("Slack Socket Mode hello 메시지 수신")
                }
                
                "events_api" -> {
                    handleEventApiMessage(json)
                }
                
                else -> {
                    logger.debug("처리하지 않는 메시지 타입: $type")
                }
            }
            
        } catch (e: Exception) {
            logger.error("메시지 파싱 오류", e)
        }
    }

    /**
     * Events API 메시지 처리
     */
    private suspend fun handleEventApiMessage(json: JsonNode) {
        try {
            val envelopeId = json.get("envelope_id")?.asText()
            val event = json.get("payload")?.get("event")
            
            if (event != null) {
                val eventType = event.get("type")?.asText()
                
                when (eventType) {
                    "message" -> handleMessageEvent(event)
                    "app_mention" -> handleAppMentionEvent(event)
                }
            }
            
            // ACK 응답 전송
            if (envelopeId != null) {
                sendAcknowledgment(envelopeId)
            }
            
        } catch (e: Exception) {
            logger.error("Events API 메시지 처리 오류", e)
        }
    }

    /**
     * 메시지 이벤트 처리
     */
    private suspend fun handleMessageEvent(event: JsonNode) {
        try {
            val text = event.get("text")?.asText() ?: return
            val channel = event.get("channel")?.asText() ?: return
            val user = event.get("user")?.asText() ?: return
            val ts = event.get("ts")?.asText() ?: return
            
            // 봇 자신의 메시지는 무시 (botId 또는 봇 User ID로 확인)
            val botId = getBotUserId()
            if (event.get("bot_id") != null || user == botId) return
            
            val threadTs = event.get("thread_ts")?.asText()
            
            if (threadTs != null && threadTs != ts) {
                // 스레드 메시지 처리 - 답변 학습 목적
                logger.info("🧵 스레드 메시지 수신: '$text' (channel: $channel, thread: $threadTs)")
                handleThreadMessage(text, channel, threadTs)
                return
            }
            
            logger.info("📨 Slack 메시지 수신: '$text' (channel: $channel)")
            
            // 질문인지 확인
            if (isQuestion(text)) {
                // 질문 감지 즉시 눈 이모지로 인식 표시
                addReaction(channel, ts, "eyes")
                processQuestion(text, channel, ts)
            }
            
        } catch (e: Exception) {
            logger.error("메시지 이벤트 처리 오류", e)
        }
    }

    /**
     * 앱 멘션 이벤트 처리
     */
    private suspend fun handleAppMentionEvent(event: JsonNode) {
        try {
            val text = event.get("text")?.asText() ?: return
            val channel = event.get("channel")?.asText() ?: return
            val ts = event.get("ts")?.asText() ?: return
            
            logger.info("🔔 Slack 앱 멘션 수신: '$text' (channel: $channel)")
            
            // 멘션 부분 제거
            val cleanText = text.replace(Regex("<@[A-Z0-9]+>"), "").trim()
            
            if (cleanText.isNotEmpty()) {
                // 앱 멘션 감지 즉시 눈 이모지로 인식 표시
                addReaction(channel, ts, "eyes")
                processQuestion(cleanText, channel, ts)
            }
            
        } catch (e: Exception) {
            logger.error("앱 멘션 이벤트 처리 오류", e)
        }
    }

    /**
     * 스레드 메시지 처리 - 답변 학습
     */
    private suspend fun handleThreadMessage(text: String, channel: String, threadTs: String) {
        try {
            // 단순한 즉시 처리 대신, 스레드 전체를 분석하여 최적 답변 선택
            // 일정 시간 후 스레드 전체를 분석하도록 지연 처리
            logger.info("🧵 스레드 메시지 수신: '$text' - 5초 후 스레드 전체 분석 예정")
            
            // 5초 후 스레드 전체 분석 (대화가 어느 정도 진행된 후)
            scope.launch {
                delay(5000) // 5초 대기
                analyzeCompleteThread(channel, threadTs)
            }
            
        } catch (e: Exception) {
            logger.error("스레드 메시지 처리 오류", e)
        }
    }
    
    /**
     * 스레드 전체를 분석하여 최적의 답변 찾기
     */
    private suspend fun analyzeCompleteThread(channel: String, threadTs: String) {
        try {
            logger.info("🔍 스레드 전체 분석 시작: $threadTs")
            
            // 스레드 전체 메시지 가져오기
            val threadMessages = getCompleteThreadMessages(channel, threadTs)
            if (threadMessages.isEmpty()) return
            
            val originalQuestion = threadMessages.firstOrNull()?.text ?: ""
            if (originalQuestion.isEmpty()) return
            
            // 봇 메시지 제외하고 사람의 답변만 추출
            val botId = getBotUserId()
            val humanMessages = threadMessages.drop(1) // 첫 번째(질문) 제외
                .filter { it.botId == null && it.user != botId }
                .mapNotNull { it.text }
                .filter { it.trim().isNotEmpty() && it.length >= 10 }
            
            if (humanMessages.isEmpty()) {
                logger.debug("스레드에 의미 있는 답변이 없음")
                return
            }
            
            // 최적의 답변 선택
            val bestAnswer = selectBestAnswer(humanMessages)
            if (bestAnswer.isNotEmpty()) {
                // 답변 정제 및 LLM 기반 재가공
                val cleanedAnswer = cleanAnswer(bestAnswer)
                val formalAnswer = reformatAnswerWithLLM(originalQuestion, cleanedAnswer)
                logger.info("✅ 스마트 Q&A 학습: 질문='$originalQuestion'")
                logger.info("   원본 답변: '$bestAnswer'")
                logger.info("   정제 답변: '$cleanedAnswer'")
                logger.info("   형식화 답변: '$formalAnswer'")
                
                // 정제된 답변으로 Q&A 엔트리 저장
                val channelName = getChannelNameFromId(channel) ?: "unknown"
                val qaEntry = com.monkeys.shared.dto.SlackQAEntry(
                    id = threadTs,
                    question = originalQuestion,
                    answer = formalAnswer, // 형식화된 답변 사용
                    channel = channelName,
                    author = "learned",
                    timestamp = System.currentTimeMillis(),
                    threadId = threadTs
                )
                
                try {
                    slackRepository.addQAEntry(qaEntry)
                    logger.info("📚 Q&A 엔트리 저장 완료")
                } catch (e: Exception) {
                    logger.warn("Q&A 엔트리 저장 실패", e)
                }
                
                // 캐시 무효화
                slackRepository.invalidateCache(channelName)
                logger.info("🔄 채널 '$channelName' 캐시 무효화 완료")
            }
            
        } catch (e: Exception) {
            logger.error("스레드 분석 오류", e)
        }
    }
    
    /**
     * 스레드 전체 메시지 가져오기
     */
    private suspend fun getCompleteThreadMessages(channel: String, threadTs: String): List<com.slack.api.model.Message> {
        return try {
            val response = slack.methods(botToken).conversationsReplies { req ->
                req.channel(channel).ts(threadTs).limit(50)
            }
            
            if (response.isOk) {
                response.messages ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            logger.debug("스레드 메시지 가져오기 실패", e)
            emptyList()
        }
    }
    
    /**
     * 여러 답변 중 최적의 답변 선택 (AI 기반)
     */
    private suspend fun selectBestAnswer(candidateAnswers: List<String>): String {
        if (candidateAnswers.isEmpty()) return ""
        if (candidateAnswers.size == 1) return candidateAnswers.first()
        
        logger.info("🤔 ${candidateAnswers.size}개 답변 후보 중 AI로 최적 답변 선택 중...")
        
        // 1. 질문이 아닌 답변들만 필터링
        val nonQuestionAnswers = candidateAnswers.filter { !isQuestion(it) }
        
        if (nonQuestionAnswers.isEmpty()) {
            logger.debug("질문이 아닌 답변이 없음")
            return ""
        }
        
        if (nonQuestionAnswers.size == 1) {
            return nonQuestionAnswers.first()
        }
        
        // 2. 개선된 품질 기반 선택 (간단하지만 효과적)
        return try {
            logger.info("🔍 ${nonQuestionAnswers.size}개 답변을 품질 기준으로 분석 중...")
            
            // 각 답변에 대해 품질 점수 계산
            val scoredAnswers = nonQuestionAnswers.map { answer ->
                var score = 0.0
                
                // 1. 기본 길이 점수 (너무 짧거나 너무 길면 감점)
                score += when (answer.length) {
                    in 10..200 -> 1.0    // 적정 길이
                    in 5..9 -> 0.7       // 짧지만 의미있을 수 있음
                    in 201..500 -> 0.8   // 조금 길지만 상세할 수 있음
                    else -> 0.3          // 너무 짧거나 너무 김
                }
                
                // 2. 구체적 정보 포함 여부
                val infoIndicators = listOf(
                    "방법", "단계", "절차", "과정", "순서", "먼저", "다음", "그리고",
                    "클릭", "선택", "입력", "확인", "접속", "실행", "설정", "변경",
                    "메뉴", "버튼", "화면", "페이지", "링크", "주소", "경로",
                    "해결", "해답", "답변", "결과", "완료"
                )
                val infoCount = infoIndicators.count { answer.contains(it) }
                score += (infoCount * 0.3).coerceAtMost(1.5)
                
                // 3. 완결성 (문장 구조가 완전한지)
                if (answer.contains(".") || answer.contains("!") || answer.contains("다") || answer.contains("요")) {
                    score += 0.5
                }
                
                // 4. 단순한 인사말이나 감정표현은 감점
                val casualExpressions = listOf("ㅋㅋ", "ㅎㅎ", "네네", "아하", "오케이", "굿", "👍", "😊")
                if (casualExpressions.any { answer.contains(it) } && answer.length < 20) {
                    score *= 0.3
                }
                
                // 5. 질문형태면 감점 (답변이 아니라 추가 질문일 가능성)
                if (answer.contains("?") || answer.contains("？")) {
                    score *= 0.5
                }
                
                Pair(answer, score)
            }
            
            // 최고 점수 답변 선택
            val bestScoredAnswer = scoredAnswers.maxByOrNull { it.second }
            val bestAnswer = bestScoredAnswer?.first ?: ""
            val bestScore = bestScoredAnswer?.second ?: 0.0
            
            logger.info("🎯 품질 기반 최적 답변 선택 (점수: ${"%.2f".format(bestScore)}): '${bestAnswer.take(50)}...'")
            
            // 최소 품질 기준을 통과한 경우에만 반환
            if (bestScore >= 1.0) {
                bestAnswer
            } else {
                logger.info("⚠️ 모든 답변이 최소 품질 기준(1.0) 미달, 가장 긴 답변으로 폴백")
                nonQuestionAnswers.maxByOrNull { it.length } ?: ""
            }
            
        } catch (e: Exception) {
            logger.warn("답변 품질 분석 실패, 가장 긴 답변으로 폴백", e)
            nonQuestionAnswers.maxByOrNull { it.length } ?: ""
        }
    }
    
    /**
     * 답변 정제 - 불필요한 예의 표현 제거하되 핵심 내용은 유지
     */
    private fun cleanAnswer(rawAnswer: String): String {
        var cleaned = rawAnswer.trim()
        
        // 문장 끝의 불필요한 예의 표현 제거
        val unnecessaryEndings = listOf(
            "감사합니다!*".toRegex(),
            "감사해요!*".toRegex(), 
            "고맙습니다!*".toRegex(),
            "고마워요!*".toRegex(),
            "네네!*".toRegex(),
            "네네$".toRegex(),
            "ㅋㅋ+!*$".toRegex(),
            "ㅎㅎ+!*$".toRegex(),
            "👍+$".toRegex(),
            "😊+$".toRegex(),
            "굿!*$".toRegex(),
            "좋아요!*$".toRegex()
        )
        
        // 문장 끝에서 불필요한 표현들 제거
        for (pattern in unnecessaryEndings) {
            cleaned = cleaned.replace(pattern, "").trim()
        }
        
        // 중간에 있는 불필요한 이모지나 표현 제거
        cleaned = cleaned
            .replace("ㅋㅋ+".toRegex(), "") // 중간의 ㅋㅋ
            .replace("ㅎㅎ+".toRegex(), "") // 중간의 ㅎㅎ
            .replace("\\s+".toRegex(), " ") // 여러 공백을 하나로
            .trim()
        
        // 의미있는 마침표나 느낌표는 유지하되, 연속된 것은 하나로
        cleaned = cleaned
            .replace("!+".toRegex(), "!")
            .replace("\\.+".toRegex(), ".")
        
        // 마지막에 마침표가 없고 완전한 문장인 경우 마침표 추가
        if (cleaned.isNotEmpty() && 
            !cleaned.endsWith(".") && 
            !cleaned.endsWith("!") && 
            !cleaned.endsWith("?") &&
            (cleaned.contains("입니다") || cleaned.contains("됩니다") || cleaned.contains("해요"))) {
            cleaned += "."
        }
        
        return cleaned
    }
    
    /**
     * LLM을 사용하여 답변을 형식적이고 포멀한 형태로 재가공
     */
    private suspend fun reformatAnswerWithLLM(question: String, cleanedAnswer: String): String {
        return try {
            logger.debug("LLM으로 답변 재가공 시작: '$cleanedAnswer'")
            
            val reformatPrompt = buildString {
                append("다음 질문과 답변을 분석해서, 답변을 회사 공식 Q&A 형태로 재작성해주세요.\n\n")
                append("**요구사항:**\n")
                append("1. 형식적이고 포멀한 톤으로 작성\n")
                append("2. 핵심 정보는 정확히 유지\n")
                append("3. 간결하고 명확하게 표현\n")
                append("4. 한국어 존댓말 사용\n")
                append("5. 불확실한 표현('같아요', '아마') 제거\n\n")
                append("**질문:** $question\n")
                append("**원본 답변:** $cleanedAnswer\n\n")
                append("**재작성된 답변:**")
            }
            
            val result = slackRepository.reformatAnswerWithGemini(reformatPrompt)
            
            // Gemini 직접 호출 결과 처리
            if (result.isNotEmpty() && result != cleanedAnswer) {
                logger.info("🔄 Gemini 재가공 성공: '$result'")
                result
            } else {
                logger.info("⚠️ Gemini 재가공 실패, 정제된 답변 사용")
                cleanedAnswer
            }
            
        } catch (e: Exception) {
            logger.warn("LLM 답변 재가공 실패, 정제된 답변 사용", e)
            cleanedAnswer
        }
    }
    
    
    /**
     * 스레드의 원본 질문 가져오기
     */
    private suspend fun getOriginalQuestionFromThread(channel: String, threadTs: String): String {
        return try {
            val response = slack.methods(botToken).conversationsReplies { req ->
                req.channel(channel).ts(threadTs).limit(1)
            }
            
            if (response.isOk && !response.messages.isNullOrEmpty()) {
                response.messages[0].text ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            logger.debug("원본 질문 가져오기 실패", e)
            ""
        }
    }

    /**
     * 질문 처리 및 답변
     */
    private suspend fun processQuestion(question: String, channel: String, ts: String) {
        try {
            // 중복 처리 방지
            val messageKey = "$channel:$ts"
            if (processedMessages.contains(messageKey)) {
                logger.debug("이미 처리된 메시지: $messageKey")
                return
            }
            processedMessages.add(messageKey)
            
            logger.info("🔍 Q&A 검색 시작: '$question'")
            
            
            // 검색 중 이모지 추가
            addReaction(channel, ts, "mag")
            
            // 실제 채널명 획득 (채널 ID에서 이름으로 변환)
            val channelName = getChannelNameFromId(channel) ?: "unknown"
            
            // Q&A 검색 (실제 채널 데이터 사용)
            val request = SlackQARequest(
                question = question,
                channel = channelName,
                threshold = similarityThreshold
            )
            
            val result = slackService.searchSimilarQuestion(request)
            
            if (result.found) {
                logger.info("✅ 유사한 질문 발견! 유사도: ${result.similarity}")
                
                // 답변 찾는 중
                addReaction(channel, ts, "brain")
                
                // 자연스러운 답변 생성
                val naturalAnswer = generateNaturalAnswer(result, channelName)
                
                // 최종 성공 - 진행 상황 이모지들 제거하고 완료 표시
                removeReaction(channel, ts, "mag") 
                removeReaction(channel, ts, "brain")
                addReaction(channel, ts, "white_check_mark")
                
                sendMessage(channel, naturalAnswer, ts)
                
            } else {
                logger.info("❓ 유사한 질문 없음")
                
                // 검색 완료했지만 결과 없음 - 검색 이모지만 제거
                removeReaction(channel, ts, "mag")
                addReaction(channel, ts, "question")
                
                // 채널 담당자 멘션
                val managerMessage = getChannelManagerMessage()
                sendMessage(channel, managerMessage, ts)
            }
            
        } catch (e: Exception) {
            logger.error("질문 처리 오류", e)
            
            // 에러 시 진행 상황 리액션 제거하고 에러 표시 (eyes는 유지)
            removeReaction(channel, ts, "mag")
            removeReaction(channel, ts, "brain")
            addReaction(channel, ts, "x")
            
            sendMessage(channel, "죄송해요, 검색 중 오류가 발생했어요 😓 잠시 후 다시 시도해주세요!", ts)
        }
    }

    /**
     * 질문인지 판단
     */
    private fun isQuestion(text: String): Boolean {
        val questionKeywords = listOf("?", "？", "무엇", "뭐", "어떻게", "왜", "언제", "어디서", "누가", "방법", "어떤")
        val lowerText = text.lowercase()
        return questionKeywords.any { lowerText.contains(it) }
    }

    /**
     * 자연스러운 답변 생성 
     */
    private suspend fun generateNaturalAnswer(result: SlackQAResult, channel: String): String {
        logger.info("답변 생성: result.answer='${result.answer}', length=${result.answer.length}")
        
        return if (result.answer.isNotEmpty() && result.answer.length > 3) {
            // 실제 답변이 있으면 그대로 사용
            logger.info("기존 답변 사용: '${result.answer}'")
            result.answer
        } else {
            // 답변이 없으면 해당 질문의 스레드에서 실제 답변 찾기
            logger.info("스레드에서 답변 재검색 시작...")
            val actualAnswer = findActualAnswerFromThread(result.matchedQuestion ?: "", channel)
            if (actualAnswer.isNotEmpty()) {
                logger.info("스레드에서 찾은 답변: '${actualAnswer}'")
                actualAnswer
            } else {
                logger.info("답변을 찾지 못함")
                getChannelManagerMessage()
            }
        }
    }

    /**
     * 실제 스레드에서 답변 찾기
     */
    private suspend fun findActualAnswerFromThread(question: String, channelName: String): String {
        try {
            // 채널명에서 채널 ID 찾기
            val channelId = findChannelIdByName(channelName) ?: return ""
            
            // 해당 질문 메시지 찾기
            val response = slack.methods(botToken).conversationsHistory { req ->
                req.channel(channelId).limit(200)
            }
            
            if (!response.isOk) return ""
            
            // 질문과 매칭되는 메시지 찾기
            val matchedMessage = response.messages?.find { message ->
                val text = message.text ?: ""
                val similarity = calculateSimpleSimilarity(question, text)
                similarity > 0.5
            }
            
            if (matchedMessage != null) {
                // 해당 메시지의 스레드 답변들 가져오기
                val threadResponse = slack.methods(botToken).conversationsReplies { req ->
                    req.channel(channelId).ts(matchedMessage.ts)
                }
                
                if (threadResponse.isOk) {
                    val threadMessages = threadResponse.messages?.drop(1) ?: emptyList()
                    logger.info("스레드 메시지 ${threadMessages.size}개 발견")
                    
                    // 모든 답변 후보 찾기 (사람의 답변만)
                    val botId = getBotUserId()
                    val answerCandidates = threadMessages
                        .filter { it.botId == null && it.user != botId } // 봇 메시지 완전 제외
                        .mapNotNull { it.text }
                        .filter { it.trim().isNotEmpty() } // 빈 메시지 제외
                        .filter { it.length >= 3 } // 최소 3글자 이상
                    
                    logger.info("답변 후보 ${answerCandidates.size}개: ${answerCandidates.joinToString { "\"$it\"" }}")
                    
                    if (answerCandidates.isNotEmpty()) {
                        // 질문이 아닌 답변 우선 선택
                        val nonQuestionAnswers = answerCandidates.filter { !isQuestion(it) }
                        
                        if (nonQuestionAnswers.isNotEmpty()) {
                            val bestAnswer = nonQuestionAnswers.maxByOrNull { it.length }
                            logger.info("최종 선택된 답변: \"$bestAnswer\"")
                            return bestAnswer ?: ""
                        }
                        
                        // 질문이 아닌 답변이 없으면 가장 긴 답변 선택
                        val fallbackAnswer = answerCandidates.maxByOrNull { it.length }
                        logger.info("대체 답변 선택: \"$fallbackAnswer\"")
                        return fallbackAnswer ?: ""
                    }
                    
                    logger.info("스레드에서 적절한 답변을 찾지 못함")
                    return ""
                }
            }
            
            return ""
            
        } catch (e: Exception) {
            logger.debug("스레드에서 답변 찾기 실패", e)
            return ""
        }
    }

    /**
     * 채널명으로 채널 ID 찾기
     */
    private suspend fun findChannelIdByName(channelName: String): String? {
        return try {
            val response = slack.methods(botToken).conversationsList { req ->
                req.limit(1000)
            }
            
            if (response.isOk) {
                response.channels?.find { it.name == channelName }?.id
            } else {
                null
            }
        } catch (e: Exception) {
            logger.debug("채널 ID 찾기 실패", e)
            null
        }
    }

    /**
     * 키워드 기반 유사도 계산 (관대한 매칭)
     */
    private fun calculateSimpleSimilarity(text1: String, text2: String): Double {
        val words1 = text1.lowercase()
            .replace(Regex("[^가-힣a-z0-9\\s]"), " ")
            .split("\\s+".toRegex())
            .filter { it.length >= 2 }
        val words2 = text2.lowercase()
            .replace(Regex("[^가-힣a-z0-9\\s]"), " ")
            .split("\\s+".toRegex())
            .filter { it.length >= 2 }
        
        if (words1.isEmpty() || words2.isEmpty()) return 0.0
        
        // 정확 매칭
        val exactMatches = words1.intersect(words2.toSet()).size
        
        // 부분 매칭 (포함 관계)
        val partialMatches = words1.count { w1 ->
            words2.any { w2 -> 
                w1.contains(w2) || w2.contains(w1) ||
                // 편집거리 1 이하인 유사 단어
                calculateEditDistance(w1, w2) <= 1
            }
        }
        
        // 가중치 점수 계산
        val totalScore = (exactMatches * 2.0) + (partialMatches * 1.0)
        val maxPossibleScore = maxOf(words1.size, words2.size) * 2.0
        
        return (totalScore / maxPossibleScore).coerceAtMost(1.0)
    }
    
    /**
     * 편집 거리 계산
     */
    private fun calculateEditDistance(s1: String, s2: String): Int {
        if (s1.length > 10 || s2.length > 10) return Int.MAX_VALUE // 너무 긴 단어는 스킵
        
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i-1] == s2[j-1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i-1][j] + 1,
                    dp[i][j-1] + 1,
                    dp[i-1][j-1] + cost
                )
            }
        }
        
        return dp[len1][len2]
    }

    /**
     * ACK 응답 전송
     */
    private fun sendAcknowledgment(envelopeId: String) {
        try {
            val ackMessage = objectMapper.writeValueAsString(mapOf("envelope_id" to envelopeId))
            webSocketClient?.send(ackMessage)
        } catch (e: Exception) {
            logger.error("ACK 전송 실패", e)
        }
    }

    /**
     * 메시지 전송
     */
    private suspend fun sendMessage(channel: String, text: String, threadTs: String? = null): String? {
        return try {
            val response = slack.methods(botToken).chatPostMessage { req ->
                req.channel(channel)
                    .text(text)
                    .threadTs(threadTs)
            }
            if (response.isOk) {
                logger.info("💬 메시지 전송 완료")
                response.ts
            } else {
                logger.error("메시지 전송 실패: ${response.error}")
                null
            }
        } catch (e: Exception) {
            logger.error("메시지 전송 실패", e)
            null
        }
    }

    /**
     * 리액션 추가
     */
    private suspend fun addReaction(channel: String, timestamp: String, reaction: String) {
        try {
            val response = slack.methods(botToken).reactionsAdd { req ->
                req.channel(channel).timestamp(timestamp).name(reaction)
            }
            if (!response.isOk) {
                logger.warn("리액션 추가 실패: $reaction, 오류: ${response.error}")
            }
        } catch (e: Exception) {
            logger.warn("리액션 추가 실패: $reaction", e)
        }
    }

    /**
     * 리액션 제거
     */
    private suspend fun removeReaction(channel: String, timestamp: String, reaction: String) {
        try {
            val response = slack.methods(botToken).reactionsRemove { req ->
                req.channel(channel).timestamp(timestamp).name(reaction)
            }
            if (!response.isOk) {
                logger.debug("리액션 제거 실패: $reaction, 오류: ${response.error}")
            }
        } catch (e: Exception) {
            logger.debug("리액션 제거 실패: $reaction", e)
        }
    }

    /**
     * 채널 ID에서 채널 이름 조회
     */
    private suspend fun getChannelNameFromId(channelId: String): String? {
        return try {
            val response = slack.methods(botToken).conversationsInfo { req ->
                req.channel(channelId)
            }
            response.channel?.name
        } catch (e: Exception) {
            logger.warn("채널 정보 조회 실패: channelId=$channelId", e)
            null
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
    
    /**
     * 채널 담당자 안내 메시지 생성
     */
    private fun getChannelManagerMessage(): String {
        return "새로운 질문이네요! 채널 담당자 님께 문의해주시면 좋을 것 같아요 💡"
    }
}