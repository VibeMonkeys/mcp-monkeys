package com.monkeys.shared.config

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(HttpClientProperties::class)
class HttpClientConfig(
    private val properties: HttpClientProperties
) {
    private val logger = LoggerFactory.getLogger(HttpClientConfig::class.java)

    @Bean
    fun sharedOkHttpClient(): OkHttpClient {
        logger.info("공통 OkHttpClient 설정: connectTimeout=${properties.connectTimeoutSeconds}s, readTimeout=${properties.readTimeoutSeconds}s, maxIdleConnections=${properties.maxIdleConnections}")
        
        return OkHttpClient.Builder()
            .connectTimeout(properties.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(properties.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(properties.writeTimeoutSeconds, TimeUnit.SECONDS)
            .connectionPool(
                ConnectionPool(
                    properties.maxIdleConnections, 
                    properties.keepAliveDurationMinutes, 
                    TimeUnit.MINUTES
                )
            )
            .retryOnConnectionFailure(properties.retryOnConnectionFailure)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "MCP-Monkeys/${properties.userAgent}")
                    .build()
                
                val startTime = System.nanoTime()
                val response = chain.proceed(request)
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                
                logger.debug("HTTP 요청 완료: {} {} - {}ms", 
                    request.method, 
                    request.url.host, 
                    duration.toMillis()
                )
                
                response
            }
            .build()
    }
}

@ConfigurationProperties(prefix = "shared.http-client")
data class HttpClientProperties(
    val connectTimeoutSeconds: Long = 10,
    val readTimeoutSeconds: Long = 30,
    val writeTimeoutSeconds: Long = 30,
    val maxIdleConnections: Int = 10,
    val keepAliveDurationMinutes: Long = 5,
    val retryOnConnectionFailure: Boolean = true,
    val userAgent: String = "1.0.0"
)