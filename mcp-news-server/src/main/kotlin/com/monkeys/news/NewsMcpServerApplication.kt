package com.monkeys.news

import com.monkeys.news.service.NewsMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class NewsMcpServerApplication {

    @Bean
    fun newsTools(newsMcpService: NewsMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(newsMcpService)
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<NewsMcpServerApplication>(*args)
}