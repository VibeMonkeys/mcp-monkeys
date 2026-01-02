package com.monkeys.todo.config

import com.monkeys.todo.adapter.`in`.mcp.TodoMcpAdapter
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    @Bean
    fun todoToolCallbackProvider(todoMcpAdapter: TodoMcpAdapter): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(todoMcpAdapter)
            .build()
    }
}
