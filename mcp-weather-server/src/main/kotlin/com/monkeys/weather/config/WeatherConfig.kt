package com.monkeys.weather.config

import com.monkeys.shared.config.HttpClientConfig
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(HttpClientConfig::class)
class WeatherConfig {

    @Bean
    fun weatherHttpClient(sharedOkHttpClient: OkHttpClient): OkHttpClient {
        // Weather 서버용 공통 HTTP 클라이언트 사용
        return sharedOkHttpClient
    }
}