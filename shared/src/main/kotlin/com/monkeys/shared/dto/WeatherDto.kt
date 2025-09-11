package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

/**
 * 날씨 정보 DTO
 */
data class WeatherInfo(
    @JsonPropertyDescription("도시명")
    val city: String,
    
    @JsonPropertyDescription("국가 코드")
    val country: String,
    
    @JsonPropertyDescription("현재 온도")
    val temperature: Double,
    
    @JsonPropertyDescription("체감 온도")
    val feelsLike: Double,
    
    @JsonPropertyDescription("습도 (%)")
    val humidity: Int,
    
    @JsonPropertyDescription("기압 (hPa)")
    val pressure: Int,
    
    @JsonPropertyDescription("날씨 설명")
    val description: String,
    
    @JsonPropertyDescription("날씨 주요 상태")
    val main: String,
    
    @JsonPropertyDescription("풍속 (m/s)")
    val windSpeed: Double,
    
    @JsonPropertyDescription("풍향 (degrees)")
    val windDirection: Int,
    
    @JsonPropertyDescription("가시거리 (m)")
    val visibility: Int,
    
    @JsonPropertyDescription("단위 시스템")
    val units: String
)

/**
 * 날씨 예보 DTO
 */
data class WeatherForecast(
    @JsonPropertyDescription("예보 일시")
    val dateTime: String,
    
    @JsonPropertyDescription("예상 온도")
    val temperature: Double,
    
    @JsonPropertyDescription("예상 체감온도")
    val feelsLike: Double,
    
    @JsonPropertyDescription("예상 습도 (%)")
    val humidity: Int,
    
    @JsonPropertyDescription("예상 기압 (hPa)")
    val pressure: Int,
    
    @JsonPropertyDescription("날씨 설명")
    val description: String,
    
    @JsonPropertyDescription("날씨 주요 상태")
    val main: String,
    
    @JsonPropertyDescription("예상 풍속 (m/s)")
    val windSpeed: Double,
    
    @JsonPropertyDescription("예상 풍향 (degrees)")
    val windDirection: Int,
    
    @JsonPropertyDescription("강수 확률 (%)")
    val precipitationProbability: Double,
    
    @JsonPropertyDescription("도시명")
    val city: String,
    
    @JsonPropertyDescription("단위 시스템")
    val units: String
)

/**
 * 날씨 API 요청 DTO
 */
data class WeatherRequest(
    @JsonPropertyDescription("도시명")
    val city: String,
    
    @JsonPropertyDescription("단위 (metric, imperial, kelvin)")
    val units: String = "metric",
    
    @JsonPropertyDescription("언어 코드")
    val language: String = "ko"
)

/**
 * 날씨 예보 요청 DTO
 */
data class WeatherForecastRequest(
    @JsonPropertyDescription("도시명")
    val city: String,
    
    @JsonPropertyDescription("단위 (metric, imperial, kelvin)")
    val units: String = "metric",
    
    @JsonPropertyDescription("예보 개수 (최대 40개)")
    val count: Int = 8,
    
    @JsonPropertyDescription("언어 코드")
    val language: String = "ko"
)

/**
 * 도시별 날씨 비교 요청 DTO
 */
data class WeatherCompareRequest(
    @JsonPropertyDescription("비교할 도시들 (쉼표로 구분)")
    val cities: String,
    
    @JsonPropertyDescription("단위 (metric, imperial, kelvin)")
    val units: String = "metric"
)

/**
 * 날씨 응답 DTO (클라이언트용)
 */
data class WeatherResponse(
    @JsonPropertyDescription("현재 위치 이름")
    val location: String,
    
    @JsonPropertyDescription("현재 온도 (섭씨)")
    val temperature: Double,
    
    @JsonPropertyDescription("날씨 상태 설명")
    val condition: String,
    
    @JsonPropertyDescription("습도 퍼센트")
    val humidity: Int,
    
    @JsonPropertyDescription("체감 온도")
    val feelsLike: Double,
    
    @JsonPropertyDescription("추가 정보나 권장사항")
    val recommendation: String? = null,
    
    @JsonPropertyDescription("상세 날씨 정보")
    val details: WeatherInfo? = null
)