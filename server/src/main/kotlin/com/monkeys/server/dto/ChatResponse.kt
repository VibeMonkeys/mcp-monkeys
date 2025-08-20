package com.monkeys.server.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("클라이언트에 전송하는 채팅 응답 데이터")
data class ChatResponse(
    @JsonProperty(required = true)
    @JsonPropertyDescription("AI 어시스턴트의 응답 메시지")
    val message: String,
    
    @JsonPropertyDescription("응답 생성 타임스탬프")
    val timestamp: Long = System.currentTimeMillis(),
    
    @JsonPropertyDescription("응답 생성에 사용된 도구들")
    val usedTools: List<String>? = null,
    
    @JsonPropertyDescription("처리 시간 (밀리초)")
    val processingTimeMs: Long? = null
)