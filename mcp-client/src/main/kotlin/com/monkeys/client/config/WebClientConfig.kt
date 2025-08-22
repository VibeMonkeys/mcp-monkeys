package com.monkeys.client.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
class WebClientConfig {
    
    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
            .codecs { codecs ->
                codecs.defaultCodecs().maxInMemorySize(1024 * 1024) // 1MB
            }
    }
}