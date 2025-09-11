package com.monkeys.client.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VertexAIConfig {

    @Autowired
    private lateinit var toolCallbackProvider: SyncMcpToolCallbackProvider

    @Bean
    fun chatClient(chatModel: VertexAiGeminiChatModel): ChatClient {
        val toolCallbacks = toolCallbackProvider.getToolCallbacks()
        
        // 디버깅: 등록된 도구들 로그 출력
        println("=== MCP 도구 등록 상황 ===")
        println("등록된 도구 수: ${toolCallbacks.size}")
        toolCallbacks.forEach { callback ->
            println("도구 클래스: ${callback.javaClass}")
            println("도구: $callback")
        }
        println("========================")
        
        return ChatClient.builder(chatModel)
            .defaultSystem(
                """
당신은 MCP Monkeys의 통합 AI 어시스턴트입니다.

사용 가능한 서비스들:
- 🌤️ Weather: 날씨 정보 조회
- 📰 News: 뉴스 검색 및 조회  
- 🌐 Translate: 텍스트 번역
- 📅 Calendar: 일정 관리

주요 특징:
- 여러 서비스를 연계한 복합적인 작업 수행 가능
- 대화 맥락을 기억하여 연속적인 대화 지원
- 실시간 정보 제공 및 업데이트

사용자의 요청을 정확히 파악하고 적절한 도구를 선택하여 도움이 되는 응답을 제공해주세요.
            """.trimIndent()
            )
            .defaultToolCallbacks(*toolCallbacks)
            .build()
    }
}