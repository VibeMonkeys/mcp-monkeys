package com.monkeys.library.config

import com.monkeys.library.service.LibraryMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    /**
     * LibraryMcpService의 @Tool 어노테이션이 있는 메서드들을 자동으로 스캔하여 등록
     */
    @Bean
    fun libraryToolCallbackProvider(libraryMcpService: LibraryMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(libraryMcpService)
            .build()
    }
}
