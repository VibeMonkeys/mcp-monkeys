package com.monkeys.weather

import com.monkeys.weather.service.WeatherMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class WeatherMcpServerApplication {

    @Bean
    fun weatherTools(weatherMcpService: WeatherMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherMcpService)
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<WeatherMcpServerApplication>(*args)
}