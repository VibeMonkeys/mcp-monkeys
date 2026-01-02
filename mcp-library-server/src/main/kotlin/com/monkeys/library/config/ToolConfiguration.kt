package com.monkeys.library.config

import com.monkeys.library.adapter.`in`.mcp.LibraryMcpAdapter
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    @Bean
    fun libraryTools(libraryMcpAdapter: LibraryMcpAdapter): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(libraryMcpAdapter)
            .build()
    }
}
