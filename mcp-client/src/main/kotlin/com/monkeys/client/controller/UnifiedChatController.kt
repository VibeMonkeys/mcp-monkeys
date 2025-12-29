package com.monkeys.client.controller

import com.monkeys.client.service.chat.BasicChatService
import com.monkeys.client.service.chat.StructuredResponseService
import com.monkeys.client.service.chat.StreamingChatService
import com.monkeys.client.service.ToolDiscoveryService
import com.monkeys.client.exception.ChatServiceException
import com.monkeys.shared.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import org.springframework.http.MediaType

/**
 * 통합 채팅 컨트롤러
 * 각 기능별로 명확한 엔드포인트 제공
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3004", "http://localhost:3000"])
class UnifiedChatController(
    private val basicChatService: BasicChatService,
    private val structuredResponseService: StructuredResponseService,
    private val streamingChatService: StreamingChatService,
    private val toolDiscoveryService: ToolDiscoveryService
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
    fun getAvailableTools(): ResponseEntity<BaseResponse<Map<String, List<Map<String, String>>>>> {
        val tools = toolDiscoveryService.getAvailableTools()
            .mapValues { (_, toolInfoList) ->
                toolInfoList.map { toolInfo ->
                    mapOf(
                        "name" to toolInfo.name,
                        "description" to toolInfo.description
                    )
                }
            }

        val totalCount = tools.values.sumOf { it.size }
        return ResponseEntity.ok(BaseResponse.success(tools, "사용 가능한 도구 목록 (총 ${totalCount}개)"))
    }

    @GetMapping("/tools/summary")
    fun getToolsSummary(): ResponseEntity<BaseResponse<Map<String, Int>>> {
        val summary = toolDiscoveryService.getToolsSummary()
        return ResponseEntity.ok(BaseResponse.success(summary, "서버별 도구 개수"))
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
            "version" to "2.0.0",
            "services" to serviceStatus,
            "mcpServers" to mapOf(
                "library" to "http://localhost:8091",
                "todo" to "http://localhost:8096",
                "employee" to "http://localhost:8097",
                "product" to "http://localhost:8098"
            ),
            "timestamp" to System.currentTimeMillis()
        )

        return ResponseEntity.ok(BaseResponse.success(status, "서비스 상태 정상"))
    }
}
