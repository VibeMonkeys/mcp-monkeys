package com.monkeys.server.client

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerateContentResponse
import com.google.cloud.vertexai.generativeai.GenerativeModel
import com.google.cloud.vertexai.generativeai.ResponseHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class GeminiClient(
    private val vertexAI: VertexAI,
    @Value("${gcp.model.name}") private val modelName: String
) {
    /**
     * 주어진 프롬프트를 사용하여 Gemini 모델에 콘텐츠 생성을 요청합니다.
     */
    fun generateContent(prompt: String): Mono<String> {
        return Mono.fromCallable {
            val generativeModel = GenerativeModel(modelName, vertexAI)
            val response: GenerateContentResponse = generativeModel.generateContent(prompt)
            ResponseHandler.getText(response)
        }.subscribeOn(Schedulers.boundedElastic()) // I/O-bound 작업을 별도 스레드에서 실행
    }
}