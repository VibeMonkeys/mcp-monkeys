package com.monkeys.weather.controller

import com.monkeys.weather.service.WeatherMcpService
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = ["*"])
class WeatherController(
    private val weatherService: WeatherMcpService
) {

    @GetMapping("/current")
    fun getCurrentWeather(
        @RequestParam city: String,
        @RequestParam(defaultValue = "metric") units: String
    ): ResponseEntity<Any> {
        return try {
            val weather = weatherService.getCurrentWeather(city, units)
            ResponseEntity.ok(weather)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                mapOf(
                    "error" to "날씨 정보 조회 실패",
                    "message" to e.message
                )
            )
        }
    }

    @GetMapping("/forecast")
    fun getWeatherForecast(
        @RequestParam city: String,
        @RequestParam(defaultValue = "metric") units: String,
        @RequestParam(defaultValue = "8") count: Int
    ): ResponseEntity<Any> {
        return try {
            val forecast = weatherService.getWeatherForecast(city, units, count)
            ResponseEntity.ok(forecast)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                mapOf(
                    "error" to "날씨 예보 조회 실패",
                    "message" to e.message
                )
            )
        }
    }

    @GetMapping("/compare")
    fun compareWeather(
        @RequestParam cities: String,
        @RequestParam(defaultValue = "metric") units: String
    ): ResponseEntity<Any> {
        return try {
            val comparison = weatherService.compareWeather(cities, units)
            ResponseEntity.ok(comparison)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                mapOf(
                    "error" to "날씨 비교 실패",
                    "message" to e.message
                )
            )
        }
    }

    @GetMapping("/status")
    fun getStatus(): Map<String, Any> {
        return mapOf(
            "service" to "Weather MCP Server",
            "version" to "1.0.0",
            "status" to "UP",
            "port" to 8092,
            "endpoints" to listOf(
                "/api/weather/current",
                "/api/weather/forecast", 
                "/api/weather/compare"
            ),
            "timestamp" to System.currentTimeMillis()
        )
    }
}