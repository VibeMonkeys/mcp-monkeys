package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

/**
 * 뉴스 기사 DTO
 */
data class NewsArticle(
    @JsonPropertyDescription("기사 제목")
    val title: String,
    
    @JsonPropertyDescription("기사 설명/요약")
    val description: String,
    
    @JsonPropertyDescription("작성자")
    val author: String,
    
    @JsonPropertyDescription("출처")
    val source: String,
    
    @JsonPropertyDescription("기사 URL")
    val url: String,
    
    @JsonPropertyDescription("이미지 URL")
    val urlToImage: String,
    
    @JsonPropertyDescription("게시 일시")
    val publishedAt: String,
    
    @JsonPropertyDescription("기사 전체 내용")
    val content: String
)

/**
 * 뉴스 헤드라인 요청 DTO
 */
data class NewsHeadlinesRequest(
    @JsonPropertyDescription("국가 코드 (kr, us, jp 등)")
    val country: String = "kr",
    
    @JsonPropertyDescription("카테고리 (business, entertainment, general, health, science, sports, technology)")
    val category: String = "general",
    
    @JsonPropertyDescription("조회할 뉴스 개수")
    val pageSize: Int = 10
)

/**
 * 뉴스 검색 요청 DTO
 */
data class NewsSearchRequest(
    @JsonPropertyDescription("검색 키워드")
    val query: String,
    
    @JsonPropertyDescription("정렬 방식 (relevancy, popularity, publishedAt)")
    val sortBy: String = "publishedAt",
    
    @JsonPropertyDescription("언어 코드 (ko, en, ja 등)")
    val language: String = "ko",
    
    @JsonPropertyDescription("조회할 뉴스 개수")
    val pageSize: Int = 10,
    
    @JsonPropertyDescription("시작 날짜 (YYYY-MM-DD)")
    val fromDate: String? = null,
    
    @JsonPropertyDescription("종료 날짜 (YYYY-MM-DD)")
    val toDate: String? = null
)

/**
 * 출처별 뉴스 요청 DTO
 */
data class NewsBySourceRequest(
    @JsonPropertyDescription("출처 도메인 (예: cnn.com, bbc-news)")
    val source: String,
    
    @JsonPropertyDescription("조회할 뉴스 개수")
    val pageSize: Int = 10
)

/**
 * 뉴스 응답 DTO (클라이언트용)
 */
data class NewsResponse(
    @JsonPropertyDescription("뉴스 기사 목록")
    val articles: List<NewsArticle>,
    
    @JsonPropertyDescription("검색 키워드")
    val searchQuery: String,
    
    @JsonPropertyDescription("총 기사 수")
    val totalCount: Int,
    
    @JsonPropertyDescription("요약 또는 트렌드 분석")
    val summary: String? = null,
    
    @JsonPropertyDescription("카테고리")
    val category: String? = null,
    
    @JsonPropertyDescription("국가")
    val country: String? = null
)

/**
 * 뉴스 통계 DTO
 */
data class NewsStatistics(
    @JsonPropertyDescription("총 기사 수")
    val totalArticles: Int,
    
    @JsonPropertyDescription("출처별 통계")
    val sourceDistribution: Map<String, Int>,
    
    @JsonPropertyDescription("카테고리별 통계") 
    val categoryDistribution: Map<String, Int>,
    
    @JsonPropertyDescription("최근 업데이트 시간")
    val lastUpdated: String
)