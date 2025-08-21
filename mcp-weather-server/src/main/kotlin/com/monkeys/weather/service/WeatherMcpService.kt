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
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(WeatherMcpService::class.java)
    private val baseUrl = "http://api.openweathermap.org/data/2.5"

    @Tool(description = "지정된 도시의 현재 날씨 정보를 조회합니다")
    fun getCurrentWeather(
        @ToolParam(description = "도시 이름 (예: Seoul, Tokyo, New York)", required = true)
        city: String,
        @ToolParam(description = "단위 (metric: 섭씨, imperial: 화씨, kelvin: 켈빈)")
        units: String = "metric"
    ): WeatherInfo {
        if (apiKey == "dummy-key") {
            return createDummyWeather(city, "OpenWeatherMap API 키가 설정되지 않았습니다. WEATHER_API_KEY 환경변수를 설정해주세요.")
        }
        
        return try {
            val url = "$baseUrl/weather?q=$city&appid=$apiKey&units=$units&lang=ko"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                logger.error("날씨 API 호출 실패: ${response.code} ${response.message}")
                return createDummyWeather(city, "날씨 정보를 가져올 수 없습니다: ${response.code}")
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val weatherData: Map<String, Any> = mapper.readValue(jsonResponse)
            
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
            createDummyWeather(city, "날씨 정보 조회 중 오류: ${e.message}")
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
            val url = "$baseUrl/forecast?q=$city&appid=$apiKey&units=$units&lang=ko&cnt=$count"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                logger.error("날씨 예보 API 호출 실패: ${response.code} ${response.message}")
                return listOf(createDummyForecast(city, "날씨 예보를 가져올 수 없습니다: ${response.code}"))
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val forecastData: Map<String, Any> = mapper.readValue(jsonResponse)
            
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
    private fun createDummyWeather(city: String, errorMessage: String) = WeatherInfo(
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
        units = "metric"
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