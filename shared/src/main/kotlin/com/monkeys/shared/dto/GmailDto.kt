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
    
    @JsonPropertyDescription("메일 스니펫")
    val snippet: String,
    
    @JsonPropertyDescription("메일 날짜")
    val date: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("읽지 않음 여부")
    val isUnread: Boolean
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

@JsonClassDescription("Gmail 메일 발송 결과")
data class GmailSendResult(
    @JsonProperty(required = true)
    @JsonPropertyDescription("발송 성공 여부")
    val success: Boolean,
    
    @JsonPropertyDescription("메시지 ID")
    val messageId: String?,
    
    @JsonPropertyDescription("오류 메시지")
    val error: String?
)