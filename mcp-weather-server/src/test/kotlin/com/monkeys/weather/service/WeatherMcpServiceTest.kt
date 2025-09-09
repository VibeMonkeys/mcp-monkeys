package com.monkeys.weather.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("WeatherMcpService 단위 테스트")
class WeatherMcpServiceTest {

    @Nested
    @DisplayName("더미 API 키로 테스트")
    inner class DummyApiKeyTest {

        @Test
        @DisplayName("현재 날씨 조회 - API 키 미설정")
        fun `should return dummy weather when api key is not set`() {
            // Given
            val service = WeatherMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val weather = service.getCurrentWeather("Seoul")
            
            // Then
            assertNotNull(weather)
            assertEquals("Seoul", weather.city)
            assertEquals("XX", weather.country)
            assertEquals(20.0, weather.temperature)
            assertEquals(22.0, weather.feelsLike)
            assertEquals(60, weather.humidity)
            assertEquals(1013, weather.pressure)
            assertTrue(weather.description.contains("테스트 날씨"))
            assertTrue(weather.description.contains("API 키가 설정되지 않았습니다"))
            assertEquals("Clear", weather.main)
            assertEquals(3.5, weather.windSpeed)
            assertEquals(180, weather.windDirection)
            assertEquals(10000, weather.visibility)
            assertEquals("metric", weather.units)
        }

        @Test
        @DisplayName("날씨 예보 조회 - API 키 미설정")
        fun `should return dummy forecast when api key is not set`() {
            // Given
            val service = WeatherMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val forecast = service.getWeatherForecast("Tokyo")
            
            // Then
            assertNotNull(forecast)
            assertEquals(1, forecast.size)
            
            val firstForecast = forecast[0]
            assertEquals("2024-01-01 12:00:00", firstForecast.dateTime)
            assertEquals("Tokyo", firstForecast.city)
            assertEquals(20.0, firstForecast.temperature)
            assertTrue(firstForecast.description.contains("테스트 예보"))
            assertEquals("Clear", firstForecast.main)
            assertEquals(0.0, firstForecast.precipitationProbability)
            assertEquals("metric", firstForecast.units)
        }

        @Test
        @DisplayName("도시 날씨 비교 - API 키 미설정")
        fun `should return dummy weather comparison when api key is not set`() {
            // Given
            val service = WeatherMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val comparison = service.compareWeather("Seoul, Tokyo, New York")
            
            // Then
            assertNotNull(comparison)
            assertEquals(3, comparison.size)
            
            assertEquals("Seoul", comparison[0].city)
            assertEquals("Tokyo", comparison[1].city)
            assertEquals("New York", comparison[2].city)
            
            comparison.forEach { weather ->
                assertEquals(20.0, weather.temperature)
                assertTrue(weather.description.contains("테스트 날씨"))
            }
        }
    }

    @Nested
    @DisplayName("단위 설정 테스트")
    inner class UnitsTest {

        @Test
        @DisplayName("현재 날씨 - 다른 단위")
        fun `should handle different units for current weather`() {
            // Given
            val service = WeatherMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val weatherMetric = service.getCurrentWeather("Seoul", "metric")
            val weatherImperial = service.getCurrentWeather("Seoul", "imperial")
            val weatherKelvin = service.getCurrentWeather("Seoul", "kelvin")
            
            // Then
            assertEquals("metric", weatherMetric.units)
            assertEquals("imperial", weatherImperial.units)
            assertEquals("kelvin", weatherKelvin.units)
        }

        @Test
        @DisplayName("날씨 예보 - 다른 개수")
        fun `should handle different forecast counts`() {
            // Given
            val service = WeatherMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val forecast = service.getWeatherForecast("Seoul", "metric", 1)
            
            // Then
            assertEquals(1, forecast.size)
        }
    }

    @Nested
    @DisplayName("입력값 처리 테스트")
    inner class InputHandlingTest {

        @Test
        @DisplayName("도시 이름 공백 처리")
        fun `should handle whitespace in city names for comparison`() {
            // Given
            val service = WeatherMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val comparison = service.compareWeather(" Seoul , Tokyo,  New York ")
            
            // Then
            assertEquals(3, comparison.size)
            assertEquals("Seoul", comparison[0].city)
            assertEquals("Tokyo", comparison[1].city)
            assertEquals("New York", comparison[2].city)
        }

        @Test
        @DisplayName("단일 도시 비교")
        fun `should handle single city comparison`() {
            // Given
            val service = WeatherMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val comparison = service.compareWeather("Seoul")
            
            // Then
            assertEquals(1, comparison.size)
            assertEquals("Seoul", comparison[0].city)
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