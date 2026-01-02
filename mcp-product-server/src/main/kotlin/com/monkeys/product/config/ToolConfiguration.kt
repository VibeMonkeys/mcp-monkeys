package com.monkeys.product.config

import com.monkeys.product.adapter.`in`.mcp.ProductMcpAdapter
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    @Bean
    fun productTools(productMcpAdapter: ProductMcpAdapter): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(productMcpAdapter)
            .build()
    }
}
