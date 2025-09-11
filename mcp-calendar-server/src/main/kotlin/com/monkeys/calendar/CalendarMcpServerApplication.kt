package com.monkeys.calendar

import com.monkeys.calendar.service.CalendarMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class CalendarMcpServerApplication {

    @Bean
    fun calendarTools(calendarMcpService: CalendarMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(calendarMcpService)
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<CalendarMcpServerApplication>(*args)
}