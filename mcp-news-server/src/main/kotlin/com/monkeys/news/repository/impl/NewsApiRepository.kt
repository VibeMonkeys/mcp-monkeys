package com.monkeys.news.repository.impl

import com.monkeys.news.repository.NewsRepository
import com.monkeys.shared.dto.*
import com.monkeys.shared.util.ApiClient
import com.monkeys.shared.util.ApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.*
import java.net.URLEncoder

/**
 * NewsAPI를 통한 뉴스 데이터 조회 구현체
 * Repository 패턴 - 외부 API와의 데이터 액세스 담당
 * 공통 ApiClient 사용으로 HTTP 처리 표준화
 */
@Repository
@Profile("external-api")
class NewsApiRepository(
    @Value("\${news.api.key:dummy-key}") private val apiKey: String,
    newsHttpClient: OkHttpClient,
    meterRegistry: MeterRegistry
) : NewsRepository {
    
    private val logger = LoggerFactory.getLogger(NewsApiRepository::class.java)
    private val baseUrl = "https://newsapi.org/v2"
    
    // 공통 ApiClient 사용으로 HTTP 처리 표준화
    private val apiClient = ApiClient(newsHttpClient, meterRegistry, "news")
    
    override suspend fun getTopHeadlines(request: NewsHeadlinesRequest): List<NewsArticle> = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val url = "$baseUrl/top-headlines?country=${request.country}&category=${request.category}&pageSize=${request.pageSize}&apiKey=$apiKey"
        val newsData = apiClient.get(url).getDataOrThrow()
        
        parseNewsArticles(newsData)
    }
    
    override suspend fun searchNews(request: NewsSearchRequest): List<NewsArticle> = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val encodedQuery = URLEncoder.encode(request.query, "UTF-8")
        val url = buildString {
            append("$baseUrl/everything?q=$encodedQuery")
            append("&sortBy=${request.sortBy}")
            append("&language=${request.language}")
            append("&pageSize=${request.pageSize}")
            request.fromDate?.let { append("&from=$it") }
            request.toDate?.let { append("&to=$it") }
            append("&apiKey=$apiKey")
        }
        
        val newsData = apiClient.get(url).getDataOrThrow()
        parseNewsArticles(newsData)
    }
    
    override suspend fun getNewsBySource(request: NewsBySourceRequest): List<NewsArticle> = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val url = "$baseUrl/everything?sources=${request.source}&pageSize=${request.pageSize}&apiKey=$apiKey"
        val newsData = apiClient.get(url).getDataOrThrow()
        
        parseNewsArticles(newsData)
    }
    
    override suspend fun checkApiHealth(): Boolean = withContext(Dispatchers.IO) {
        if (apiKey == "dummy-key") return@withContext false
        
        try {
            val url = "$baseUrl/top-headlines?country=kr&category=general&pageSize=1&apiKey=$apiKey"
            apiClient.get(url).getDataOrThrow()
            true
        } catch (e: Exception) {
            logger.error("News API health check failed", e)
            false
        }
    }
    
    private fun validateApiKey() {
        if (apiKey == "dummy-key") {
            throw ApiException("API 키가 설정되지 않음", "MISSING_API_KEY")
        }
    }
    
    /**
     * NewsAPI 응답을 NewsArticle 리스트로 변환
     */
    private fun parseNewsArticles(newsData: Map<String, Any>): List<NewsArticle> {
        val articles = newsData["articles"] as? List<Map<String, Any>> ?: emptyList()
        
        return articles.map { article ->
            NewsArticle(
                title = article["title"] as? String ?: "제목 없음",
                description = article["description"] as? String ?: "",
                author = article["author"] as? String ?: "작성자 미상",
                source = (article["source"] as? Map<String, Any>)?.get("name") as? String ?: "출처 미상",
                url = article["url"] as? String ?: "",
                urlToImage = article["urlToImage"] as? String ?: "",
                publishedAt = article["publishedAt"] as? String ?: "",
                content = article["content"] as? String ?: ""
            )
        }
    }
    
    /**
     * 에러 발생 시 기본 뉴스 정보 생성
     */
    private fun createErrorNewsArticle(errorMessage: String, type: String): NewsArticle {
        return NewsArticle(
            title = "뉴스 조회 오류 ($type)",
            description = "뉴스를 조회하는 중 오류가 발생했습니다: $errorMessage",
            author = "시스템",
            source = "MCP News Service",
            url = "",
            urlToImage = "",
            publishedAt = java.time.Instant.now().toString(),
            content = "뉴스 데이터를 불러올 수 없습니다. $errorMessage"
        )
    }
}