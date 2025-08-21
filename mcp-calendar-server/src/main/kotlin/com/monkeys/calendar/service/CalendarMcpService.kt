package com.monkeys.calendar.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class CalendarMcpService {

    @Tool(description = "캘린더 이벤트를 생성합니다 (개발 예정)")
    fun createCalendarEvent(
        @ToolParam(description = "이벤트 제목", required = true)
        title: String,
        @ToolParam(description = "이벤트 날짜")
        date: String,
        @ToolParam(description = "이벤트 시간")
        time: String = "09:00"
    ): String {
        return "캘린더 서비스가 개발 중입니다. 제목: $title, 날짜: $date"
    }
}