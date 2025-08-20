package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("Gmail 메일 정보")
data class GmailMessage(
    @JsonProperty(required = true)
    @JsonPropertyDescription("메일 ID")
    val id: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메일 제목")
    val subject: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("발신자")
    val from: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("수신자")
    val to: List<String>,
    
    @JsonPropertyDescription("참조")
    val cc: List<String> = emptyList(),
    
    @JsonPropertyDescription("숨은참조")
    val bcc: List<String> = emptyList(),
    
    @JsonPropertyDescription("메일 본문")
    val body: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("읽음 여부")
    val isRead: Boolean,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("수신 날짜")
    val receivedAt: String,
    
    @JsonPropertyDescription("라벨 목록")
    val labels: List<String> = emptyList(),
    
    @JsonPropertyDescription("첨부파일 개수")
    val attachmentCount: Int = 0
)

@JsonClassDescription("Gmail 라벨 정보")
data class GmailLabel(
    @JsonProperty(required = true)
    @JsonPropertyDescription("라벨 ID")
    val id: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("라벨 이름")
    val name: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("라벨 타입 (system, user)")
    val type: String,
    
    @JsonPropertyDescription("미읽은 메일 수")
    val unreadCount: Int = 0,
    
    @JsonPropertyDescription("전체 메일 수")
    val totalCount: Int = 0
)

@JsonClassDescription("Gmail 메일 작성 요청")
data class GmailComposeRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("수신자 이메일")
    val to: List<String>,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메일 제목")
    val subject: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메일 본문")
    val body: String,
    
    @JsonPropertyDescription("참조")
    val cc: List<String> = emptyList(),
    
    @JsonPropertyDescription("숨은참조")
    val bcc: List<String> = emptyList(),
    
    @JsonPropertyDescription("HTML 형식 여부")
    val isHtml: Boolean = false
)