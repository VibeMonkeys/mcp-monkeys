package com.monkeys.todo.config

import com.monkeys.todo.service.TodoMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    @Bean
    fun todoToolCallbackProvider(todoMcpService: TodoMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(todoMcpService)
            .build()
    }
}
