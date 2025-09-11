package com.monkeys.weather.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * Weather MCP Tool Provider
 * MCP 프로토콜용 Tool 어노테이션만 담당 - 얇은 어댑터 계층
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
        
        return try {
            val request = WeatherRequest(city = city, units = units)
            weatherService.getCurrentWeather(request)
        } catch (e: WeatherServiceException) {
            logger.error("날씨 조회 실패: ${e.message}", e)
            createErrorWeatherInfo(city, units, e.message)
        } catch (e: Exception) {
            logger.error("예상치 못한 오류", e)
            createErrorWeatherInfo(city, units, "일시적 오류가 발생했습니다")
        }
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
        
        return try {
            val request = WeatherForecastRequest(city = city, units = units, count = count)
            weatherService.getWeatherForecast(request)
        } catch (e: WeatherServiceException) {
            logger.error("날씨 예보 조회 실패: ${e.message}", e)
            listOf(createErrorWeatherForecast(city, units, e.message))
        } catch (e: Exception) {
            logger.error("예상치 못한 예보 오류", e)
            listOf(createErrorWeatherForecast(city, units, "일시적 오류가 발생했습니다"))
        }
    }

    @Tool(description = "여러 도시의 날씨를 비교합니다")
    fun compareWeather(
        @ToolParam(description = "비교할 도시들 (쉼표로 구분)", required = true)
        cities: String,
        @ToolParam(description = "단위 (metric: 섭씨, imperial: 화씨, kelvin: 켈빈)")
        units: String = "metric"
    ): List<WeatherInfo> {
        logger.info("MCP Tool 호출: compareWeather - cities=$cities, units=$units")
        
        return try {
            val request = WeatherCompareRequest(cities = cities, units = units)
            weatherService.compareWeather(request)
        } catch (e: WeatherServiceException) {
            logger.error("날씨 비교 실패: ${e.message}", e)
            listOf(createErrorWeatherInfo("비교 실패", units, e.message))
        } catch (e: Exception) {
            logger.error("예상치 못한 비교 오류", e)
            listOf(createErrorWeatherInfo("비교 실패", units, "일시적 오류가 발생했습니다"))
        }
    }
    
    // 에러 응답 생성 헬퍼 메소드들
    private fun createErrorWeatherInfo(city: String, units: String, error: String): WeatherInfo {
        return WeatherInfo(
            city = city,
            country = "XX",
            temperature = 0.0,
            feelsLike = 0.0,
            humidity = 0,
            pressure = 0,
            description = "오류: $error",
            main = "Error",
            windSpeed = 0.0,
            windDirection = 0,
            visibility = 0,
            units = units
        )
    }
    
    private fun createErrorWeatherForecast(city: String, units: String, error: String): WeatherForecast {
        return WeatherForecast(
            dateTime = "2024-01-01 12:00:00",
            temperature = 0.0,
            feelsLike = 0.0,
            humidity = 0,
            pressure = 0,
            description = "오류: $error",
            main = "Error",
            windSpeed = 0.0,
            windDirection = 0,
            precipitationProbability = 0.0,
            city = city,
            units = units
        )
    }
}
        
        logger.info("날씨 조회 요청: city={}, units={}, apiKey={}", city, units, maskApiKey(apiKey))
        
        return try {
            val weatherData = executeWithRetry { 
                fetchWeatherData("$baseUrl/weather?q=$city&appid=$apiKey&units=$units&lang=ko")
            }
            
            val main = weatherData["main"] as Map<String, Any>
            val weather = (weatherData["weather"] as List<Map<String, Any>>).first()
            val wind = weatherData["wind"] as? Map<String, Any>
            
            WeatherInfo(
                city = weatherData["name"] as String,
                country = (weatherData["sys"] as Map<String, Any>)["country"] as String,
                temperature = (main["temp"] as Number).toDouble(),
                feelsLike = (main["feels_like"] as Number).toDouble(),
                humidity = main["humidity"] as Int,
                pressure = main["pressure"] as Int,
                description = weather["description"] as String,
                main = weather["main"] as String,
                windSpeed = wind?.get("speed") as? Double ?: 0.0,
                windDirection = wind?.get("deg") as? Int ?: 0,
                visibility = (weatherData["visibility"] as? Number)?.toInt() ?: 0,
                units = units
            )
        } catch (e: WeatherApiException) {
            logger.error("Weather API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("weather.service.error", "type", e.errorCode).increment()
            createDummyWeather(city, "날씨 조회 실패: ${e.message}", units)
        } catch (e: Exception) {
            logger.error("예상치 못한 오류", e)
            meterRegistry.counter("weather.service.error", "type", "UNEXPECTED").increment()
            createDummyWeather(city, "일시적 오류가 발생했습니다", units)
        }
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
        if (apiKey == "dummy-key") {
            return listOf(createDummyForecast(city, "OpenWeatherMap API 키가 설정되지 않았습니다."))
        }
        
        return try {
            val forecastData = executeWithRetry { 
                fetchWeatherData("$baseUrl/forecast?q=$city&appid=$apiKey&units=$units&lang=ko&cnt=$count")
            }
            
            val forecasts = forecastData["list"] as List<Map<String, Any>>
            val cityInfo = forecastData["city"] as Map<String, Any>
            
            forecasts.map { forecast ->
                val main = forecast["main"] as Map<String, Any>
                val weather = (forecast["weather"] as List<Map<String, Any>>).first()
                val wind = forecast["wind"] as? Map<String, Any>
                
                WeatherForecast(
                    dateTime = forecast["dt_txt"] as String,
                    temperature = (main["temp"] as Number).toDouble(),
                    feelsLike = (main["feels_like"] as Number).toDouble(),
                    humidity = main["humidity"] as Int,
                    pressure = main["pressure"] as Int,
                    description = weather["description"] as String,
                    main = weather["main"] as String,
                    windSpeed = wind?.get("speed") as? Double ?: 0.0,
                    windDirection = wind?.get("deg") as? Int ?: 0,
                    precipitationProbability = ((forecast["pop"] as? Number)?.toDouble() ?: 0.0) * 100,
                    city = cityInfo["name"] as String,
                    units = units
                )
            }
        } catch (e: WeatherApiException) {
            logger.error("Weather Forecast API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("weather.forecast.error", "type", e.errorCode).increment()
            listOf(createDummyForecast(city, "예보 조회 실패: ${e.message}"))
        } catch (e: Exception) {
            logger.error("예상치 못한 예보 오류", e)
            meterRegistry.counter("weather.forecast.error", "type", "UNEXPECTED").increment()
            listOf(createDummyForecast(city, "일시적 오류가 발생했습니다"))
        }
    }

    @Tool(description = "여러 도시의 날씨를 비교합니다")
    fun compareWeather(
        @ToolParam(description = "비교할 도시들 (쉼표로 구분)", required = true)
        cities: String,
        @ToolParam(description = "단위 (metric: 섭씨, imperial: 화씨, kelvin: 켈빈)")
        units: String = "metric"
    ): List<WeatherInfo> {
        val cityList = cities.split(",").map { it.trim() }
        return cityList.map { city ->
            getCurrentWeather(city, units)
        }
    }

    // 더미 데이터 생성 함수들
    private fun createDummyWeather(city: String, errorMessage: String, units: String = "metric") = WeatherInfo(
        city = city,
        country = "XX",
        temperature = 20.0,
        feelsLike = 22.0,
        humidity = 60,
        pressure = 1013,
        description = "테스트 날씨 ($errorMessage)",
        main = "Clear",
        windSpeed = 3.5,
        windDirection = 180,
        visibility = 10000,
        units = units
    )
    
    private fun createDummyForecast(city: String, errorMessage: String) = WeatherForecast(
        dateTime = "2024-01-01 12:00:00",
        temperature = 20.0,
        feelsLike = 22.0,
        humidity = 60,
        pressure = 1013,
        description = "테스트 예보 ($errorMessage)",
        main = "Clear",
        windSpeed = 3.5,
        windDirection = 180,
        precipitationProbability = 0.0,
        city = city,
        units = "metric"
    )
    
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
    
    // HTTP 요청 실행 및 JSON 파싱 (메트릭 포함)
    private fun fetchWeatherData(url: String): Map<String, Any> {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "MCP-Monkeys-Weather/1.0")
            .build()

        return weatherApiTimer.recordCallable {
            weatherHttpClient.newCall(request).execute().use { response ->
                when (response.code) {
                    200 -> {
                        val jsonResponse = response.body?.string() ?: "{}"
                        meterRegistry.counter("weather.api.success").increment()
                        mapper.readValue(jsonResponse)
                    }
                    401 -> {
                        meterRegistry.counter("weather.api.error", "type", "auth").increment()
                        throw WeatherApiException("인증 실패", "INVALID_API_KEY")
                    }
                    404 -> {
                        meterRegistry.counter("weather.api.error", "type", "not_found").increment() 
                        throw WeatherApiException("도시를 찾을 수 없음", "CITY_NOT_FOUND")
                    }
                    429 -> {
                        meterRegistry.counter("weather.api.error", "type", "rate_limit").increment()
                        throw WeatherApiException("API 사용량 한도 초과", "RATE_LIMIT_EXCEEDED")
                    }
                    500, 502, 503, 504 -> {
                        meterRegistry.counter("weather.api.error", "type", "server_error").increment()
                        throw WeatherApiException("서버 오류", "SERVER_ERROR")
                    }
                    else -> {
                        meterRegistry.counter("weather.api.error", "type", "unknown").increment()
                        throw WeatherApiException("알 수 없는 오류", "UNKNOWN_ERROR")
                    }
                }
            }
        } ?: throw WeatherApiException("응답 파싱 실패", "PARSE_ERROR")
    }
    
    // API 키 마스킹 유틸리티
    private fun maskApiKey(apiKey: String): String {
        return if (apiKey.length > 8) {
            "${apiKey.take(4)}****${apiKey.takeLast(4)}"
        } else {
            "****"
        }
    }
}

// 커스텀 예외 클래스
class WeatherApiException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

// 날씨 정보 데이터 클래스
data class WeatherInfo(
    val city: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val description: String,
    val main: String,
    val windSpeed: Double,
    val windDirection: Int,
    val visibility: Int,
    val units: String
)

// 날씨 예보 데이터 클래스
data class WeatherForecast(
    val dateTime: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val description: String,
    val main: String,
    val windSpeed: Double,
    val windDirection: Int,
    val precipitationProbability: Double,
    val city: String,
    val units: String
)