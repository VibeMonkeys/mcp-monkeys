package com.monkeys.server.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAIConfig {

    @Bean
    fun chatClient(chatModel: OpenAiChatModel): ChatClient {
        return ChatClient.create(chatModel)
    }
}