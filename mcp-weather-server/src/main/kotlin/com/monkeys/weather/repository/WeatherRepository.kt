package com.monkeys.weather.repository

import com.monkeys.shared.dto.WeatherInfo
import com.monkeys.shared.dto.WeatherForecast

/**
 * 날씨 데이터 액세스 인터페이스
 * Repository 패턴 적용 - 데이터 소스와의 분리
 */
interface WeatherRepository {
    
    /**
     * 현재 날씨 정보 조회
     */
    suspend fun getCurrentWeather(city: String, units: String): WeatherInfo
    
    /**
     * 날씨 예보 조회
     */
    suspend fun getWeatherForecast(city: String, units: String, count: Int): List<WeatherForecast>
    
    /**
     * 여러 도시 날씨 조회
     */
    suspend fun getWeatherForCities(cities: List<String>, units: String): List<WeatherInfo>
    
    /**
     * API 상태 확인
     */
    suspend fun checkApiHealth(): Boolean
}