package com.monkeys.calendar.service

import com.monkeys.shared.dto.*
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("CalendarMcpService 단위 테스트")
class CalendarMcpServiceTest {

    private lateinit var calendarService: CalendarService
    private lateinit var calendarMcpService: CalendarMcpService

    @BeforeEach
    fun setUp() {
        calendarService = mockk()
        calendarMcpService = CalendarMcpService(calendarService)
    }

    @Nested
    @DisplayName("캘린더 이벤트 생성 테스트")
    inner class CreateEventTest {

        @Test
        @DisplayName("캘린더 이벤트 생성 성공")
        fun `should create calendar event successfully`() {
            // Given
            val expectedResult = CalendarEventResult(
                id = "event-123",
                title = "회의",
                date = "2024-01-15",
                startTime = "14:00",
                endTime = "15:00",
                description = "중요한 회의",
                status = "created",
                error = null
            )
            coEvery { calendarService.createEvent(any()) } returns expectedResult

            // When
            val result = calendarMcpService.createCalendarEvent(
                "회의", "2024-01-15", "14:00", "15:00", "중요한 회의"
            )

            // Then
            assertNotNull(result)
            assertEquals("회의", result.title)
            assertEquals("2024-01-15", result.date)
            assertEquals("14:00", result.startTime)
            assertEquals("15:00", result.endTime)
            assertEquals("created", result.status)
            coVerify { calendarService.createEvent(any()) }
        }

        @Test
        @DisplayName("기본 시간으로 이벤트 생성")
        fun `should use default times when not specified`() {
            // Given
            val expectedResult = CalendarEventResult(
                id = "event-456",
                title = "테스트 이벤트",
                date = "2024-01-15",
                startTime = "09:00",
                endTime = "10:00",
                description = "",
                status = "created",
                error = null
            )
            coEvery { calendarService.createEvent(any()) } returns expectedResult

            // When
            val result = calendarMcpService.createCalendarEvent("테스트 이벤트", "2024-01-15")

            // Then
            assertNotNull(result)
            assertEquals("created", result.status)
        }
    }

    @Nested
    @DisplayName("캘린더 이벤트 조회 테스트")
    inner class GetEventsTest {

        @Test
        @DisplayName("캘린더 이벤트 목록 조회 성공")
        fun `should get calendar events successfully`() {
            // Given
            val expectedEvents = listOf(
                CalendarEventResult(
                    id = "event-1",
                    title = "회의 1",
                    date = "2024-01-15",
                    startTime = "10:00",
                    endTime = "11:00",
                    description = "첫 번째 회의",
                    status = "active",
                    error = null
                ),
                CalendarEventResult(
                    id = "event-2",
                    title = "회의 2",
                    date = "2024-01-16",
                    startTime = "14:00",
                    endTime = "15:00",
                    description = "두 번째 회의",
                    status = "active",
                    error = null
                )
            )
            coEvery { calendarService.getEvents(any()) } returns expectedEvents

            // When
            val result = calendarMcpService.getCalendarEvents("2024-01-01", "2024-01-31", 10)

            // Then
            assertNotNull(result)
            assertEquals(2, result.size)
            assertEquals("회의 1", result[0].title)
            assertEquals("회의 2", result[1].title)
            coVerify { calendarService.getEvents(any()) }
        }
    }

    @Nested
    @DisplayName("캘린더 이벤트 삭제 테스트")
    inner class DeleteEventTest {

        @Test
        @DisplayName("캘린더 이벤트 삭제 성공")
        fun `should delete calendar event successfully`() {
            // Given
            val expectedResult = CalendarDeleteResult(
                eventId = "event-123",
                status = "deleted",
                error = null
            )
            coEvery { calendarService.deleteEvent(any()) } returns expectedResult

            // When
            val result = calendarMcpService.deleteCalendarEvent("event-123")

            // Then
            assertNotNull(result)
            assertEquals("event-123", result.eventId)
            assertEquals("deleted", result.status)
            coVerify { calendarService.deleteEvent(any()) }
        }
    }

    @Nested
    @DisplayName("캘린더 목록 조회 테스트")
    inner class GetCalendarListTest {

        @Test
        @DisplayName("캘린더 목록 조회 성공")
        fun `should get calendar list successfully`() {
            // Given
            val expectedCalendars = listOf(
                CalendarInfo(
                    id = "primary",
                    name = "기본 캘린더",
                    description = "기본 캘린더입니다",
                    timeZone = "Asia/Seoul"
                ),
                CalendarInfo(
                    id = "work",
                    name = "업무 캘린더",
                    description = "업무용 캘린더",
                    timeZone = "Asia/Seoul"
                )
            )
            coEvery { calendarService.getCalendarList() } returns expectedCalendars

            // When
            val result = calendarMcpService.getCalendarList()

            // Then
            assertNotNull(result)
            assertEquals(2, result.size)
            assertEquals("primary", result[0].id)
            assertEquals("기본 캘린더", result[0].name)
            coVerify { calendarService.getCalendarList() }
        }
    }

    @Nested
    @DisplayName("데이터 구조 검증")
    inner class DataStructureTest {

        @Test
        @DisplayName("CalendarEventResult 데이터 구조")
        fun `should have correct CalendarEventResult structure`() {
            // Given
            val event = CalendarEventResult(
                id = "event-test",
                title = "테스트 이벤트",
                date = "2024-01-15",
                startTime = "14:00",
                endTime = "15:00",
                description = "테스트 설명",
                status = "active",
                error = null
            )

            // Then
            assertEquals("event-test", event.id)
            assertEquals("테스트 이벤트", event.title)
            assertEquals("2024-01-15", event.date)
            assertEquals("14:00", event.startTime)
            assertEquals("15:00", event.endTime)
            assertEquals("테스트 설명", event.description)
            assertEquals("active", event.status)
        }

        @Test
        @DisplayName("CalendarInfo 데이터 구조")
        fun `should have correct CalendarInfo structure`() {
            // Given
            val calendarInfo = CalendarInfo(
                id = "test-calendar",
                name = "테스트 캘린더",
                description = "테스트용 캘린더입니다",
                timeZone = "Asia/Seoul"
            )

            // Then
            assertEquals("test-calendar", calendarInfo.id)
            assertEquals("테스트 캘린더", calendarInfo.name)
            assertEquals("테스트용 캘린더입니다", calendarInfo.description)
            assertEquals("Asia/Seoul", calendarInfo.timeZone)
        }

        @Test
        @DisplayName("CalendarDeleteResult 데이터 구조")
        fun `should have correct CalendarDeleteResult structure`() {
            // Given
            val deleteResult = CalendarDeleteResult(
                eventId = "event-to-delete",
                status = "deleted",
                error = null
            )

            // Then
            assertEquals("event-to-delete", deleteResult.eventId)
            assertEquals("deleted", deleteResult.status)
            assertEquals(null, deleteResult.error)
        }
    }
}
