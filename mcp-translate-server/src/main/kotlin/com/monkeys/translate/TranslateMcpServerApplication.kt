package com.monkeys.translate

import com.monkeys.translate.service.TranslateMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class TranslateMcpServerApplication {

    @Bean
    fun translateTools(translateMcpService: TranslateMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(translateMcpService)
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<TranslateMcpServerApplication>(*args)
}