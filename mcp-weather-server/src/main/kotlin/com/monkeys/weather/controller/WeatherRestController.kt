package com.monkeys.weather.controller

import com.monkeys.weather.service.WeatherService
import com.monkeys.shared.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

/**
 * Weather REST API Controller
 * 얇은 컨트롤러 - HTTP 요청 처리와 응답만 담당
 */
@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = ["*"])
class WeatherRestController(
    private val weatherService: WeatherService
) {
    private val logger = LoggerFactory.getLogger(WeatherRestController::class.java)

    @GetMapping("/current")
    fun getCurrentWeather(
        @RequestParam city: String,
        @RequestParam(defaultValue = "metric") units: String
    ): ResponseEntity<BaseResponse<WeatherResponse>> {
        return try {
            val request = WeatherRequest(city = city, units = units)
            val weatherInfo = weatherService.getCurrentWeather(request)
            val response = weatherService.convertToResponse(weatherInfo)
            
            ResponseEntity.ok(BaseResponse.success(response, "날씨 정보 조회 성공"))
        } catch (e: Exception) {
            logger.error("Weather API 오류", e)
            ResponseEntity.badRequest().body(
                BaseResponse.failure("날씨 정보를 조회할 수 없습니다: ${e.message}")
            )
        }
    }

    @PostMapping("/current")
    fun getCurrentWeatherPost(@RequestBody request: WeatherRequest): ResponseEntity<BaseResponse<WeatherResponse>> {
        return try {
            val weatherInfo = weatherService.getCurrentWeather(request)
            val response = weatherService.convertToResponse(weatherInfo)
            
            ResponseEntity.ok(BaseResponse.success(response, "날씨 정보 조회 성공"))
        } catch (e: Exception) {
            logger.error("Weather API 오류", e)
            ResponseEntity.badRequest().body(
                BaseResponse.failure("날씨 정보를 조회할 수 없습니다: ${e.message}")
            )
        }
    }

    @GetMapping("/forecast")
    fun getWeatherForecast(
        @RequestParam city: String,
        @RequestParam(defaultValue = "metric") units: String,
        @RequestParam(defaultValue = "8") count: Int
    ): ResponseEntity<BaseResponse<List<WeatherForecast>>> {
        return try {
            val request = WeatherForecastRequest(city = city, units = units, count = count)
            val forecasts = weatherService.getWeatherForecast(request)
            
            ResponseEntity.ok(BaseResponse.success(forecasts, "날씨 예보 조회 성공"))
        } catch (e: Exception) {
            logger.error("Weather Forecast API 오류", e)
            ResponseEntity.badRequest().body(
                BaseResponse.failure("날씨 예보를 조회할 수 없습니다: ${e.message}")
            )
        }
    }

    @PostMapping("/compare")
    fun compareWeather(@RequestBody request: WeatherCompareRequest): ResponseEntity<BaseResponse<List<WeatherInfo>>> {
        return try {
            val weatherList = weatherService.compareWeather(request)
            
            ResponseEntity.ok(BaseResponse.success(weatherList, "날씨 비교 조회 성공"))
        } catch (e: Exception) {
            logger.error("Weather Compare API 오류", e)
            ResponseEntity.badRequest().body(
                BaseResponse.failure("날씨 비교 정보를 조회할 수 없습니다: ${e.message}")
            )
        }
    }

    @GetMapping("/health")
    fun checkHealth(): ResponseEntity<BaseResponse<Map<String, Any>>> {
        return try {
            val isHealthy = weatherService.checkServiceHealth()
            val healthInfo = mapOf(
                "status" to if (isHealthy) "UP" else "DOWN",
                "service" to "Weather API",
                "timestamp" to System.currentTimeMillis()
            )
            
            ResponseEntity.ok(BaseResponse.success(healthInfo, "상태 확인 완료"))
        } catch (e: Exception) {
            logger.error("Health check 오류", e)
            ResponseEntity.status(500).body(
                BaseResponse.failure("상태 확인 실패: ${e.message}")
            )
        }
    }
}