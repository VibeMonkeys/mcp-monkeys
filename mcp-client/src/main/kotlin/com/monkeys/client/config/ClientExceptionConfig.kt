package com.monkeys.client.config

import com.monkeys.shared.exception.GlobalExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Client 서버용 예외 처리 설정
 */
@Configuration
class ClientExceptionConfig {

    @Bean
    fun globalExceptionHandler() = GlobalExceptionHandler()
}