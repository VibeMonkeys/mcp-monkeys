package com.monkeys.weather.repository.impl

import com.monkeys.weather.repository.WeatherRepository
import com.monkeys.shared.dto.WeatherInfo
import com.monkeys.shared.dto.WeatherForecast
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import okhttp3.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * OpenWeatherMap API를 통한 날씨 데이터 조회 구현체
 * Repository 패턴 - 외부 API와의 데이터 액세스 담당
 */
@Repository
class OpenWeatherMapRepository(
    @Value("\${weather.api.key:dummy-key}") private val apiKey: String,
    private val weatherHttpClient: OkHttpClient,
    private val meterRegistry: MeterRegistry
) : WeatherRepository {
    
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(OpenWeatherMapRepository::class.java)
    private val baseUrl = "https://api.openweathermap.org/data/2.5"
    private val maxRetries = 3
    
    // 메트릭 타이머
    private val weatherApiTimer = Timer.builder("weather.api.request")
        .description("Weather API 요청 시간")
        .register(meterRegistry)
    
    override suspend fun getCurrentWeather(city: String, units: String): WeatherInfo = withContext(Dispatchers.IO) {
        if (apiKey == "dummy-key") {
            throw WeatherRepositoryException("API 키가 설정되지 않음", "MISSING_API_KEY")
        }
        
        val weatherData = executeWithRetry { 
            fetchWeatherData("$baseUrl/weather?q=$city&appid=$apiKey&units=$units&lang=ko")
        }
        
        mapToWeatherInfo(weatherData, units)
    }
    
    override suspend fun getWeatherForecast(city: String, units: String, count: Int): List<WeatherForecast> = withContext(Dispatchers.IO) {
        if (apiKey == "dummy-key") {
            throw WeatherRepositoryException("API 키가 설정되지 않음", "MISSING_API_KEY")
        }
        
        val forecastData = executeWithRetry { 
            fetchWeatherData("$baseUrl/forecast?q=$city&appid=$apiKey&units=$units&lang=ko&cnt=$count")
        }
        
        mapToWeatherForecastList(forecastData, units)
    }
    
    override suspend fun getWeatherForCities(cities: List<String>, units: String): List<WeatherInfo> = withContext(Dispatchers.IO) {
        cities.map { city ->
            try {
                getCurrentWeather(city, units)
            } catch (e: Exception) {
                logger.warn("Failed to get weather for city: $city", e)
                createErrorWeatherInfo(city, units, e.message ?: "Unknown error")
            }
        }
    }
    
    override suspend fun checkApiHealth(): Boolean = withContext(Dispatchers.IO) {
        if (apiKey == "dummy-key") return@withContext false
        
        try {
            executeWithRetry { 
                fetchWeatherData("$baseUrl/weather?q=Seoul&appid=$apiKey&units=metric")
            }
            true
        } catch (e: Exception) {
            logger.error("Weather API health check failed", e)
            false
        }
    }
    
    /**
     * API 응답을 WeatherInfo DTO로 변환
     */
    private fun mapToWeatherInfo(weatherData: Map<String, Any>, units: String): WeatherInfo {
        val main = weatherData["main"] as Map<String, Any>
        val weather = (weatherData["weather"] as List<Map<String, Any>>).first()
        val wind = weatherData["wind"] as? Map<String, Any>
        
        return WeatherInfo(
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
    }
    
    /**
     * API 응답을 WeatherForecast 리스트로 변환
     */
    private fun mapToWeatherForecastList(forecastData: Map<String, Any>, units: String): List<WeatherForecast> {
        val forecasts = forecastData["list"] as List<Map<String, Any>>
        val cityInfo = forecastData["city"] as Map<String, Any>
        
        return forecasts.map { forecast ->
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
    }
    
    /**
     * 에러 발생 시 기본 날씨 정보 생성
     */
    private fun createErrorWeatherInfo(city: String, units: String, error: String): WeatherInfo {
        return WeatherInfo(
            city = city,
            country = "XX",
            temperature = 0.0,
            feelsLike = 0.0,
            humidity = 0,
            pressure = 0,
            description = "데이터 조회 실패: $error",
            main = "Error",
            windSpeed = 0.0,
            windDirection = 0,
            visibility = 0,
            units = units
        )
    }
    
    /**
     * 재시도 로직을 포함한 API 호출
     */
    private fun <T> executeWithRetry(operation: () -> T): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logger.warn("API 호출 시도 ${attempt + 1}/$maxRetries 실패: ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    val delay = (1000 * Math.pow(2.0, attempt.toDouble())).toLong()
                    Thread.sleep(delay)
                }
            }
        }
        
        throw lastException ?: RuntimeException("API 호출이 $maxRetries 번 모두 실패했습니다")
    }
    
    /**
     * HTTP 요청 실행 및 JSON 파싱 (메트릭 포함)
     */
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
                        throw WeatherRepositoryException("인증 실패", "INVALID_API_KEY")
                    }
                    404 -> {
                        meterRegistry.counter("weather.api.error", "type", "not_found").increment() 
                        throw WeatherRepositoryException("도시를 찾을 수 없음", "CITY_NOT_FOUND")
                    }
                    429 -> {
                        meterRegistry.counter("weather.api.error", "type", "rate_limit").increment()
                        throw WeatherRepositoryException("API 사용량 한도 초과", "RATE_LIMIT_EXCEEDED")
                    }
                    500, 502, 503, 504 -> {
                        meterRegistry.counter("weather.api.error", "type", "server_error").increment()
                        throw WeatherRepositoryException("서버 오류", "SERVER_ERROR")
                    }
                    else -> {
                        meterRegistry.counter("weather.api.error", "type", "unknown").increment()
                        throw WeatherRepositoryException("알 수 없는 오류", "UNKNOWN_ERROR")
                    }
                }
            }
        } ?: throw WeatherRepositoryException("응답 파싱 실패", "PARSE_ERROR")
    }
}

/**
 * Weather Repository 전용 예외
 */
class WeatherRepositoryException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)