package com.monkeys.calendar.config

import com.monkeys.shared.exception.GlobalExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Calendar 서버용 예외 처리 설정
 */
@Configuration
class CalendarExceptionConfig {

    @Bean
    fun globalExceptionHandler() = GlobalExceptionHandler()
}