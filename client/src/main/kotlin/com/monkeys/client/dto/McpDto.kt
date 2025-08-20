package com.monkeys.client.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("MCP 서버와의 통신을 위한 요청 데이터")
data class McpChatRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("사용자의 채팅 메시지")
    val userInput: String,
    
    @JsonPropertyDescription("요청 타임스탬프")
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClassDescription("MCP 서버로부터 받는 응답 데이터")
data class McpChatResponse(
    @JsonProperty(required = true)
    @JsonPropertyDescription("AI 어시스턴트의 응답 메시지")
    val message: String,
    
    @JsonPropertyDescription("응답 타임스탬프")
    val timestamp: Long? = null,
    
    @JsonPropertyDescription("사용된 도구들의 목록")
    val usedTools: List<String>? = null
)

@JsonClassDescription("MCP 서버의 사용 가능한 도구 정보")
data class McpToolInfo(
    @JsonProperty(required = true)
    @JsonPropertyDescription("도구 이름")
    val name: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("도구 설명")
    val description: String,
    
    @JsonPropertyDescription("도구 카테고리")
    val category: String? = null
)

@JsonClassDescription("채팅 메시지 정보")
data class McpChatMessage(
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 역할 (USER, ASSISTANT)")
    val role: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 내용")
    val content: String,
    
    @JsonPropertyDescription("메시지 생성 시간")
    val timestamp: String? = null
)

@JsonClassDescription("MCP 서버 상태 정보")
data class McpServerStatus(
    @JsonProperty(required = true)
    @JsonPropertyDescription("서버 상태 (running, stopped, error)")
    val status: String,
    
    @JsonPropertyDescription("서버 버전")
    val version: String? = null,
    
    @JsonPropertyDescription("사용 가능한 도구 개수")
    val availableToolsCount: Int? = null,
    
    @JsonPropertyDescription("서버 업타임")
    val uptime: String? = null
)