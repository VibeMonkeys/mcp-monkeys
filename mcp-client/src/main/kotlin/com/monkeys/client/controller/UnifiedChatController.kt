package com.monkeys.client.controller

import com.monkeys.client.service.chat.BasicChatService
import com.monkeys.client.service.chat.StructuredResponseService
import com.monkeys.client.service.chat.StreamingChatService
import com.monkeys.client.service.SimpleUnifiedMcpService
import com.monkeys.client.service.ChatServiceException
import com.monkeys.shared.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import org.springframework.http.MediaType

/**
 * 분리된 채팅 컨트롤러
 * 각 기능별로 명확한 엔드포인트 제공
 * 책임 분리 원칙 적용
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3004", "http://localhost:3000"])
class UnifiedChatController(
    private val basicChatService: BasicChatService,
    private val structuredResponseService: StructuredResponseService,
    private val streamingChatService: StreamingChatService,
    private val unifiedMcpService: SimpleUnifiedMcpService
) {
    private val logger = LoggerFactory.getLogger(UnifiedChatController::class.java)

    /**
     * 일반 채팅 - 대화 메모리와 컨텍스트 관리
     */
    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<BaseResponse<ChatResponse>> {
        logger.info("일반 채팅 요청: sessionId=${request.sessionId}")
        
        return try {
            val response = basicChatService.generateResponse(request)
            ResponseEntity.ok(BaseResponse.success(response))
        } catch (e: ChatServiceException) {
            logger.error("채팅 처리 오류: ${e.message}", e)
            ResponseEntity.status(400).body(
                BaseResponse.failure("채팅 요청 처리 실패: ${e.message}", e.errorCode)
            )
        } catch (e: Exception) {
            logger.error("예상치 못한 채팅 오류", e)
            ResponseEntity.status(500).body(
                BaseResponse.failure("서버 내부 오류가 발생했습니다")
            )
        }
    }
    
    /**
     * 구조화된 응답 - 타입 안전성과 구조화된 데이터
     */
    @PostMapping("/chat/structured")
    fun chatStructured(@RequestBody request: ChatRequest): ResponseEntity<BaseResponse<StructuredChatResponse>> {
        logger.info("구조화된 채팅 요청: sessionId=${request.sessionId}")
        
        return try {
            val response = structuredResponseService.generateStructuredResponse(request)
            ResponseEntity.ok(BaseResponse.success(response))
        } catch (e: ChatServiceException) {
            logger.error("구조화된 채팅 처리 오류: ${e.message}", e)
            ResponseEntity.status(400).body(
                BaseResponse.failure("구조화된 채팅 요청 처리 실패: ${e.message}", e.errorCode)
            )
        } catch (e: Exception) {
            logger.error("예상치 못한 구조화된 채팅 오류", e)
            ResponseEntity.status(500).body(
                BaseResponse.failure("서버 내부 오류가 발생했습니다")
            )
        }
    }
    
    /**
     * 스트리밍 채팅 - 실시간 응답
     */
    @GetMapping(value = ["/chat/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun chatStream(
        @RequestParam message: String, 
        @RequestParam(required = false) sessionId: String?
    ): Flux<String> {
        logger.info("스트리밍 채팅 요청: sessionId=$sessionId")
        
        return streamingChatService.generateStreamingResponse(message, sessionId)
    }

    @GetMapping("/tools")
    fun getAvailableTools(): ResponseEntity<BaseResponse<Map<String, Map<String, String>>>> {
        val tools = mapOf(
            "Weather" to mapOf(
                "getCurrentWeather" to "현재 날씨 정보를 조회합니다",
                "getWeatherForecast" to "날씨 예보를 조회합니다",
                "compareWeather" to "여러 도시의 날씨를 비교합니다"
            ),
            "News" to mapOf(
                "getTopHeadlines" to "최신 뉴스 헤드라인을 조회합니다",
                "searchNews" to "키워드로 뉴스를 검색합니다",
                "getNewsBySource" to "특정 출처의 뉴스를 조회합니다"
            ),
            "Translate" to mapOf(
                "translateText" to "텍스트를 번역합니다",
                "detectLanguage" to "언어를 감지합니다",
                "getSupportedLanguages" to "지원되는 언어 목록을 조회합니다"
            ),
            "Calendar" to mapOf(
                "createCalendarEvent" to "캘린더 이벤트를 생성합니다",
                "getCalendarEvents" to "캘린더 이벤트 목록을 조회합니다",
                "deleteCalendarEvent" to "캘린더 이벤트를 삭제합니다"
            ),
            "External" to mapOf(
                "getGitHubIssues" to "GitHub 저장소의 이슈 목록을 조회합니다",
                "getJiraIssues" to "Jira 프로젝트의 이슈 목록을 조회합니다",
                "sendSlackMessage" to "Slack 채널에 메시지를 전송합니다"
            )
        )
        
        return ResponseEntity.ok(BaseResponse.success(tools, "사용 가능한 도구 목록"))
    }

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<BaseResponse<Map<String, Any>>> {
        val serviceStatus = mapOf(
            "basicChatService" to "active",
            "structuredResponseService" to "active", 
            "streamingChatService" to "active"
        )
        
        val status = mapOf(
            "status" to "UP",
            "service" to "Unified MCP Client",
            "version" to "1.0.0",
            "services" to serviceStatus,
            "mcpServers" to mapOf(
                "weather" to "http://localhost:8092",
                "news" to "http://localhost:8093", 
                "translate" to "http://localhost:8094",
                "calendar" to "http://localhost:8095"
            ),
            "timestamp" to System.currentTimeMillis()
        )
        
        return ResponseEntity.ok(BaseResponse.success(status, "서비스 상태 정상"))
    }

    @PostMapping("/api-status")
    fun checkApiStatus(): ResponseEntity<BaseResponse<Map<String, Any>>> {
        return try {
            val statusResult = unifiedMcpService.checkAllApiStatus()
            ResponseEntity.ok(
                BaseResponse.success(
                    mapOf("apiStatus" to statusResult),
                    "API 상태 확인 완료"
                )
            )
        } catch (e: Exception) {
            logger.error("API 상태 확인 실패", e)
            ResponseEntity.status(500).body(
                BaseResponse.failure<Map<String, Any>>("API 상태 확인 중 오류: ${e.message}")
            )
        }
    }
}