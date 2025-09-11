package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import java.time.LocalDateTime

/**
 * 캘린더 이벤트 DTO
 */
data class CalendarEvent(
    @JsonPropertyDescription("이벤트 ID")
    val id: String? = null,
    
    @JsonPropertyDescription("이벤트 제목")
    val title: String,
    
    @JsonPropertyDescription("이벤트 설명")
    val description: String? = null,
    
    @JsonPropertyDescription("시작 시간")
    val startTime: String,
    
    @JsonPropertyDescription("종료 시간")
    val endTime: String,
    
    @JsonPropertyDescription("위치")
    val location: String? = null,
    
    @JsonPropertyDescription("참가자 목록")
    val attendees: List<String>? = null,
    
    @JsonPropertyDescription("알림 설정 (분 단위)")
    val reminderMinutes: List<Int>? = null,
    
    @JsonPropertyDescription("반복 규칙")
    val recurrence: String? = null,
    
    @JsonPropertyDescription("종일 이벤트 여부")
    val isAllDay: Boolean = false,
    
    @JsonPropertyDescription("이벤트 상태 (confirmed, tentative, cancelled)")
    val status: String = "confirmed",
    
    @JsonPropertyDescription("생성자")
    val creator: String? = null,
    
    @JsonPropertyDescription("생성 시간")
    val createdAt: String? = null,
    
    @JsonPropertyDescription("수정 시간")
    val updatedAt: String? = null
)

/**
 * 캘린더 이벤트 생성 요청 DTO
 */
data class CreateEventRequest(
    @JsonPropertyDescription("이벤트 제목")
    val title: String,
    
    @JsonPropertyDescription("이벤트 설명")
    val description: String? = null,
    
    @JsonPropertyDescription("시작 시간 (ISO 8601 형식)")
    val startTime: String,
    
    @JsonPropertyDescription("종료 시간 (ISO 8601 형식)")
    val endTime: String,
    
    @JsonPropertyDescription("위치")
    val location: String? = null,
    
    @JsonPropertyDescription("참가자 이메일 목록")
    val attendees: List<String>? = null,
    
    @JsonPropertyDescription("알림 설정 (분 단위)")
    val reminderMinutes: List<Int>? = listOf(15),
    
    @JsonPropertyDescription("반복 규칙 (RRULE 형식)")
    val recurrence: String? = null,
    
    @JsonPropertyDescription("종일 이벤트 여부")
    val isAllDay: Boolean = false
)

/**
 * 캘린더 이벤트 수정 요청 DTO
 */
data class UpdateEventRequest(
    @JsonPropertyDescription("이벤트 ID")
    val eventId: String,
    
    @JsonPropertyDescription("이벤트 제목")
    val title: String? = null,
    
    @JsonPropertyDescription("이벤트 설명")
    val description: String? = null,
    
    @JsonPropertyDescription("시작 시간")
    val startTime: String? = null,
    
    @JsonPropertyDescription("종료 시간")
    val endTime: String? = null,
    
    @JsonPropertyDescription("위치")
    val location: String? = null,
    
    @JsonPropertyDescription("참가자 목록")
    val attendees: List<String>? = null,
    
    @JsonPropertyDescription("알림 설정")
    val reminderMinutes: List<Int>? = null
)


/**
 * 캘린더 응답 DTO (클라이언트용)
 */
data class CalendarResponse(
    @JsonPropertyDescription("일정 목록")
    val events: List<CalendarEvent>,
    
    @JsonPropertyDescription("조회 기간")
    val dateRange: String,
    
    @JsonPropertyDescription("총 일정 수")
    val totalCount: Int,
    
    @JsonPropertyDescription("다음 페이지 토큰")
    val nextPageToken: String? = null
)

/**
 * 시간 슬롯 DTO (가용 시간 검색용)
 */
data class TimeSlot(
    @JsonPropertyDescription("시작 시간")
    val startTime: String,
    
    @JsonPropertyDescription("종료 시간")
    val endTime: String,
    
    @JsonPropertyDescription("가용 여부")
    val isAvailable: Boolean,
    
    @JsonPropertyDescription("충돌하는 이벤트 목록")
    val conflictingEvents: List<String>? = null
)

