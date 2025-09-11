package com.monkeys.calendar.service

import com.monkeys.calendar.repository.CalendarRepository
import com.monkeys.shared.dto.*
import com.monkeys.shared.util.RetryHandler
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry

/**
 * Calendar 비즈니스 로직 처리 서비스
 * Repository 계층과 MCP 계층 사이의 비즈니스 로직 담당
 * 공통 RetryHandler 사용으로 재시도 로직 표준화
 */
@Service
class CalendarService(
    private val calendarRepository: CalendarRepository,
    meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(CalendarService::class.java)
    private val retryHandler = RetryHandler(
        maxRetries = 3,
        baseDelayMs = 1000,
        meterRegistry = meterRegistry,
        serviceName = "calendar"
    )

    suspend fun createEvent(request: CalendarEventRequest): CalendarEventResult {
        logger.info("캘린더 이벤트 생성 요청: title={}, date={}", request.title, request.date)
        
        return retryHandler.executeWithRetryAsync(
            operation = {
                calendarRepository.createEvent(
                    title = request.title,
                    date = request.date,
                    startTime = request.startTime,
                    endTime = request.endTime,
                    description = request.description,
                    calendarId = request.calendarId
                )
            }
        )
    }

    suspend fun getEvents(request: CalendarEventsRequest): List<CalendarEventResult> {
        logger.info("캘린더 이벤트 조회 요청: startDate={}, endDate={}, maxResults={}", 
            request.startDate, request.endDate, request.maxResults)
        
        return retryHandler.executeWithRetryAsync(
            operation = {
                calendarRepository.getEvents(
                    startDate = request.startDate,
                    endDate = request.endDate,
                    maxResults = request.maxResults,
                    calendarId = request.calendarId
                )
            }
        )
    }

    suspend fun deleteEvent(request: CalendarDeleteRequest): CalendarDeleteResult {
        logger.info("캘린더 이벤트 삭제 요청: eventId={}", request.eventId)
        
        return retryHandler.executeWithRetryAsync(
            operation = {
                calendarRepository.deleteEvent(
                    eventId = request.eventId,
                    calendarId = request.calendarId
                )
            }
        )
    }

    suspend fun getCalendarList(): List<CalendarInfo> {
        logger.info("캘린더 목록 조회 요청")
        
        return retryHandler.executeWithRetryAsync(
            operation = {
                calendarRepository.getCalendarList()
            }
        )
    }

    suspend fun checkApiHealth(): Boolean {
        return try {
            calendarRepository.checkApiHealth()
        } catch (e: Exception) {
            logger.error("Calendar API 상태 확인 실패", e)
            false
        }
    }
}