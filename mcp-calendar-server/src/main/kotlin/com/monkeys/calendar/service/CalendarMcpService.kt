package com.monkeys.calendar.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

@Service
class CalendarMcpService(
    @Value("\${google.calendar.api.key:dummy-key}") private val googleApiKey: String,
    private val calendarHttpClient: OkHttpClient,
    private val meterRegistry: MeterRegistry
) {
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(CalendarMcpService::class.java)
    private val maxRetries = 3
    
    // 메트릭 타이머
    private val calendarApiTimer = Timer.builder("calendar.api.request")
        .description("Google Calendar API 요청 시간")
        .register(meterRegistry)

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
        if (title.isBlank()) {
            return CalendarEventResult(
                id = "",
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = "failed",
                error = "이벤트 제목이 필요합니다"
            )
        }

        if (googleApiKey == "dummy-key") {
            logger.warn("Google Calendar API 키가 설정되지 않음")
            return createDummyCalendarEvent(title, date, startTime, endTime, description, "API 키가 설정되지 않았습니다")
        }

        logger.info("캘린더 이벤트 생성 요청: title={}, date={}, startTime={}, endTime={}, calendarId={}", 
            title, date, startTime, endTime, calendarId)

        return try {
            val eventData = executeWithRetry {
                createCalendarEventViaApi(title, date, startTime, endTime, description, calendarId)
            }

            CalendarEventResult(
                id = eventData["id"] as String,
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = "created",
                htmlLink = eventData["htmlLink"] as? String,
                description = description
            )
        } catch (e: CalendarApiException) {
            logger.error("Google Calendar API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("calendar.api.error", "type", e.errorCode).increment()
            createDummyCalendarEvent(title, date, startTime, endTime, description, "캘린더 이벤트 생성 실패: ${e.message}")
        } catch (e: Exception) {
            logger.error("예상치 못한 캘린더 오류", e)
            meterRegistry.counter("calendar.api.error", "type", "UNEXPECTED").increment()
            createDummyCalendarEvent(title, date, startTime, endTime, description, "일시적 오류가 발생했습니다")
        }
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
        if (googleApiKey == "dummy-key") {
            return listOf(createDummyCalendarEvent("더미 이벤트", startDate ?: "2024-01-01", "09:00", "10:00", "API 키가 설정되지 않았습니다", ""))
        }

        return try {
            val eventsData = executeWithRetry {
                getCalendarEventsViaApi(startDate, endDate, maxResults, calendarId)
            }

            val items = eventsData["items"] as? List<Map<String, Any>> ?: emptyList()
            
            items.map { event ->
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
        } catch (e: CalendarApiException) {
            logger.error("Calendar Events API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("calendar.events.error", "type", e.errorCode).increment()
            listOf(createDummyCalendarEvent("이벤트 조회 실패", startDate ?: "2024-01-01", "09:00", "10:00", "", "이벤트 조회 실패: ${e.message}"))
        } catch (e: Exception) {
            logger.error("예상치 못한 이벤트 조회 오류", e)
            meterRegistry.counter("calendar.events.error", "type", "UNEXPECTED").increment()
            listOf(createDummyCalendarEvent("일시적 오류", startDate ?: "2024-01-01", "09:00", "10:00", "", "일시적 오류가 발생했습니다"))
        }
    }

    @Tool(description = "캘린더 이벤트를 삭제합니다")
    fun deleteCalendarEvent(
        @ToolParam(description = "삭제할 이벤트 ID", required = true)
        eventId: String,
        @ToolParam(description = "캘린더 ID (기본: primary)")
        calendarId: String = "primary"
    ): CalendarDeleteResult {
        if (eventId.isBlank()) {
            return CalendarDeleteResult(
                eventId = eventId,
                status = "failed",
                error = "이벤트 ID가 필요합니다"
            )
        }

        if (googleApiKey == "dummy-key") {
            return CalendarDeleteResult(
                eventId = eventId,
                status = "failed",
                error = "API 키가 설정되지 않았습니다"
            )
        }

        return try {
            executeWithRetry {
                deleteCalendarEventViaApi(eventId, calendarId)
            }

            CalendarDeleteResult(
                eventId = eventId,
                status = "deleted"
            )
        } catch (e: CalendarApiException) {
            logger.error("Calendar Delete API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("calendar.delete.error", "type", e.errorCode).increment()
            CalendarDeleteResult(
                eventId = eventId,
                status = "failed",
                error = "이벤트 삭제 실패: ${e.message}"
            )
        } catch (e: Exception) {
            logger.error("예상치 못한 이벤트 삭제 오류", e)
            meterRegistry.counter("calendar.delete.error", "type", "UNEXPECTED").increment()
            CalendarDeleteResult(
                eventId = eventId,
                status = "failed",
                error = "일시적 오류가 발생했습니다"
            )
        }
    }

    @Tool(description = "사용 가능한 캘린더 목록을 조회합니다")
    fun getCalendarList(): List<CalendarInfo> {
        if (googleApiKey == "dummy-key") {
            return listOf(
                CalendarInfo(
                    id = "primary",
                    name = "기본 캘린더",
                    description = "API 키가 설정되지 않았습니다",
                    timeZone = "Asia/Seoul"
                )
            )
        }

        return try {
            val calendarData = executeWithRetry {
                getCalendarListViaApi()
            }

            val items = calendarData["items"] as? List<Map<String, Any>> ?: emptyList()
            
            items.map { calendar ->
                CalendarInfo(
                    id = calendar["id"] as? String ?: "",
                    name = calendar["summary"] as? String ?: "이름 없음",
                    description = calendar["description"] as? String ?: "",
                    timeZone = calendar["timeZone"] as? String ?: "Asia/Seoul"
                )
            }
        } catch (e: Exception) {
            logger.error("캘린더 목록 조회 오류", e)
            listOf(
                CalendarInfo(
                    id = "primary",
                    name = "오류 발생",
                    description = "캘린더 목록 조회 실패: ${e.message}",
                    timeZone = "Asia/Seoul"
                )
            )
        }
    }

    // Google Calendar API 호출 메소드들
    private fun createCalendarEventViaApi(
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        calendarId: String
    ): Map<String, Any> {
        val url = "https://www.googleapis.com/calendar/v3/calendars/$calendarId/events?key=$googleApiKey"
        
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

        return fetchCalendarData(url, eventPayload, "POST")
    }

    private fun getCalendarEventsViaApi(
        startDate: String?,
        endDate: String?,
        maxResults: Int,
        calendarId: String
    ): Map<String, Any> {
        val urlBuilder = StringBuilder("https://www.googleapis.com/calendar/v3/calendars/$calendarId/events?key=$googleApiKey")
        urlBuilder.append("&maxResults=$maxResults")
        urlBuilder.append("&singleEvents=true")
        urlBuilder.append("&orderBy=startTime")
        
        startDate?.let { urlBuilder.append("&timeMin=${it}T00:00:00Z") }
        endDate?.let { urlBuilder.append("&timeMax=${it}T23:59:59Z") }

        return fetchCalendarData(urlBuilder.toString(), emptyMap(), "GET")
    }

    private fun deleteCalendarEventViaApi(eventId: String, calendarId: String) {
        val url = "https://www.googleapis.com/calendar/v3/calendars/$calendarId/events/$eventId?key=$googleApiKey"
        fetchCalendarData(url, emptyMap(), "DELETE")
    }

    private fun getCalendarListViaApi(): Map<String, Any> {
        val url = "https://www.googleapis.com/calendar/v3/users/me/calendarList?key=$googleApiKey"
        return fetchCalendarData(url, emptyMap(), "GET")
    }

    // HTTP 요청 실행 및 JSON 파싱 (메트릭 포함)
    private fun fetchCalendarData(
        url: String, 
        requestBody: Map<String, Any>, 
        method: String
    ): Map<String, Any> {
        val request = when (method) {
            "GET" -> Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MCP-Monkeys-Calendar/1.0")
                .build()
            "POST" -> {
                val jsonBody = mapper.writeValueAsString(requestBody)
                Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "MCP-Monkeys-Calendar/1.0")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()
            }
            "DELETE" -> Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MCP-Monkeys-Calendar/1.0")
                .delete()
                .build()
            else -> throw IllegalArgumentException("지원하지 않는 HTTP 메소드: $method")
        }

        return calendarApiTimer.recordCallable {
            calendarHttpClient.newCall(request).execute().use { response ->
                when (response.code) {
                    200, 201 -> {
                        val jsonResponse = response.body?.string() ?: "{}"
                        meterRegistry.counter("calendar.api.success").increment()
                        if (jsonResponse.isEmpty() || jsonResponse == "{}") {
                            emptyMap()
                        } else {
                            mapper.readValue(jsonResponse)
                        }
                    }
                    204 -> {
                        meterRegistry.counter("calendar.api.success").increment()
                        emptyMap() // DELETE 성공 시 빈 응답
                    }
                    400 -> {
                        meterRegistry.counter("calendar.api.error", "type", "bad_request").increment()
                        throw CalendarApiException("잘못된 요청", "BAD_REQUEST")
                    }
                    401, 403 -> {
                        meterRegistry.counter("calendar.api.error", "type", "auth").increment()
                        throw CalendarApiException("인증 실패", "INVALID_API_KEY")
                    }
                    404 -> {
                        meterRegistry.counter("calendar.api.error", "type", "not_found").increment()
                        throw CalendarApiException("리소스를 찾을 수 없습니다", "NOT_FOUND")
                    }
                    429 -> {
                        meterRegistry.counter("calendar.api.error", "type", "rate_limit").increment()
                        throw CalendarApiException("API 사용량 한도 초과", "RATE_LIMIT_EXCEEDED")
                    }
                    500, 502, 503, 504 -> {
                        meterRegistry.counter("calendar.api.error", "type", "server_error").increment()
                        throw CalendarApiException("서버 오류", "SERVER_ERROR")
                    }
                    else -> {
                        meterRegistry.counter("calendar.api.error", "type", "unknown").increment()
                        throw CalendarApiException("알 수 없는 오류", "UNKNOWN_ERROR")
                    }
                }
            }
        } ?: throw CalendarApiException("응답 파싱 실패", "PARSE_ERROR")
    }

    // 재시도 로직을 포함한 API 호출
    private fun <T> executeWithRetry(operation: () -> T): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logger.warn("API 호출 시도 ${attempt + 1}/$maxRetries 실패: ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    // 지수 백오프: 1초, 2초, 4초
                    val delay = (1000 * Math.pow(2.0, attempt.toDouble())).toLong()
                    Thread.sleep(delay)
                }
            }
        }
        
        throw lastException ?: RuntimeException("API 호출이 $maxRetries 번 모두 실패했습니다")
    }

    // 유틸리티 메소드들
    private fun createDummyCalendarEvent(
        title: String, 
        date: String, 
        startTime: String, 
        endTime: String, 
        description: String, 
        error: String
    ): CalendarEventResult {
        return CalendarEventResult(
            id = "dummy-${System.currentTimeMillis()}",
            title = title,
            date = date,
            startTime = startTime,
            endTime = endTime,
            status = "dummy",
            description = "테스트 이벤트: $description",
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

// 예외 클래스
class CalendarApiException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

// 데이터 클래스들
data class CalendarEventResult(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val htmlLink: String? = null,
    val description: String? = null,
    val error: String? = null
)

data class CalendarDeleteResult(
    val eventId: String,
    val status: String,
    val error: String? = null
)

data class CalendarInfo(
    val id: String,
    val name: String,
    val description: String,
    val timeZone: String
)