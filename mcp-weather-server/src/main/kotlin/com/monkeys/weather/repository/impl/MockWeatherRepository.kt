package com.monkeys.weather.repository.impl

import com.monkeys.weather.repository.WeatherRepository
import com.monkeys.shared.dto.WeatherInfo
import com.monkeys.shared.dto.WeatherForecast
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Mock 날씨 데이터 제공 Repository
 * 외부 API 의존성 없이 하드코딩된 날씨 정보 제공
 * 데모 및 테스트 환경에서 사용
 */
@Repository
@Profile("!external-api")
class MockWeatherRepository : WeatherRepository {
    
    private val logger = LoggerFactory.getLogger(MockWeatherRepository::class.java)
    
    // 주요 도시들의 Mock 날씨 데이터
    private val mockWeatherData = mapOf(
        "서울" to WeatherInfo(
            city = "서울",
            country = "KR",
            main = "Clear",
            description = "맑음",
            temperature = 22.5,
            feelsLike = 24.1,
            humidity = 65,
            pressure = 1013,
            windSpeed = 2.1,
            windDirection = 180,
            visibility = 10.0,
            cloudiness = 10,
            uvIndex = 5,
            timezone = 32400,
            sunrise = "06:30",
            sunset = "18:45"
        ),
        "부산" to WeatherInfo(
            city = "부산",
            country = "KR",
            main = "Clouds",
            description = "구름많음",
            temperature = 25.1,
            feelsLike = 26.8,
            humidity = 72,
            pressure = 1015,
            windSpeed = 3.2,
            windDirection = 225,
            visibility = 8.5,
            cloudiness = 60,
            uvIndex = 4,
            timezone = 32400,
            sunrise = "06:32",
            sunset = "18:48"
        ),
        "대구" to WeatherInfo(
            city = "대구",
            country = "KR",
            main = "Rain",
            description = "가벼운 비",
            temperature = 19.8,
            feelsLike = 20.5,
            humidity = 85,
            pressure = 1008,
            windSpeed = 1.8,
            windDirection = 90,
            visibility = 6.0,
            cloudiness = 85,
            uvIndex = 2,
            timezone = 32400,
            sunrise = "06:31",
            sunset = "18:46"
        ),
        "인천" to WeatherInfo(
            city = "인천",
            country = "KR",
            main = "Clear",
            description = "맑음",
            temperature = 21.3,
            feelsLike = 22.9,
            humidity = 58,
            pressure = 1014,
            windSpeed = 2.8,
            windDirection = 200,
            visibility = 10.0,
            cloudiness = 5,
            uvIndex = 6,
            timezone = 32400,
            sunrise = "06:29",
            sunset = "18:44"
        ),
        "광주" to WeatherInfo(
            city = "광주",
            country = "KR",
            main = "Mist",
            description = "안개",
            temperature = 18.6,
            feelsLike = 19.2,
            humidity = 92,
            pressure = 1011,
            windSpeed = 1.2,
            windDirection = 45,
            visibility = 3.0,
            cloudiness = 40,
            uvIndex = 1,
            timezone = 32400,
            sunrise = "06:33",
            sunset = "18:47"
        ),
        "대전" to WeatherInfo(
            city = "대전",
            country = "KR",
            main = "Clear",
            description = "맑음",
            temperature = 23.8,
            feelsLike = 25.1,
            humidity = 61,
            pressure = 1016,
            windSpeed = 2.5,
            windDirection = 170,
            visibility = 10.0,
            cloudiness = 15,
            uvIndex = 5,
            timezone = 32400,
            sunrise = "06:31",
            sunset = "18:45"
        ),
        "울산" to WeatherInfo(
            city = "울산",
            country = "KR",
            main = "Clouds",
            description = "흐림",
            temperature = 24.2,
            feelsLike = 25.8,
            humidity = 68,
            pressure = 1012,
            windSpeed = 3.5,
            windDirection = 240,
            visibility = 9.0,
            cloudiness = 75,
            uvIndex = 3,
            timezone = 32400,
            sunrise = "06:32",
            sunset = "18:48"
        ),
        "제주" to WeatherInfo(
            city = "제주",
            country = "KR",
            main = "Clear",
            description = "맑음",
            temperature = 26.7,
            feelsLike = 28.3,
            humidity = 55,
            pressure = 1018,
            windSpeed = 4.2,
            windDirection = 280,
            visibility = 10.0,
            cloudiness = 8,
            uvIndex = 7,
            timezone = 32400,
            sunrise = "06:35",
            sunset = "18:50"
        )
    )
    
