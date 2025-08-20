package com.monkeys.server.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("클라이언트로부터 받는 채팅 요청 데이터")
data class ChatRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("사용자가 입력한 메시지")
    val userInput: String,
    
    @JsonPropertyDescription("요청 타임스탬프")
    val timestamp: Long? = null
)