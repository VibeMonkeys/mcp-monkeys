package com.monkeys.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

data class WeatherResponse(
    @JsonPropertyDescription("현재 위치 이름")
    val location: String,
    
    @JsonPropertyDescription("현재 온도 (섭씨)")
    val temperature: Double,
    
    @JsonPropertyDescription("날씨 상태 설명")
    val condition: String,
    
    @JsonPropertyDescription("습도 퍼센트")
    val humidity: Int,
    
    @JsonPropertyDescription("체감 온도")
    val feelsLike: Double,
    
    @JsonPropertyDescription("추가 정보나 권장사항")
    val recommendation: String? = null
)

data class NewsResponse(
    @JsonPropertyDescription("뉴스 기사 목록")
    val articles: List<NewsArticle>,
    
    @JsonPropertyDescription("검색 키워드")
    val searchQuery: String,
    
    @JsonPropertyDescription("총 기사 수")
    val totalCount: Int,
    
    @JsonPropertyDescription("요약 또는 트렌드 분석")
    val summary: String? = null
)

data class NewsArticle(
    @JsonPropertyDescription("기사 제목")
    val title: String,
    
    @JsonPropertyDescription("기사 요약")
    val summary: String,
    
    @JsonPropertyDescription("게시 일시")
    val publishedAt: String,
    
    @JsonPropertyDescription("출처")
    val source: String,
    
    @JsonPropertyDescription("기사 URL")
    val url: String? = null
)

data class TranslationResponse(
    @JsonPropertyDescription("원본 텍스트")
    val originalText: String,
    
    @JsonPropertyDescription("번역된 텍스트")
    val translatedText: String,
    
    @JsonPropertyDescription("원본 언어 코드")
    val fromLanguage: String,
    
    @JsonPropertyDescription("번역 대상 언어 코드")
    val toLanguage: String,
    
    @JsonPropertyDescription("신뢰도 점수 (0-1)")
    val confidenceScore: Double? = null
)

data class CalendarResponse(
    @JsonPropertyDescription("일정 목록")
    val events: List<CalendarEvent>,
    
    @JsonPropertyDescription("조회 기간")
    val dateRange: String,
    
    @JsonPropertyDescription("총 일정 수")
    val totalCount: Int
)

data class CalendarEvent(
    @JsonPropertyDescription("일정 제목")
    val title: String,
    
    @JsonPropertyDescription("일정 설명")
    val description: String? = null,
    
    @JsonPropertyDescription("시작 시간")
    val startTime: String,
    
    @JsonPropertyDescription("종료 시간")
    val endTime: String,
    
    @JsonPropertyDescription("위치")
    val location: String? = null,
    
    @JsonPropertyDescription("참가자 목록")
    val attendees: List<String>? = null
)

data class MultiServiceResponse(
    @JsonPropertyDescription("수행한 작업들의 요약")
    val summary: String,
    
    @JsonPropertyDescription("사용된 서비스 목록")
    val usedServices: List<String>,
    
    @JsonPropertyDescription("각 서비스별 결과")
    val results: Map<String, Any>,
    
    @JsonPropertyDescription("전체 작업 성공 여부")
    val success: Boolean,
    
    @JsonPropertyDescription("추가 권장사항")
    val recommendations: List<String>? = null
)

data class ErrorResponse(
    @JsonPropertyDescription("에러 메시지")
    val message: String,
    
    @JsonPropertyDescription("에러 코드")
    val errorCode: String,
    
    @JsonPropertyDescription("실패한 서비스 이름")
    val failedService: String? = null,
    
    @JsonPropertyDescription("해결 방법 제안")
    val solution: String? = null
)