/**
 * 가용 시간 조회 요청 DTO
 */
data class AvailabilityRequest(
    @JsonPropertyDescription("조회 시작 시간")
    val startTime: String,
    
    @JsonPropertyDescription("조회 종료 시간")
    val endTime: String,
    
    @JsonPropertyDescription("슬롯 길이 (분)")
    val slotDuration: Int = 30,
    
    @JsonPropertyDescription("참가자 목록")
    val attendees: List<String>? = null
)

/**
 * 가용 시간 응답 DTO
 */
data class AvailabilityResponse(
    @JsonPropertyDescription("가용 시간 슬롯 목록")
    val availableSlots: List<TimeSlot>,
    
    @JsonPropertyDescription("사용 중인 시간 슬롯 목록")
    val busySlots: List<TimeSlot>,
    
    @JsonPropertyDescription("조회 기간")
    val searchPeriod: String
)

/**
 * 캘린더 이벤트 생성 요청 (MCP용)
 */
data class CalendarEventRequest(
    @JsonPropertyDescription("이벤트 제목")
    val title: String,
    
    @JsonPropertyDescription("이벤트 날짜 (YYYY-MM-DD)")
    val date: String,
    
    @JsonPropertyDescription("이벤트 시작 시간 (HH:MM)")
    val startTime: String = "09:00",
    
    @JsonPropertyDescription("이벤트 종료 시간 (HH:MM)")
    val endTime: String = "10:00",
    
    @JsonPropertyDescription("이벤트 설명")
    val description: String = "",
    
    @JsonPropertyDescription("캘린더 ID")
    val calendarId: String = "primary"
)

/**
 * 캘린더 이벤트 조회 요청 (MCP용)
 */
data class CalendarEventsRequest(
    @JsonPropertyDescription("조회 시작 날짜 (YYYY-MM-DD)")
    val startDate: String? = null,
    
    @JsonPropertyDescription("조회 종료 날짜 (YYYY-MM-DD)")
    val endDate: String? = null,
    
    @JsonPropertyDescription("최대 조회 개수")
    val maxResults: Int = 10,
    
    @JsonPropertyDescription("캘린더 ID")
    val calendarId: String = "primary"
)

/**
 * 캘린더 이벤트 삭제 요청 (MCP용)
 */
data class CalendarDeleteRequest(
    @JsonPropertyDescription("삭제할 이벤트 ID")
    val eventId: String,
    
    @JsonPropertyDescription("캘린더 ID")
    val calendarId: String = "primary"
)

/**
 * 캘린더 이벤트 결과 (MCP용)
 */
data class CalendarEventResult(
    @JsonPropertyDescription("이벤트 ID")
    val id: String,
    
    @JsonPropertyDescription("이벤트 제목")
    val title: String,
    
    @JsonPropertyDescription("이벤트 날짜")
    val date: String,
    
    @JsonPropertyDescription("시작 시간")
    val startTime: String,
    
    @JsonPropertyDescription("종료 시간")
    val endTime: String,
    
    @JsonPropertyDescription("상태")
    val status: String,
    
    @JsonPropertyDescription("HTML 링크")
    val htmlLink: String? = null,
    
    @JsonPropertyDescription("설명")
    val description: String? = null,
    
    @JsonPropertyDescription("에러 메시지")
    val error: String? = null
)

/**
 * 캘린더 삭제 결과 (MCP용)
 */
data class CalendarDeleteResult(
    @JsonPropertyDescription("이벤트 ID")
    val eventId: String,
    
    @JsonPropertyDescription("삭제 상태")
    val status: String,
    
    @JsonPropertyDescription("에러 메시지")
    val error: String? = null
)

/**
 * 캘린더 정보 (MCP용)
 */
data class CalendarInfo(
    @JsonPropertyDescription("캘린더 ID")
    val id: String,
    
    @JsonPropertyDescription("캘린더 이름")
    val name: String,
    
    @JsonPropertyDescription("설명")
    val description: String,
    
    @JsonPropertyDescription("시간대")
    val timeZone: String
)