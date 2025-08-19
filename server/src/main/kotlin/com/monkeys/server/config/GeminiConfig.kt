package com.monkeys.server.config

import com.google.cloud.vertexai.VertexAI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeminiConfig(
    @Value("${gcp.project.id}") private val projectId: String,
    @Value("${gcp.location}") private val location: String
) {

    @Bean
    fun vertexAI(): VertexAI {
        return VertexAI(projectId, location)
    }
}