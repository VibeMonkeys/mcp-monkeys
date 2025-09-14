package com.monkeys.client.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * AI 모델 설정 클래스
 * 향후 다중 모델 지원을 위한 기본 구조
 */
@Configuration
class AiModelConfiguration {
    
    /**
     * 주 AI 모델 (현재는 Vertex AI Gemini)
     */
    @Bean
    @Primary
    fun primaryChatClient(chatClient: ChatClient): ChatClient {
        return chatClient
    }
    
    /**
     * 백업 모델 설정 (향후 구현)
     */
    // @Bean
    // @Qualifier("backup")
    // fun backupChatClient(): ChatClient {
    //     // 백업 모델 설정 (예: OpenAI, Claude 등)
    //     return ChatClient.create()
    // }
    
    /**
     * 특화 모델 설정 (향후 구현)
     */
    // @Bean
    // @Qualifier("specialized")
    // fun specializedChatClient(): ChatClient {
    //     // 특정 작업에 특화된 모델 (예: 코드 생성, 번역 등)
    //     return ChatClient.create()
    // }
}