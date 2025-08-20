package com.monkeys.server.controller

import com.monkeys.server.dto.ChatRequest
import com.monkeys.server.dto.ChatResponse
import com.monkeys.server.entity.ChatMessage
import com.monkeys.server.repository.ChatMessageRepository
import com.monkeys.server.service.ToolService
import com.monkeys.server.service.AdvancedToolService
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

@RestController
@RequestMapping("/api")
class ChatController(
    private val chatClient: ChatClient,
    private val toolService: ToolService,
    private val advancedToolService: AdvancedToolService,
    private val chatMessageRepository: ChatMessageRepository
) {
    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        logger.info("채팅 요청 처리 시작: {}", request.userInput)
        
        val startTime = System.currentTimeMillis()
        
        return try {
            // 1. 사용자 메시지 저장
            val userMessage = ChatMessage(role = "USER", content = request.userInput, timestamp = LocalDateTime.now())
            chatMessageRepository.save(userMessage)
            logger.debug("사용자 메시지 저장 완료")

            // 2. 이전 대화 기록 불러오기 (최근 10개 메시지)
            val history = chatMessageRepository.findTop10ByOrderByIdDesc()
                .reversed() // 시간순 정렬
            logger.debug("대화 기록 조회 완료: {} 개 메시지", history.size)

            // 3. ChatClient를 통해 Tool과 함께 요청
            var response: String
            val processingTime = measureTimeMillis {
                response = chatClient.prompt()
                    .user(buildPrompt(history, request.userInput))
                    .tools(toolService, advancedToolService) // 기본 도구와 고급 도구 모두 포함
                    .call()
                    .content() ?: "죄송합니다. 응답을 생성할 수 없습니다."
            }
            logger.info("AI 응답 생성 완료. 처리 시간: {}ms", processingTime)

            // 4. AI 응답 저장
            val aiResponse = ChatMessage(role = "ASSISTANT", content = response, timestamp = LocalDateTime.now())
            chatMessageRepository.save(aiResponse)
            logger.debug("AI 응답 저장 완료")

            val totalTime = System.currentTimeMillis() - startTime
            
            ResponseEntity.ok(
                ChatResponse(
                    message = response,
                    timestamp = System.currentTimeMillis(),
                    processingTimeMs = totalTime
                )
            )
        } catch (e: Exception) {
            logger.error("채팅 처리 중 오류 발생: {}", e.message, e)
            ResponseEntity.status(500).body(
                ChatResponse(
                    message = "죄송합니다. 요청 처리 중 오류가 발생했습니다: ${e.message}",
                    timestamp = System.currentTimeMillis(),
                    processingTimeMs = System.currentTimeMillis() - startTime
                )
            )
        }
    }

    private fun buildPrompt(history: List<ChatMessage>, userInput: String): String {
        val historyText = if (history.isEmpty()) {
            "이전 대화가 없습니다."
        } else {
            history.joinToString("\n") { "${it.role}: ${it.content}" }
        }

        return """당신은 다양한 도구에 접근할 수 있는 도움이 되는 AI 어시스턴트입니다. 
사용자의 질문에 정확하고 도움이 되는 정보를 제공하기 위해 적절한 경우 도구를 사용하세요.

사용 가능한 도구들:
- 기본 도구: 날짜/시간 조회, UUID 생성, 랜덤 숫자 생성, 계산, 텍스트 변환, 시스템 정보
- 고급 도구: 작업 정보 생성, 사용자 프로필 생성, 텍스트 분석, 날씨 정보 생성

이전 대화:
$historyText

사용자: $userInput

사용자의 질문에 자연스럽게 답변하되, 질문에 도움이 될 수 있는 도구가 있다면 적절히 활용해주세요."""
    }

    @GetMapping("/history")
    fun getChatHistory(): List<ChatMessage> {
        return chatMessageRepository.findAll().sortedBy { it.id }
    }

    @DeleteMapping("/history")
    fun clearChatHistory() {
        chatMessageRepository.deleteAll()
    }

    @GetMapping("/tools")
    fun getAvailableTools(): Map<String, Map<String, String>> {
        return mapOf(
            "기본도구" to mapOf(
                "getCurrentDateTime" to "현재 날짜와 시간을 다양한 형식으로 가져옵니다",
                "generateUuid" to "고유한 UUID(범용 고유 식별자)를 생성합니다",
                "generateRandomNumber" to "지정된 범위 내에서 임의의 숫자를 생성합니다",
                "calculate" to "간단한 수학 연산을 계산합니다",
                "convertTextCase" to "텍스트를 다양한 대소문자 형식으로 변환합니다",
                "getSystemInfo" to "시스템 정보와 상태를 조회합니다"
            ),
            "고급도구" to mapOf(
                "generateTaskInfo" to "사용자 입력을 기반으로 구조화된 작업 정보를 생성합니다",
                "generateUserProfile" to "사용자 정보를 기반으로 구조화된 프로필을 생성합니다",
                "analyzeText" to "텍스트를 분석하여 구조화된 결과를 제공합니다",
                "generateWeatherInfo" to "가상의 날씨 정보를 생성합니다 (실제 API 연동 없이 테스트용)",
                "getMultiCityWeather" to "여러 도시의 날씨 정보를 한 번에 조회합니다"
            )
        )
    }
}
