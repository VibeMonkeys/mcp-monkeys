package com.monkeys.client.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.slf4j.LoggerFactory

@Configuration
class VertexAIConfig {

    private val logger = LoggerFactory.getLogger(VertexAIConfig::class.java)

    @Autowired
    private lateinit var toolCallbackProvider: SyncMcpToolCallbackProvider

    @Bean
    fun chatClient(chatModel: VertexAiGeminiChatModel, chatMemory: ChatMemory): ChatClient {
        val toolCallbacks = toolCallbackProvider.getToolCallbacks()

        logger.info("=== MCP 도구 등록 상황 ===")
        logger.info("등록된 도구 수: ${toolCallbacks.size}")
        toolCallbacks.forEach { callback ->
            logger.debug("도구: $callback")
        }

        return ChatClient.builder(chatModel)
            .defaultSystem(SYSTEM_MESSAGE)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .defaultToolCallbacks(*toolCallbacks)
            .build()
    }

    companion object {
        const val SYSTEM_MESSAGE = """
당신은 MCP Monkeys의 통합 AI 어시스턴트입니다.

사용 가능한 서비스들:
- 📚 Library: 도서 검색, 대출, 반납 관리
- ✅ Todo: 할일 목록 생성 및 관리
- 👥 Employee: 직원 정보 검색 및 부서 관리
- 📦 Product: 상품 검색 및 재고 관리

주요 특징:
- 여러 서비스를 연계한 복합적인 작업 수행 가능
- 대화 맥락을 기억하여 연속적인 대화 지원
- 데이터베이스 기반의 영구 저장

사용자의 요청을 정확히 파악하고 적절한 도구를 선택하여 도움이 되는 응답을 제공해주세요.
"""
    }
}