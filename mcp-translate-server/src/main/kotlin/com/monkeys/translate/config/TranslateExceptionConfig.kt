package com.monkeys.translate.config

import com.monkeys.shared.exception.GlobalExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Translate 서버용 예외 처리 설정
 */
@Configuration
class TranslateExceptionConfig {

    @Bean
    fun globalExceptionHandler() = GlobalExceptionHandler()
}