package com.monkeys.client.service

import com.monkeys.client.client.McpServerClient
import com.monkeys.client.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class McpService(
    private val mcpServerClient: McpServerClient
) {
    private val logger = LoggerFactory.getLogger(McpService::class.java)

    /**
     * 사용자 메시지를 MCP 서버에 전송하고 응답을 받습니다.
     */
    fun getAndProcessData(userInput: String): Mono<String> {
        logger.debug("사용자 입력 처리: {}", userInput)
        
        return mcpServerClient.sendChatMessage(userInput)
            .map { response ->
                logger.debug("MCP 서버 응답: {}", response.message)
                response.message
            }
            .doOnError { error ->
                logger.error("MCP 서버 통신 오류: {}", error.message)
            }
    }

    /**
     * 채팅 기록을 조회합니다.
     */
    fun getChatHistory(): Mono<List<McpChatMessage>> {
        logger.debug("채팅 기록 조회 요청")
        
        return mcpServerClient.getChatHistory()
            .doOnNext { history ->
                logger.debug("채팅 기록 조회 완료: {} 개 메시지", history.size)
            }
    }

    /**
     * 채팅 기록을 삭제합니다.
     */
    fun clearChatHistory(): Mono<Void> {
        logger.debug("채팅 기록 삭제 요청")
        
        return mcpServerClient.clearChatHistory()
            .doOnSuccess {
                logger.debug("채팅 기록 삭제 완료")
            }
    }

    /**
     * 사용 가능한 도구 목록을 조회합니다.
     */
    fun getAvailableTools(): Mono<Map<String, Any>> {
        logger.debug("사용 가능한 도구 목록 조회")
        
        return mcpServerClient.getAvailableTools()
            .doOnNext { tools ->
                logger.debug("도구 목록 조회 완료: {} 개 카테고리", tools.size)
            }
    }

    /**
     * MCP 서버 상태를 확인합니다.
     */
    fun getServerStatus(): Mono<McpServerStatus> {
        logger.debug("서버 상태 확인")
        
        return mcpServerClient.getServerStatus()
            .doOnNext { status ->
                logger.debug("서버 상태: {}", status.status)
            }
    }

    /**
     * 서버 연결을 테스트합니다.
     */
    fun testServerConnection(): Mono<Boolean> {
        logger.debug("서버 연결 테스트")
        
        return mcpServerClient.testConnection()
            .doOnNext { connected ->
                if (connected) {
                    logger.info("MCP 서버 연결 성공")
                } else {
                    logger.warn("MCP 서버 연결 실패")
                }
            }
    }
}
