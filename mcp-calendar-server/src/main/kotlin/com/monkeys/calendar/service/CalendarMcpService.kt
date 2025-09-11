package com.monkeys.calendar.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import kotlinx.coroutines.runBlocking

/**
 * Calendar MCP Tool Provider
 * MCP 프로토콜용 Tool 어노테이션만 담당 - 얇은 어댑터 계층
 * 예외 처리는 GlobalExceptionHandler가 담당
 */
@Service
class CalendarMcpService(
    private val calendarService: CalendarService
) {
    private val logger = LoggerFactory.getLogger(CalendarMcpService::class.java)

    @Tool(description = "캘린더 이벤트를 생성합니다")
    fun createCalendarEvent(
        @ToolParam(description = "이벤트 제목", required = true)
        title: String,
        @ToolParam(description = "이벤트 날짜 (YYYY-MM-DD)")
        date: String,
        @ToolParam(description = "이벤트 시작 시간 (HH:MM)")
        startTime: String = "09:00",
        @ToolParam(description = "이벤트 종료 시간 (HH:MM)")
        endTime: String = "10:00",
        @ToolParam(description = "이벤트 설명")
        description: String = "",
        @ToolParam(description = "캘린더 ID (기본: primary)")
        calendarId: String = "primary"
    ): CalendarEventResult {
        logger.info("MCP Tool 호출: createCalendarEvent - title={}, date={}", title, date)
        
        val request = CalendarEventRequest(
            title = title,
            date = date,
            startTime = startTime,
            endTime = endTime,
            description = description,
            calendarId = calendarId
        )
        
        return runBlocking { calendarService.createEvent(request) }
    }

    @Tool(description = "캘린더 이벤트 목록을 조회합니다")
    fun getCalendarEvents(
        @ToolParam(description = "조회 시작 날짜 (YYYY-MM-DD)")
        startDate: String? = null,
        @ToolParam(description = "조회 종료 날짜 (YYYY-MM-DD)")
        endDate: String? = null,
        @ToolParam(description = "최대 조회 개수")
        maxResults: Int = 10,
        @ToolParam(description = "캘린더 ID (기본: primary)")
        calendarId: String = "primary"
    ): List<CalendarEventResult> {
        logger.info("MCP Tool 호출: getCalendarEvents - startDate={}, endDate={}, maxResults={}", 
            startDate, endDate, maxResults)
        
        val request = CalendarEventsRequest(
            startDate = startDate,
            endDate = endDate,
            maxResults = maxResults,
            calendarId = calendarId
        )
        
        return runBlocking { calendarService.getEvents(request) }
    }

    @Tool(description = "캘린더 이벤트를 삭제합니다")
    fun deleteCalendarEvent(
        @ToolParam(description = "삭제할 이벤트 ID", required = true)
        eventId: String,
        @ToolParam(description = "캘린더 ID (기본: primary)")
        calendarId: String = "primary"
    ): CalendarDeleteResult {
        logger.info("MCP Tool 호출: deleteCalendarEvent - eventId={}", eventId)
        
        val request = CalendarDeleteRequest(
            eventId = eventId,
            calendarId = calendarId
        )
        
        return runBlocking { calendarService.deleteEvent(request) }
    }

    @Tool(description = "사용 가능한 캘린더 목록을 조회합니다")
    fun getCalendarList(): List<CalendarInfo> {
        logger.info("MCP Tool 호출: getCalendarList")
        
        return runBlocking { calendarService.getCalendarList() }
    }
}