package com.monkeys.client.config

import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.slf4j.LoggerFactory

/**
 * ChatMemory 설정
 * Spring AI 2.0의 기본 InMemoryChatMemory 사용
 * 영속성이 필요한 경우 추후 ChatMemoryRepository 구현체로 교체 가능
 */
@Configuration
class ChatMemoryConfig {

    private val logger = LoggerFactory.getLogger(ChatMemoryConfig::class.java)

    /**
     * ChatMemory Bean 정의
     * MessageWindowChatMemory: 최근 N개의 메시지만 유지하는 윈도우 방식
     */
    @Bean
    fun chatMemory(): ChatMemory {
        logger.info("ChatMemory 초기화: MessageWindowChatMemory (maxMessages=20)")
        return MessageWindowChatMemory.builder()
            .maxMessages(20)
            .build()
    }
}
