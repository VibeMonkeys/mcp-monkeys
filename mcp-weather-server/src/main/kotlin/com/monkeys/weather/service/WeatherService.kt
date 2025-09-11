package com.monkeys.weather.service

import com.monkeys.weather.repository.WeatherRepository
import com.monkeys.shared.dto.*
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import kotlinx.coroutines.runBlocking

/**
 * 날씨 비즈니스 로직 서비스
 * 순수한 비즈니스 로직만 담당 - 외부 API나 프레젠테이션 관심사 제외
 */
@Service
class WeatherService(
    private val weatherRepository: WeatherRepository
) {
    private val logger = LoggerFactory.getLogger(WeatherService::class.java)
    
    /**
     * 현재 날씨 조회
     */
    fun getCurrentWeather(request: WeatherRequest): WeatherInfo = runBlocking {
        validateCity(request.city)
        validateUnits(request.units)
        
        logger.info("날씨 조회 요청: city=${request.city}, units=${request.units}")
        
        try {
            weatherRepository.getCurrentWeather(request.city, request.units)
        } catch (e: Exception) {
            logger.error("날씨 조회 실패: city=${request.city}", e)
            throw WeatherServiceException("날씨 정보를 조회할 수 없습니다: ${e.message}", "WEATHER_FETCH_FAILED", e)
        }
    }
    
    /**
     * 날씨 예보 조회
     */
    fun getWeatherForecast(request: WeatherForecastRequest): List<WeatherForecast> = runBlocking {
        validateCity(request.city)
        validateUnits(request.units)
        validateCount(request.count)
        
        logger.info("날씨 예보 요청: city=${request.city}, units=${request.units}, count=${request.count}")
        
        try {
            weatherRepository.getWeatherForecast(request.city, request.units, request.count)
        } catch (e: Exception) {
            logger.error("날씨 예보 조회 실패: city=${request.city}", e)
            throw WeatherServiceException("날씨 예보를 조회할 수 없습니다: ${e.message}", "FORECAST_FETCH_FAILED", e)
        }
    }
    
    /**
     * 여러 도시 날씨 비교
     */
    fun compareWeather(request: WeatherCompareRequest): List<WeatherInfo> = runBlocking {
        val cities = parseCities(request.cities)
        validateUnits(request.units)
        
        if (cities.isEmpty()) {
            throw WeatherServiceException("비교할 도시가 지정되지 않았습니다", "NO_CITIES_PROVIDED")
        }
        
        if (cities.size > 10) {
            throw WeatherServiceException("한 번에 비교할 수 있는 도시는 최대 10개입니다", "TOO_MANY_CITIES")
        }
        
        logger.info("날씨 비교 요청: cities=${cities.joinToString()}, units=${request.units}")
        
        try {
            weatherRepository.getWeatherForCities(cities, request.units)
        } catch (e: Exception) {
            logger.error("날씨 비교 조회 실패: cities=${cities.joinToString()}", e)
            throw WeatherServiceException("날씨 비교 정보를 조회할 수 없습니다: ${e.message}", "COMPARE_FETCH_FAILED", e)
        }
    }
    
    /**
     * WeatherInfo를 클라이언트용 WeatherResponse로 변환
     */
    fun convertToResponse(weatherInfo: WeatherInfo): WeatherResponse {
        return WeatherResponse(
            location = "${weatherInfo.city}, ${weatherInfo.country}",
            temperature = weatherInfo.temperature,
            condition = weatherInfo.description,
            humidity = weatherInfo.humidity,
            feelsLike = weatherInfo.feelsLike,
            recommendation = generateRecommendation(weatherInfo),
            details = weatherInfo
        )
    }
    
    /**
     * 날씨 상태에 따른 권장사항 생성
     */
    private fun generateRecommendation(weather: WeatherInfo): String {
        return when {
            weather.temperature < 0 -> "매우 추운 날씨입니다. 따뜻한 옷을 챙기세요."
            weather.temperature < 10 -> "쌀쌀한 날씨입니다. 겉옷을 준비하세요."
            weather.temperature > 30 -> "매우 더운 날씨입니다. 수분 섭취에 주의하세요."
            weather.humidity > 80 -> "습도가 높습니다. 불쾌지수에 주의하세요."
            weather.windSpeed > 10 -> "바람이 강합니다. 외출 시 주의하세요."
            weather.main.contains("Rain", ignoreCase = true) -> "비가 오고 있습니다. 우산을 챙기세요."
            weather.main.contains("Snow", ignoreCase = true) -> "눈이 오고 있습니다. 미끄러짐에 주의하세요."
            else -> "좋은 날씨입니다. 즐거운 하루 보내세요!"
        }
    }
    
    /**
     * 서비스 상태 확인
     */
    fun checkServiceHealth(): Boolean = runBlocking {
        try {
            weatherRepository.checkApiHealth()
        } catch (e: Exception) {
            logger.error("Weather service health check failed", e)
            false
        }
    }
    
    // 검증 메소드들
    private fun validateCity(city: String) {
        if (city.isBlank()) {
            throw WeatherServiceException("도시명이 필요합니다", "CITY_REQUIRED")
        }
        if (city.length > 100) {
            throw WeatherServiceException("도시명이 너무 깁니다", "CITY_NAME_TOO_LONG")
        }
    }
    
    private fun validateUnits(units: String) {
        val validUnits = setOf("metric", "imperial", "kelvin")
        if (units !in validUnits) {
            throw WeatherServiceException("지원하지 않는 단위입니다: $units", "INVALID_UNITS")
        }
    }
    
    private fun validateCount(count: Int) {
        if (count < 1 || count > 40) {
            throw WeatherServiceException("예보 개수는 1-40 사이여야 합니다", "INVALID_COUNT")
        }
    }
    
    private fun parseCities(citiesString: String): List<String> {
        return citiesString.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}

/**
 * Weather Service 전용 예외
 */
class WeatherServiceException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)