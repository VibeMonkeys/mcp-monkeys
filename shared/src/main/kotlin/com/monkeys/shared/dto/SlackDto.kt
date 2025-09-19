package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("Slack 메시지 정보")
data class SlackMessage(
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 타임스탬프")
    val ts: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("작성자 ID")
    val user: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 내용")
    val text: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 ID")
    val channel: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 타입")
    val type: String,
    
    @JsonPropertyDescription("스레드 타임스탬프")
    val threadTs: String? = null
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

@JsonClassDescription("Slack 메시지 발송 결과")
data class SlackSendResult(
    @JsonProperty(required = true)
    @JsonPropertyDescription("발송 성공 여부")
    val success: Boolean,
    
    @JsonPropertyDescription("메시지 타임스탬프")
    val timestamp: String?,
    
    @JsonPropertyDescription("오류 메시지")
    val error: String?
)

// MCP Tool용 Slack DTO들
@JsonClassDescription("Slack Q&A 응답")
data class SlackQAResponse(
    @JsonProperty(required = true)
    @JsonPropertyDescription("유사한 질문을 찾았는지 여부")
    val found: Boolean,
    
    @JsonProperty(required = true) 
    @JsonPropertyDescription("원본 질문")
    val question: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("답변")
    val answer: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("유사도 점수 (0.0~1.0)")
    val similarity: Double,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 이름")
    val channel: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("타임스탬프")
    val timestamp: Long,
    
    @JsonPropertyDescription("답변 작성자")
    val author: String? = null
)

@JsonClassDescription("Slack 메시지 전송 응답")
data class SlackMessageResponse(
    @JsonProperty(required = true)
    @JsonPropertyDescription("전송 성공 여부")
    val success: Boolean,
    
    @JsonPropertyDescription("메시지 ID")
    val messageId: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 이름")
    val channel: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("타임스탬프")
    val timestamp: Long,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("응답 메시지")
    val message: String
)

@JsonClassDescription("Slack 메시지 전송 요청")
data class SlackMessageRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 이름")
    val channel: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 내용")
    val text: String,
    
    @JsonPropertyDescription("스레드 타임스탬프")
    val threadTimestamp: String? = null
)

@JsonClassDescription("Slack 히스토리 응답")
data class SlackHistoryResponse(
    @JsonProperty(required = true)
    @JsonPropertyDescription("조회 성공 여부")
    val success: Boolean,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 이름")
    val channel: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 목록")
    val messages: List<SlackMessageInfo>,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("총 메시지 개수")
    val totalCount: Int,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("응답 메시지")
    val message: String
)

@JsonClassDescription("Slack 메시지 정보")
data class SlackMessageInfo(
    @JsonPropertyDescription("메시지 ID")
    val messageId: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("메시지 내용")
    val text: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("사용자 ID")
    val user: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("타임스탬프")
    val timestamp: Long,
    
    @JsonPropertyDescription("스레드 타임스탬프")
    val threadTimestamp: String? = null
)

// Weather 서버 패턴을 따른 Slack Q&A DTO들
@JsonClassDescription("Slack Q&A 엔트리")
data class SlackQAEntry(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Q&A ID")
    val id: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("질문")
    val question: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("답변")
    val answer: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널")
    val channel: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("작성자")
    val author: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("타임스탬프")
    val timestamp: Long,
    
    @JsonPropertyDescription("스레드 ID")
    val threadId: String?
)

@JsonClassDescription("Slack Q&A 매치 결과")
data class SlackQAMatch(
    @JsonProperty(required = true)
    @JsonPropertyDescription("Q&A 엔트리")
    val qaEntry: SlackQAEntry,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("유사도 점수")
    val similarity: Double
)

@JsonClassDescription("Slack 채널 통계")
data class SlackChannelStats(
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널 이름")
    val channel: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("총 질문 수")
    val totalQuestions: Int,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("고유 작성자 수")
    val uniqueAuthors: Int,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("가장 오래된 질문 타임스탬프")
    val oldestQuestion: Long,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("가장 최근 질문 타임스탬프")
    val newestQuestion: Long
)

@JsonClassDescription("Slack Q&A 검색 요청")
data class SlackQARequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("검색할 질문")
    val question: String,
    
    @JsonPropertyDescription("채널 (기본값: general)")
    val channel: String = "general",
    
    @JsonPropertyDescription("유사도 임계값 (기본값: 0.7)")
    val threshold: Double = 0.7
)

@JsonClassDescription("Slack Q&A 검색 결과")
data class SlackQAResult(
    @JsonProperty(required = true)
    @JsonPropertyDescription("유사한 질문 발견 여부")
    val found: Boolean,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("원본 질문")
    val originalQuestion: String,
    
    @JsonPropertyDescription("매치된 질문")
    val matchedQuestion: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("답변")
    val answer: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("유사도 점수")
    val similarity: Double,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("채널")
    val channel: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("타임스탬프")
    val timestamp: Long,
    
    @JsonPropertyDescription("작성자")
    val author: String?
)