    override suspend fun getCurrentWeather(city: String, units: String): WeatherInfo = withContext(Dispatchers.IO) {
        logger.info("Mock 날씨 조회: city=$city, units=$units")
        
        // 도시명 정규화 (대소문자, 공백 처리)
        val normalizedCity = city.trim()
        
        val baseWeather = mockWeatherData[normalizedCity] 
            ?: mockWeatherData.entries.firstOrNull { it.key.contains(normalizedCity, ignoreCase = true) }?.value
            ?: throw RuntimeException("지원하지 않는 도시입니다: $normalizedCity. 지원 도시: ${mockWeatherData.keys.joinToString()}")
        
        // 단위 변환 적용
        val convertedWeather = when (units) {
            "imperial" -> baseWeather.copy(
                temperature = baseWeather.temperature * 9/5 + 32,
                feelsLike = baseWeather.feelsLike * 9/5 + 32,
                windSpeed = baseWeather.windSpeed * 2.237 // m/s to mph
            )
            "kelvin" -> baseWeather.copy(
                temperature = baseWeather.temperature + 273.15,
                feelsLike = baseWeather.feelsLike + 273.15
            )
            else -> baseWeather // metric (기본값)
        }
        
        logger.info("Mock 날씨 응답: ${convertedWeather.city} ${convertedWeather.temperature}°")
        convertedWeather
    }
    
    override suspend fun getWeatherForecast(city: String, units: String, count: Int): List<WeatherForecast> = withContext(Dispatchers.IO) {
        logger.info("Mock 날씨 예보 조회: city=$city, units=$units, count=$count")
        
        val baseWeather = getCurrentWeather(city, units)
        val forecasts = mutableListOf<WeatherForecast>()
        
        repeat(count) { index ->
            val forecastTime = LocalDateTime.now().plusHours((index + 1) * 3L)
            val tempVariation = (-3..3).random().toDouble()
            val humidityVariation = (-10..10).random()
            
            forecasts.add(
                WeatherForecast(
                    dateTime = forecastTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    temperature = baseWeather.temperature + tempVariation,
                    feelsLike = baseWeather.feelsLike + tempVariation,
                    humidity = (baseWeather.humidity + humidityVariation).coerceIn(0, 100),
                    pressure = baseWeather.pressure + (-5..5).random(),
                    main = baseWeather.main,
                    description = baseWeather.description,
                    windSpeed = baseWeather.windSpeed + (-1.0..1.0).random(),
                    windDirection = baseWeather.windDirection + (-30..30).random(),
                    cloudiness = (baseWeather.cloudiness + (-20..20).random()).coerceIn(0, 100),
                    pop = if (baseWeather.main.contains("Rain", ignoreCase = true)) 0.8 else 0.1
                )
            )
        }
        
        logger.info("Mock 날씨 예보 응답: ${forecasts.size}개 항목")
        forecasts
    }
    
    override suspend fun getWeatherForCities(cities: List<String>, units: String): List<WeatherInfo> = withContext(Dispatchers.IO) {
        logger.info("Mock 여러 도시 날씨 조회: cities=${cities.joinToString()}, units=$units")
        
        val results = cities.map { city ->
            try {
                getCurrentWeather(city, units)
            } catch (e: Exception) {
                // 지원하지 않는 도시는 기본값 반환
                WeatherInfo(
                    city = city,
                    country = "Unknown",
                    main = "Unknown",
                    description = "데이터 없음",
                    temperature = 20.0,
                    feelsLike = 20.0,
                    humidity = 50,
                    pressure = 1013,
                    windSpeed = 0.0,
                    windDirection = 0,
                    visibility = 10.0,
                    cloudiness = 0,
                    uvIndex = 0,
                    timezone = 0,
                    sunrise = "06:00",
                    sunset = "18:00"
                )
            }
        }
        
        logger.info("Mock 여러 도시 날씨 응답: ${results.size}개 도시")
        results
    }
    
    override suspend fun checkApiHealth(): Boolean {
        logger.info("Mock Weather Repository 상태 확인")
        return true // Mock은 항상 정상
    }
}