package com.monkeys.client.controller

import com.monkeys.client.service.chat.BasicChatService
import com.monkeys.client.service.chat.StructuredResponseService
import com.monkeys.client.service.chat.StreamingChatService
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
    private val streamingChatService: StreamingChatService
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
            "Library" to mapOf(
                "searchBooks" to "도서를 검색합니다",
                "getBookByIsbn" to "ISBN으로 도서를 조회합니다",
                "getAvailableBooks" to "대출 가능한 도서 목록을 조회합니다",
                "borrowBook" to "도서를 대출합니다",
                "returnBook" to "도서를 반납합니다",
                "getOverdueLoans" to "연체 대출 목록을 조회합니다",
                "getLibraryStats" to "도서관 통계를 조회합니다"
            ),
            "Todo" to mapOf(
                "createTodoList" to "할일 목록을 생성합니다",
                "createTodo" to "할일을 생성합니다",
                "getMyTodos" to "내 할일 목록을 조회합니다",
                "completeTodo" to "할일을 완료합니다",
                "getOverdueTodos" to "기한 초과 할일을 조회합니다",
                "getTodoStats" to "할일 통계를 조회합니다"
            ),
            "Employee" to mapOf(
                "searchEmployees" to "직원을 검색합니다",
                "getEmployeeByNumber" to "사번으로 직원을 조회합니다",
                "getEmployeesByDepartment" to "부서별 직원 목록을 조회합니다",
                "changeDepartment" to "부서를 변경합니다",
                "updateSalary" to "급여를 변경합니다",
                "getEmployeeStats" to "직원 통계를 조회합니다"
            ),
            "Product" to mapOf(
                "searchProducts" to "상품을 검색합니다",
                "findProductBySku" to "SKU로 상품을 조회합니다",
                "addStock" to "재고를 추가합니다",
                "removeStock" to "재고를 차감합니다",
                "getLowStockProducts" to "재고 부족 상품을 조회합니다",
                "getProductStats" to "상품 통계를 조회합니다"
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
