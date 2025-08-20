package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("Slack 메시지 정보")
data class SlackMessage(
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 타임스탬프")
    val timestamp: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 내용")
    val text: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("발신자 사용자 ID")
    val user: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 ID")
    val channel: String,
    
    @JsonPropertyDescription("발신자 이름")
    val username: String?,
    
    @JsonPropertyDescription("스레드 타임스탬프")
    val threadTimestamp: String?,
    
    @JsonPropertyDescription("반응 목록")
    val reactions: List<SlackReaction> = emptyList(),
    
    @JsonPropertyDescription("첨부파일 목록")
    val attachments: List<SlackAttachment> = emptyList()
)

@JsonClassDescription("Slack 채널 정보")
data class SlackChannel(
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 ID")
    val id: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 이름")
    val name: String,
    
    @JsonPropertyDescription("채널 설명")
    val purpose: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("공개 채널 여부")
    val isPublic: Boolean,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 멤버 수")
    val memberCount: Int,
    
    @JsonPropertyDescription("채널 생성일")
    val createdAt: String?
)

@JsonClassDescription("Slack 사용자 정보")
data class SlackUser(
    @JsonProperty(required = true)
    @JsonPropertyDescription("사용자 ID")
    val id: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("사용자 이름")
    val name: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("실명")
    val realName: String,
    
    @JsonPropertyDescription("이메일")
    val email: String?,
    
    @JsonPropertyDescription("프로필 이미지 URL")
    val profileImage: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("온라인 상태")
    val isOnline: Boolean,
    
    @JsonPropertyDescription("상태 메시지")
    val statusText: String?
)

@JsonClassDescription("Slack 반응 정보")
data class SlackReaction(
    @JsonProperty(required = true)
    @JsonPropertyDescription("이모지 이름")
    val name: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("반응한 사용자 수")
    val count: Int,
    
    @JsonPropertyDescription("반응한 사용자 목록")
    val users: List<String> = emptyList()
)

@JsonClassDescription("Slack 첨부파일 정보")
data class SlackAttachment(
    @JsonProperty(required = true)
    @JsonPropertyDescription("첨부파일 ID")
    val id: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("파일명")
    val name: String,
    
    @JsonPropertyDescription("파일 타입")
    val mimetype: String?,
    
    @JsonPropertyDescription("파일 크기 (바이트)")
    val size: Long?,
    
    @JsonPropertyDescription("다운로드 URL")
    val url: String?
)