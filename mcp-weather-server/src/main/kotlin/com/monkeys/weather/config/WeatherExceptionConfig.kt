package com.monkeys.weather.config

import com.monkeys.shared.exception.GlobalExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Weather 서버용 예외 처리 설정
 */
@Configuration
class WeatherExceptionConfig {

    @Bean
    fun globalExceptionHandler() = GlobalExceptionHandler()
}