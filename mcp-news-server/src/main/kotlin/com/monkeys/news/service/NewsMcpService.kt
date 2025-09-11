package com.monkeys.news.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * News MCP Tool Provider
 * MCP 프로토콜용 Tool 어노테이션만 담당 - 얇은 어댑터 계층
 * 예외 처리는 GlobalExceptionHandler가 담당
 */
@Service
class NewsMcpService(
    private val newsService: NewsService
) {
    private val logger = LoggerFactory.getLogger(NewsMcpService::class.java)

    @Tool(description = "최신 뉴스 헤드라인을 조회합니다")
    fun getTopHeadlines(
        @ToolParam(description = "국가 코드 (kr, us, jp 등)")
        country: String = "kr",
        @ToolParam(description = "카테고리 (business, entertainment, general, health, science, sports, technology)")
        category: String = "general",
        @ToolParam(description = "조회할 뉴스 개수")
        pageSize: Int = 10
    ): List<NewsArticle> {
        logger.info("MCP Tool 호출: getTopHeadlines - country=$country, category=$category, pageSize=$pageSize")
        
        val request = NewsHeadlinesRequest(country = country, category = category, pageSize = pageSize)
        return newsService.getTopHeadlines(request)
    }

    @Tool(description = "키워드로 뉴스를 검색합니다")
    fun searchNews(
        @ToolParam(description = "검색 키워드", required = true)
        query: String,
        @ToolParam(description = "정렬 방식 (relevancy, popularity, publishedAt)")
        sortBy: String = "publishedAt",
        @ToolParam(description = "언어 코드 (ko, en, ja 등)")
        language: String = "ko",
        @ToolParam(description = "조회할 뉴스 개수")
        pageSize: Int = 10
    ): List<NewsArticle> {
        logger.info("MCP Tool 호출: searchNews - query=$query, sortBy=$sortBy, language=$language, pageSize=$pageSize")
        
        val request = NewsSearchRequest(
            query = query,
            sortBy = sortBy,
            language = language,
            pageSize = pageSize
        )
        return newsService.searchNews(request)
    }

    @Tool(description = "특정 출처의 뉴스를 조회합니다")
    fun getNewsBySource(
        @ToolParam(description = "출처 도메인 (예: cnn.com, bbc-news, techcrunch)", required = true)
        source: String,
        @ToolParam(description = "조회할 뉴스 개수")
        pageSize: Int = 10
    ): List<NewsArticle> {
        logger.info("MCP Tool 호출: getNewsBySource - source=$source, pageSize=$pageSize")
        
        val request = NewsBySourceRequest(source = source, pageSize = pageSize)
        return newsService.getNewsBySource(request)
    }
}