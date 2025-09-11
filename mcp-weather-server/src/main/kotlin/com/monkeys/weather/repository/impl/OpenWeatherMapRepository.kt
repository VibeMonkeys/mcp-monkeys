package com.monkeys.weather.repository.impl

import com.monkeys.weather.repository.WeatherRepository
import com.monkeys.shared.dto.WeatherInfo
import com.monkeys.shared.dto.WeatherForecast
import com.monkeys.shared.util.ApiClient
import com.monkeys.shared.util.ApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.*

/**
 * OpenWeatherMap API를 통한 날씨 데이터 조회 구현체
 * Repository 패턴 - 외부 API와의 데이터 액세스 담당
 * 공통 ApiClient 사용으로 HTTP 처리 표준화
 */
@Repository
@Profile("external-api")
class OpenWeatherMapRepository(
    @Value("\${weather.api.key:dummy-key}") private val apiKey: String,
    weatherHttpClient: OkHttpClient,
    meterRegistry: MeterRegistry
) : WeatherRepository {
    
    private val logger = LoggerFactory.getLogger(OpenWeatherMapRepository::class.java)
    private val baseUrl = "https://api.openweathermap.org/data/2.5"
    
    // 공통 ApiClient 사용으로 HTTP 처리 표준화
    private val apiClient = ApiClient(weatherHttpClient, meterRegistry, "weather")
    
    override suspend fun getCurrentWeather(city: String, units: String): WeatherInfo = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val url = "$baseUrl/weather?q=$city&appid=$apiKey&units=$units&lang=ko"
        val weatherData = apiClient.get(url).getDataOrThrow()
        
        mapToWeatherInfo(weatherData, units)
    }
    
    override suspend fun getWeatherForecast(city: String, units: String, count: Int): List<WeatherForecast> = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val url = "$baseUrl/forecast?q=$city&appid=$apiKey&units=$units&lang=ko&cnt=$count"
        val forecastData = apiClient.get(url).getDataOrThrow()
        
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
            val url = "$baseUrl/weather?q=Seoul&appid=$apiKey&units=metric"
            apiClient.get(url).getDataOrThrow()
            true
        } catch (e: Exception) {
            logger.error("Weather API health check failed", e)
            false
        }
    }
    
    private fun validateApiKey() {
        if (apiKey == "dummy-key") {
            throw ApiException("API 키가 설정되지 않음", "MISSING_API_KEY")
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
}