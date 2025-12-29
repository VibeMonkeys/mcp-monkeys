package com.monkeys.weather.service

import com.monkeys.shared.dto.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("WeatherMcpService 단위 테스트")
class WeatherMcpServiceTest {

    private lateinit var weatherService: WeatherService
    private lateinit var weatherMcpService: WeatherMcpService

    @BeforeEach
    fun setUp() {
        weatherService = mockk()
        weatherMcpService = WeatherMcpService(weatherService)
    }

    @Nested
    @DisplayName("현재 날씨 조회 테스트")
    inner class GetCurrentWeatherTest {

        @Test
        @DisplayName("현재 날씨 조회 성공")
        fun `should return current weather successfully`() {
            // Given
            val expectedWeather = WeatherInfo(
                city = "Seoul",
                country = "KR",
                temperature = 20.0,
                feelsLike = 22.0,
                humidity = 60,
                pressure = 1013,
                description = "맑음",
                main = "Clear",
                windSpeed = 3.5,
                windDirection = 180,
                visibility = 10000,
                units = "metric"
            )
            every { weatherService.getCurrentWeather(any()) } returns expectedWeather

            // When
            val result = weatherMcpService.getCurrentWeather("Seoul", "metric")

            // Then
            assertNotNull(result)
            assertEquals("Seoul", result.city)
            assertEquals("KR", result.country)
            assertEquals(20.0, result.temperature)
            verify { weatherService.getCurrentWeather(any()) }
        }

        @Test
        @DisplayName("다른 단위로 날씨 조회")
        fun `should handle different units`() {
            // Given
            val expectedWeather = WeatherInfo(
                city = "Seoul",
                country = "KR",
                temperature = 68.0,
                feelsLike = 71.6,
                humidity = 60,
                pressure = 1013,
                description = "Clear sky",
                main = "Clear",
                windSpeed = 7.8,
                windDirection = 180,
                visibility = 10000,
                units = "imperial"
            )
            every { weatherService.getCurrentWeather(any()) } returns expectedWeather

            // When
            val result = weatherMcpService.getCurrentWeather("Seoul", "imperial")

            // Then
            assertEquals("imperial", result.units)
        }
    }

    @Nested
    @DisplayName("날씨 예보 조회 테스트")
    inner class GetWeatherForecastTest {

        @Test
        @DisplayName("날씨 예보 조회 성공")
        fun `should return weather forecast successfully`() {
            // Given
            val expectedForecast = listOf(
                WeatherForecast(
                    dateTime = "2024-01-01 12:00:00",
                    temperature = 20.0,
                    feelsLike = 22.0,
                    humidity = 60,
                    pressure = 1013,
                    description = "맑음",
                    main = "Clear",
                    windSpeed = 3.5,
                    windDirection = 180,
                    precipitationProbability = 0.0,
                    city = "Tokyo",
                    units = "metric"
                )
            )
            every { weatherService.getWeatherForecast(any()) } returns expectedForecast

            // When
            val result = weatherMcpService.getWeatherForecast("Tokyo", "metric", 8)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("Tokyo", result[0].city)
            verify { weatherService.getWeatherForecast(any()) }
        }
    }

    @Nested
    @DisplayName("도시 날씨 비교 테스트")
    inner class CompareWeatherTest {

        @Test
        @DisplayName("여러 도시 날씨 비교 성공")
        fun `should compare weather for multiple cities`() {
            // Given
            val expectedComparison = listOf(
                WeatherInfo(
                    city = "Seoul", country = "KR", temperature = 20.0, feelsLike = 22.0,
                    humidity = 60, pressure = 1013, description = "맑음", main = "Clear",
                    windSpeed = 3.5, windDirection = 180, visibility = 10000, units = "metric"
                ),
                WeatherInfo(
                    city = "Tokyo", country = "JP", temperature = 18.0, feelsLike = 20.0,
                    humidity = 55, pressure = 1010, description = "구름 조금", main = "Clouds",
                    windSpeed = 2.0, windDirection = 90, visibility = 9000, units = "metric"
                )
            )
            every { weatherService.compareWeather(any()) } returns expectedComparison

            // When
            val result = weatherMcpService.compareWeather("Seoul, Tokyo", "metric")

            // Then
            assertNotNull(result)
            assertEquals(2, result.size)
            assertEquals("Seoul", result[0].city)
            assertEquals("Tokyo", result[1].city)
            verify { weatherService.compareWeather(any()) }
        }
    }

    @Nested
    @DisplayName("데이터 구조 검증")
    inner class DataStructureTest {

        @Test
        @DisplayName("WeatherInfo 데이터 구조")
        fun `should have correct WeatherInfo structure`() {
            // Given
            val weatherInfo = WeatherInfo(
                city = "Seoul",
                country = "KR",
                temperature = 15.5,
                feelsLike = 18.0,
                humidity = 65,
                pressure = 1020,
                description = "맑음",
                main = "Clear",
                windSpeed = 2.5,
                windDirection = 90,
                visibility = 8000,
                units = "metric"
            )

            // Then
            assertEquals("Seoul", weatherInfo.city)
            assertEquals("KR", weatherInfo.country)
            assertEquals(15.5, weatherInfo.temperature)
            assertEquals(18.0, weatherInfo.feelsLike)
            assertEquals(65, weatherInfo.humidity)
            assertEquals(1020, weatherInfo.pressure)
            assertEquals("맑음", weatherInfo.description)
            assertEquals("Clear", weatherInfo.main)
            assertEquals(2.5, weatherInfo.windSpeed)
            assertEquals(90, weatherInfo.windDirection)
            assertEquals(8000, weatherInfo.visibility)
            assertEquals("metric", weatherInfo.units)
        }

        @Test
        @DisplayName("WeatherForecast 데이터 구조")
        fun `should have correct WeatherForecast structure`() {
            // Given
            val forecast = WeatherForecast(
                dateTime = "2024-01-01 15:00:00",
                temperature = 12.5,
                feelsLike = 14.0,
                humidity = 70,
                pressure = 1015,
                description = "흐림",
                main = "Clouds",
                windSpeed = 4.0,
                windDirection = 120,
                precipitationProbability = 30.0,
                city = "Tokyo",
                units = "metric"
            )

            // Then
            assertEquals("2024-01-01 15:00:00", forecast.dateTime)
            assertEquals(12.5, forecast.temperature)
            assertEquals(14.0, forecast.feelsLike)
            assertEquals(70, forecast.humidity)
            assertEquals(1015, forecast.pressure)
            assertEquals("흐림", forecast.description)
            assertEquals("Clouds", forecast.main)
            assertEquals(4.0, forecast.windSpeed)
            assertEquals(120, forecast.windDirection)
            assertEquals(30.0, forecast.precipitationProbability)
            assertEquals("Tokyo", forecast.city)
            assertEquals("metric", forecast.units)
        }
    }
}
