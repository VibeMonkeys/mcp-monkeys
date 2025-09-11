package com.monkeys.calendar.repository

import com.monkeys.shared.dto.*

interface CalendarRepository {
    suspend fun createEvent(
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        calendarId: String
    ): CalendarEventResult

    suspend fun getEvents(
        startDate: String?,
        endDate: String?,
        maxResults: Int,
        calendarId: String
    ): List<CalendarEventResult>

    suspend fun deleteEvent(eventId: String, calendarId: String): CalendarDeleteResult

    suspend fun getCalendarList(): List<CalendarInfo>

    suspend fun checkApiHealth(): Boolean
}