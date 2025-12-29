package com.monkeys.client.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank

@Configuration
@EnableConfigurationProperties(McpServerUrls::class)
class SecurityConfig

@Component
@ConfigurationProperties(prefix = "mcp")
@Validated
data class McpServerUrls(
    val library: ServerConfig = ServerConfig("http://localhost:8091"),
    val todo: ServerConfig = ServerConfig("http://localhost:8096"),
    val employee: ServerConfig = ServerConfig("http://localhost:8097"),
    val product: ServerConfig = ServerConfig("http://localhost:8098")
) {
    data class ServerConfig(
        @field:NotBlank
        val url: String
    )
}