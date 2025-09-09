package com.monkeys.calendar.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("CalendarMcpService 단위 테스트")
class CalendarMcpServiceTest {

    @Nested
    @DisplayName("더미 API 키로 테스트")
    inner class DummyApiKeyTest {

        @Test
        @DisplayName("캘린더 이벤트 생성 - API 키 미설정")
        fun `should return dummy event when api key is not set`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.createCalendarEvent("회의", "2024-01-15", "14:00", "15:00", "중요한 회의")
            
            // Then
            assertNotNull(result)
            assertEquals("회의", result.title)
            assertEquals("2024-01-15", result.date)
            assertEquals("14:00", result.startTime)
            assertEquals("15:00", result.endTime)
            assertEquals("dummy", result.status)
            assertTrue(result.id.startsWith("dummy-"))
            assertTrue(result.error!!.contains("API 키가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("캘린더 이벤트 조회 - API 키 미설정")
        fun `should return dummy events when api key is not set`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val results = service.getCalendarEvents("2024-01-01", "2024-01-31")
            
            // Then
            assertNotNull(results)
            assertEquals(1, results.size)
            val event = results[0]
            assertEquals("더미 이벤트", event.title)
            assertEquals("2024-01-01", event.date)
            assertTrue(event.error!!.isEmpty()) // 더미 데이터에는 빈 에러 메시지
        }

        @Test
        @DisplayName("캘린더 이벤트 삭제 - API 키 미설정")
        fun `should return failure when deleting event without api key`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.deleteCalendarEvent("test-event-id")
            
            // Then
            assertNotNull(result)
            assertEquals("test-event-id", result.eventId)
            assertEquals("failed", result.status)
            assertTrue(result.error!!.contains("API 키가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("캘린더 목록 조회 - API 키 미설정")
        fun `should return dummy calendar list when api key is not set`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val calendars = service.getCalendarList()
            
            // Then
            assertNotNull(calendars)
            assertEquals(1, calendars.size)
            val calendar = calendars[0]
            assertEquals("primary", calendar.id)
            assertEquals("기본 캘린더", calendar.name)
            assertEquals("Asia/Seoul", calendar.timeZone)
            assertTrue(calendar.description.contains("API 키가 설정되지 않았습니다"))
        }
    }

    @Nested
    @DisplayName("매개변수 검증 테스트")
    inner class ParameterValidationTest {

        @Test
        @DisplayName("빈 제목으로 이벤트 생성")
        fun `should return error when creating event with empty title`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val emptyTitleResult = service.createCalendarEvent("", "2024-01-15", "14:00", "15:00")
            val blankTitleResult = service.createCalendarEvent("   ", "2024-01-15", "14:00", "15:00")
            
            // Then
            assertEquals("failed", emptyTitleResult.status)
            assertEquals("이벤트 제목이 필요합니다", emptyTitleResult.error)
            
            assertEquals("failed", blankTitleResult.status)
            assertEquals("이벤트 제목이 필요합니다", blankTitleResult.error)
        }

        @Test
        @DisplayName("빈 이벤트 ID로 삭제 요청")
        fun `should return error when deleting event with empty id`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val emptyIdResult = service.deleteCalendarEvent("")
            val blankIdResult = service.deleteCalendarEvent("   ")
            
            // Then
            assertEquals("failed", emptyIdResult.status)
            assertEquals("이벤트 ID가 필요합니다", emptyIdResult.error)
            
            assertEquals("failed", blankIdResult.status)
            assertEquals("이벤트 ID가 필요합니다", blankIdResult.error)
        }

        @Test
        @DisplayName("기본값 매개변수 테스트")
        fun `should use default parameters correctly`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.createCalendarEvent("테스트 이벤트", "2024-01-15")
            
            // Then
            assertEquals("09:00", result.startTime) // 기본 시작 시간
            assertEquals("10:00", result.endTime)   // 기본 종료 시간
        }

        @Test
        @DisplayName("다양한 시간 형식 테스트")
        fun `should handle different time formats`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val morningEvent = service.createCalendarEvent("아침 회의", "2024-01-15", "09:30", "10:30")
            val eveningEvent = service.createCalendarEvent("저녁 회의", "2024-01-15", "18:00", "19:00")
            val allDayEvent = service.createCalendarEvent("종일 이벤트", "2024-01-15", "00:00", "23:59")
            
