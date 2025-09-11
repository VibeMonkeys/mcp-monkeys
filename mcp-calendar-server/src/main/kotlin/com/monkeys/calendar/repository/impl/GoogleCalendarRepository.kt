package com.monkeys.calendar.repository.impl

import com.monkeys.calendar.repository.CalendarRepository
import com.monkeys.shared.dto.*
import com.monkeys.shared.util.ApiClient
import com.monkeys.shared.util.ApiException
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Google Calendar API를 통한 캘린더 데이터 조회 구현체
 * Repository 패턴 - 외부 API와의 데이터 액세스 담당
 * 공통 ApiClient 사용으로 HTTP 처리 표준화
 */
@Repository
class GoogleCalendarRepository(
    @Value("\${google.calendar.api.key:dummy-key}") private val apiKey: String,
    calendarHttpClient: OkHttpClient,
    meterRegistry: MeterRegistry
) : CalendarRepository {
    
    private val logger = LoggerFactory.getLogger(GoogleCalendarRepository::class.java)
    private val baseUrl = "https://www.googleapis.com/calendar/v3"
    
    private val apiClient = ApiClient(calendarHttpClient, meterRegistry, "calendar")
    
    override suspend fun createEvent(
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        calendarId: String
    ): CalendarEventResult = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val startDateTime = "${date}T${startTime}:00"
        val endDateTime = "${date}T${endTime}:00"
        
        val eventPayload = mapOf(
            "summary" to title,
            "description" to description,
            "start" to mapOf(
                "dateTime" to startDateTime,
                "timeZone" to "Asia/Seoul"
            ),
            "end" to mapOf(
                "dateTime" to endDateTime,
                "timeZone" to "Asia/Seoul"
            )
        )
        
        val url = "$baseUrl/calendars/$calendarId/events?key=$apiKey"
        val eventData = apiClient.post(url, eventPayload).getDataOrThrow()
        
        mapToCalendarEventResult(eventData, title, date, startTime, endTime, "created", description)
    }
    
    override suspend fun getEvents(
        startDate: String?,
        endDate: String?,
        maxResults: Int,
        calendarId: String
    ): List<CalendarEventResult> = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val urlBuilder = StringBuilder("$baseUrl/calendars/$calendarId/events?key=$apiKey")
        urlBuilder.append("&maxResults=$maxResults")
        urlBuilder.append("&singleEvents=true")
        urlBuilder.append("&orderBy=startTime")
        
        startDate?.let { urlBuilder.append("&timeMin=${it}T00:00:00Z") }
        endDate?.let { urlBuilder.append("&timeMax=${it}T23:59:59Z") }
        
        val eventsData = apiClient.get(urlBuilder.toString()).getDataOrThrow()
        mapToCalendarEventList(eventsData)
    }
    
    override suspend fun deleteEvent(eventId: String, calendarId: String): CalendarDeleteResult = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val url = "$baseUrl/calendars/$calendarId/events/$eventId?key=$apiKey"
        apiClient.delete(url).getDataOrThrow()
        
        CalendarDeleteResult(
            eventId = eventId,
            status = "deleted"
        )
    }
    
    override suspend fun getCalendarList(): List<CalendarInfo> = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val url = "$baseUrl/users/me/calendarList?key=$apiKey"
        val calendarData = apiClient.get(url).getDataOrThrow()
        
        mapToCalendarInfoList(calendarData)
    }
    
    override suspend fun checkApiHealth(): Boolean = withContext(Dispatchers.IO) {
        if (apiKey == "dummy-key") return@withContext false
        
        try {
            val url = "$baseUrl/users/me/calendarList?key=$apiKey&maxResults=1"
            apiClient.get(url).getDataOrThrow()
            true
        } catch (e: Exception) {
            logger.error("Calendar API health check failed", e)
            false
        }
    }
    
    private fun validateApiKey() {
        if (apiKey == "dummy-key") {
            throw ApiException("API 키가 설정되지 않음", "MISSING_API_KEY")
        }
    }
    
    private fun mapToCalendarEventResult(
        eventData: Map<String, Any>,
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        status: String,
        description: String? = null
    ): CalendarEventResult {
        return CalendarEventResult(
            id = eventData["id"] as? String ?: "",
            title = title,
            date = date,
            startTime = startTime,
            endTime = endTime,
            status = status,
            htmlLink = eventData["htmlLink"] as? String,
            description = description
        )
    }
    
    private fun mapToCalendarEventList(eventsData: Map<String, Any>): List<CalendarEventResult> {
        val items = eventsData["items"] as? List<Map<String, Any>> ?: emptyList()
        
        return items.map { event ->
            val start = event["start"] as? Map<String, Any>
            val end = event["end"] as? Map<String, Any>
            
            CalendarEventResult(
                id = event["id"] as? String ?: "",
                title = event["summary"] as? String ?: "제목 없음",
                date = extractDate(start),
                startTime = extractTime(start),
                endTime = extractTime(end),
                status = "existing",
                htmlLink = event["htmlLink"] as? String,
                description = event["description"] as? String ?: ""
            )
        }
    }
    
    private fun mapToCalendarInfoList(calendarData: Map<String, Any>): List<CalendarInfo> {
        val items = calendarData["items"] as? List<Map<String, Any>> ?: emptyList()
        
        return items.map { calendar ->
            CalendarInfo(
                id = calendar["id"] as? String ?: "",
                name = calendar["summary"] as? String ?: "이름 없음",
                description = calendar["description"] as? String ?: "",
                timeZone = calendar["timeZone"] as? String ?: "Asia/Seoul"
            )
        }
    }
    
    private fun createErrorCalendarEvent(title: String, date: String, startTime: String, endTime: String, error: String): CalendarEventResult {
        return CalendarEventResult(
            id = "error-${System.currentTimeMillis()}",
            title = title,
            date = date,
            startTime = startTime,
            endTime = endTime,
            status = "error",
            description = "오류 발생",
            error = error
        )
    }
    
    private fun extractDate(timeInfo: Map<String, Any>?): String {
        return when {
            timeInfo?.containsKey("date") == true -> timeInfo["date"] as String
            timeInfo?.containsKey("dateTime") == true -> {
                val dateTime = timeInfo["dateTime"] as String
                dateTime.substringBefore("T")
            }
            else -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
    }
    
    private fun extractTime(timeInfo: Map<String, Any>?): String {
        return when {
            timeInfo?.containsKey("dateTime") == true -> {
                val dateTime = timeInfo["dateTime"] as String
                val time = dateTime.substringAfter("T").substringBefore(":")
                val minute = dateTime.substringAfter("T").substringAfter(":").substringBefore(":")
                "$time:$minute"
            }
            else -> "09:00"
        }
    }
}