package com.monkeys.news.service

import com.monkeys.news.repository.NewsRepository
import com.monkeys.shared.dto.*
import com.monkeys.shared.exception.BusinessException
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import kotlinx.coroutines.runBlocking

/**
 * 뉴스 비즈니스 로직 서비스
 */
@Service
class NewsService(
    private val newsRepository: NewsRepository
) {
    private val logger = LoggerFactory.getLogger(NewsService::class.java)
    
    fun getTopHeadlines(request: NewsHeadlinesRequest): List<NewsArticle> = runBlocking {
        validateHeadlinesRequest(request)
        
        logger.info("뉴스 헤드라인 조회: country=${request.country}, category=${request.category}")
        
        try {
            newsRepository.getTopHeadlines(request)
        } catch (e: Exception) {
            logger.error("뉴스 헤드라인 조회 실패", e)
            throw BusinessException("뉴스 헤드라인을 조회할 수 없습니다: ${e.message}", "HEADLINES_FETCH_FAILED", e)
        }
    }
    
    fun searchNews(request: NewsSearchRequest): List<NewsArticle> = runBlocking {
        validateSearchRequest(request)
        
        logger.info("뉴스 검색: query=${request.query}, language=${request.language}")
        
        try {
            newsRepository.searchNews(request)
        } catch (e: Exception) {
            logger.error("뉴스 검색 실패", e)
            throw BusinessException("뉴스 검색을 실행할 수 없습니다: ${e.message}", "SEARCH_FAILED", e)
        }
    }
    
    fun getNewsBySource(request: NewsBySourceRequest): List<NewsArticle> = runBlocking {
        validateSourceRequest(request)
        
        logger.info("출처별 뉴스 조회: source=${request.source}")
        
        try {
            newsRepository.getNewsBySource(request)
        } catch (e: Exception) {
            logger.error("출처별 뉴스 조회 실패", e)
            throw BusinessException("출처별 뉴스를 조회할 수 없습니다: ${e.message}", "SOURCE_FETCH_FAILED", e)
        }
    }
    
    fun convertToResponse(articles: List<NewsArticle>, searchQuery: String = ""): NewsResponse {
        return NewsResponse(
            articles = articles,
            searchQuery = searchQuery,
            totalCount = articles.size,
            summary = generateSummary(articles),
            category = null,
            country = null
        )
    }
    
    private fun generateSummary(articles: List<NewsArticle>): String {
        return when {
            articles.isEmpty() -> "검색 결과가 없습니다."
            articles.size == 1 -> "1개의 뉴스 기사를 찾았습니다."
            else -> "${articles.size}개의 뉴스 기사를 찾았습니다."
        }
    }
    
    fun checkServiceHealth(): Boolean = runBlocking {
        try {
            newsRepository.checkApiHealth()
        } catch (e: Exception) {
            logger.error("News service health check failed", e)
            false
        }
    }
    
    // 검증 메소드들
    private fun validateHeadlinesRequest(request: NewsHeadlinesRequest) {
        if (request.pageSize < 1 || request.pageSize > 100) {
            throw BusinessException("페이지 크기는 1-100 사이여야 합니다", "INVALID_PAGE_SIZE")
        }
    }
    
    private fun validateSearchRequest(request: NewsSearchRequest) {
        if (request.query.isBlank()) {
            throw BusinessException("검색 키워드가 필요합니다", "QUERY_REQUIRED")
        }
        if (request.query.length > 500) {
            throw BusinessException("검색 키워드가 너무 깁니다", "QUERY_TOO_LONG")
        }
        if (request.pageSize < 1 || request.pageSize > 100) {
            throw BusinessException("페이지 크기는 1-100 사이여야 합니다", "INVALID_PAGE_SIZE")
        }
    }
    
    private fun validateSourceRequest(request: NewsBySourceRequest) {
        if (request.source.isBlank()) {
            throw BusinessException("출처가 필요합니다", "SOURCE_REQUIRED")
        }
        if (request.pageSize < 1 || request.pageSize > 100) {
            throw BusinessException("페이지 크기는 1-100 사이여야 합니다", "INVALID_PAGE_SIZE")
        }
    }
}