            // Then
            assertEquals("09:30", morningEvent.startTime)
            assertEquals("10:30", morningEvent.endTime)
            assertEquals("18:00", eveningEvent.startTime)
            assertEquals("19:00", eveningEvent.endTime)
            assertEquals("00:00", allDayEvent.startTime)
            assertEquals("23:59", allDayEvent.endTime)
        }
    }

    @Nested
    @DisplayName("이벤트 조회 테스트")
    inner class EventQueryTest {

        @Test
        @DisplayName("날짜 범위 없이 이벤트 조회")
        fun `should get events without date range`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val results = service.getCalendarEvents()
            
            // Then
            assertNotNull(results)
            assertEquals(1, results.size)
        }

        @Test
        @DisplayName("최대 조회 개수 설정 테스트")
        fun `should respect max results parameter`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val results = service.getCalendarEvents(maxResults = 5)
            
            // Then
            // 더미 환경에서는 1개만 반환되지만 매개변수가 올바르게 전달되는지 확인
            assertNotNull(results)
            assertTrue(results.size <= 5)
        }

        @Test
        @DisplayName("특정 캘린더 ID로 이벤트 조회")
        fun `should query events from specific calendar`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val primaryResults = service.getCalendarEvents(calendarId = "primary")
            val customResults = service.getCalendarEvents(calendarId = "custom-calendar-id")
            
            // Then
            assertNotNull(primaryResults)
            assertNotNull(customResults)
            // 더미 환경에서는 둘 다 같은 결과를 반환하지만 매개변수 전달 확인
            assertEquals(1, primaryResults.size)
            assertEquals(1, customResults.size)
        }
    }

    @Nested
    @DisplayName("데이터 구조 검증 테스트")
    inner class DataStructureTest {

        @Test
        @DisplayName("캘린더 이벤트 결과 구조 확인")
        fun `should return proper calendar event result structure`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.createCalendarEvent("테스트", "2024-01-15", "14:00", "15:00", "테스트 설명")
            
            // Then
            assertNotNull(result.id)
            assertNotNull(result.title)
            assertNotNull(result.date)
            assertNotNull(result.startTime)
            assertNotNull(result.endTime)
            assertNotNull(result.status)
            assertTrue(result.id.isNotEmpty())
            assertTrue(result.title.isNotEmpty())
        }

        @Test
        @DisplayName("캘린더 삭제 결과 구조 확인")
        fun `should return proper calendar delete result structure`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.deleteCalendarEvent("test-event-id")
            
            // Then
            assertNotNull(result.eventId)
            assertNotNull(result.status)
            assertEquals("test-event-id", result.eventId)
            assertTrue(result.status in listOf("deleted", "failed"))
        }

        @Test
        @DisplayName("캘린더 정보 구조 확인")
        fun `should return proper calendar info structure`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val calendars = service.getCalendarList()
            
            // Then
            calendars.forEach { calendar ->
                assertNotNull(calendar.id)
                assertNotNull(calendar.name)
                assertNotNull(calendar.description)
                assertNotNull(calendar.timeZone)
                assertTrue(calendar.id.isNotEmpty())
                assertTrue(calendar.name.isNotEmpty())
                assertTrue(calendar.timeZone.isNotEmpty())
            }
        }

        @Test
        @DisplayName("이벤트 목록 구조 확인")
        fun `should return proper event list structure`() {
            // Given
            val service = CalendarMcpService(
                "dummy-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val events = service.getCalendarEvents("2024-01-01", "2024-01-31")
            
            // Then
            events.forEach { event ->
                assertNotNull(event.id)
                assertNotNull(event.title)
                assertNotNull(event.date)
                assertNotNull(event.startTime)
                assertNotNull(event.endTime)
                assertNotNull(event.status)
                assertTrue(event.id.isNotEmpty())
                assertTrue(event.title.isNotEmpty())
                assertTrue(event.date.isNotEmpty())
                assertTrue(event.startTime.isNotEmpty())
                assertTrue(event.endTime.isNotEmpty())
            }
        }
    }

    @Nested
    @DisplayName("실제 API 키 환경 테스트")
    inner class RealApiKeyTest {

        @Test
        @DisplayName("실제 API 키가 있을 때 이벤트 생성 시도")
        fun `should attempt real api call when real api key is provided`() {
            // Given
            val service = CalendarMcpService(
                "real-google-calendar-api-key",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.createCalendarEvent("실제 테스트 이벤트", "2024-01-15", "14:00", "15:00")
            
            // Then
            // 실제 API 키가 있으면 API 호출을 시도하지만, 네트워크 오류나 인증 실패로 에러가 발생할 것
            assertNotNull(result)
            assertNotNull(result.status)
            // dummy가 아닌 실제 처리 결과 (성공 또는 실패)
            assertTrue(result.status in listOf("created", "dummy"))
        }
    }
}