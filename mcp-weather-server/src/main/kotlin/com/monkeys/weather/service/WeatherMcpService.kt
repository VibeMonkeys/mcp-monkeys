package com.monkeys.weather.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import okhttp3.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory

@Service
class WeatherMcpService(
    @Value("\${weather.api.key:dummy-key}") private val apiKey: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(WeatherMcpService::class.java)
    private val baseUrl = "http://api.openweathermap.org/data/2.5"
    private val maxRetries = 3

    @Tool(description = "지정된 도시의 현재 날씨 정보를 조회합니다")
    fun getCurrentWeather(
        @ToolParam(description = "도시 이름 (예: Seoul, Tokyo, New York)", required = true)
        city: String,
        @ToolParam(description = "단위 (metric: 섭씨, imperial: 화씨, kelvin: 켈빈)")
        units: String = "metric"
    ): WeatherInfo {
        if (apiKey == "dummy-key") {
            return createDummyWeather(city, "OpenWeatherMap API 키가 설정되지 않았습니다. WEATHER_API_KEY 환경변수를 설정해주세요.", units)
        }
        
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
        } catch (e: Exception) {
            logger.error("날씨 정보 조회 중 오류", e)
            createDummyWeather(city, "날씨 정보 조회 중 오류: ${e.message}", units)
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
        } catch (e: Exception) {
            logger.error("날씨 예보 조회 중 오류", e)
            listOf(createDummyForecast(city, "날씨 예보 조회 중 오류: ${e.message}"))
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
    
    // HTTP 요청 실행 및 JSON 파싱
    private fun fetchWeatherData(url: String): Map<String, Any> {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "MCP-Monkeys-Weather/1.0")
            .build()

        client.newCall(request).execute().use { response ->
            when (response.code) {
                200 -> {
                    val jsonResponse = response.body?.string() ?: "{}"
                    return mapper.readValue(jsonResponse)
                }
                401 -> throw IllegalArgumentException("잘못된 API 키입니다. WEATHER_API_KEY를 확인해주세요.")
                404 -> throw IllegalArgumentException("도시를 찾을 수 없습니다. 도시명을 확인해주세요.")
                429 -> throw RuntimeException("API 사용량 한도를 초과했습니다. 잠시 후 다시 시도해주세요.")
                500, 502, 503, 504 -> throw RuntimeException("날씨 서비스 일시 장애입니다. 잠시 후 다시 시도해주세요.")
                else -> throw RuntimeException("예상치 못한 오류입니다: ${response.code} ${response.message}")
            }
        }
    }
}

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