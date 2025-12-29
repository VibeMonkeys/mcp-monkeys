package com.monkeys.product.config

import com.monkeys.product.service.ProductMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    @Bean
    fun productToolCallbackProvider(productMcpService: ProductMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(productMcpService)
            .build()
    }
}
