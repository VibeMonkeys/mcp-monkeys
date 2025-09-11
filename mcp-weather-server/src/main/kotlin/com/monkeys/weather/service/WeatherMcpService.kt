package com.monkeys.weather.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * Weather MCP Tool Provider
 * MCP 프로토콜용 Tool 어노테이션만 담당 - 얇은 어댑터 계층
 * 예외 처리는 GlobalExceptionHandler가 담당
 */
@Service
class WeatherMcpService(
    private val weatherService: WeatherService
) {
    private val logger = LoggerFactory.getLogger(WeatherMcpService::class.java)

    @Tool(description = "지정된 도시의 현재 날씨 정보를 조회합니다")
    fun getCurrentWeather(
        @ToolParam(description = "도시 이름 (예: Seoul, Tokyo, New York)", required = true)
        city: String,
        @ToolParam(description = "단위 (metric: 섭씨, imperial: 화씨, kelvin: 켈빈)")
        units: String = "metric"
    ): WeatherInfo {
        logger.info("MCP Tool 호출: getCurrentWeather - city=$city, units=$units")
        
        val request = WeatherRequest(city = city, units = units)
        return weatherService.getCurrentWeather(request)
    }

    @Tool(description = "지정된 도시의 5일 날씨 예보를 조회합니다")
    fun getWeatherForecast(
        @ToolParam(description = "도시 이름 (예: Seoul, Tokyo, New York)", required = true)
        city: String,
        @ToolParam(description = "단위 (metric: 섭씨, imperial: 화씨, kelvin: 켈빈)")
        units: String = "metric",
        @ToolParam(description = "예보 개수 (최대 40개, 3시간 단위)")
        count: Int = 8
    ): List<WeatherForecast> {
        logger.info("MCP Tool 호출: getWeatherForecast - city=$city, units=$units, count=$count")
        
        val request = WeatherForecastRequest(city = city, units = units, count = count)
        return weatherService.getWeatherForecast(request)
    }

    @Tool(description = "여러 도시의 날씨를 비교합니다")
    fun compareWeather(
        @ToolParam(description = "비교할 도시들 (쉼표로 구분)", required = true)
        cities: String,
        @ToolParam(description = "단위 (metric: 섭씨, imperial: 화씨, kelvin: 켈빈)")
        units: String = "metric"
    ): List<WeatherInfo> {
        logger.info("MCP Tool 호출: compareWeather - cities=$cities, units=$units")
        
        val request = WeatherCompareRequest(cities = cities, units = units)
        return weatherService.compareWeather(request)
    }
}