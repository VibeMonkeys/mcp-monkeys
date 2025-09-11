package com.monkeys.news.config

import com.monkeys.shared.exception.GlobalExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * News 서버용 예외 처리 설정
 */
@Configuration
class NewsExceptionConfig {

    @Bean
    fun globalExceptionHandler() = GlobalExceptionHandler()
}