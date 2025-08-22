package com.monkeys.news.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import okhttp3.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory

@Service
class NewsMcpService(
    @Value("\${news.api.key:dummy-key}") private val apiKey: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(NewsMcpService::class.java)
    private val baseUrl = "https://newsapi.org/v2"
    private val maxRetries = 3

    @Tool(description = "최신 뉴스 헤드라인을 조회합니다")
    fun getTopHeadlines(
        @ToolParam(description = "국가 코드 (kr, us, jp 등)")
        country: String = "kr",
        @ToolParam(description = "카테고리 (business, entertainment, general, health, science, sports, technology)")
        category: String = "general",
        @ToolParam(description = "조회할 뉴스 개수")
        pageSize: Int = 10
    ): List<NewsArticle> {
        if (apiKey == "dummy-key") {
            return listOf(createDummyNews("News API 키가 설정되지 않았습니다. NEWS_API_KEY 환경변수를 설정해주세요."))
        }
        
        return try {
            val newsData = executeWithRetry {
                fetchNewsData("$baseUrl/top-headlines?country=$country&category=$category&pageSize=$pageSize&apiKey=$apiKey")
            }
            
            val articles = newsData["articles"] as? List<Map<String, Any>> ?: emptyList()
            
            articles.map { article ->
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
        } catch (e: Exception) {
            logger.error("뉴스 조회 중 오류", e)
            listOf(createDummyNews("뉴스 조회 중 오류: ${e.message}"))
        }
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
        if (apiKey == "dummy-key") {
            return listOf(createDummyNews("News API 키가 설정되지 않았습니다."))
        }
        
        return try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val newsData = executeWithRetry {
                fetchNewsData("$baseUrl/everything?q=$encodedQuery&sortBy=$sortBy&language=$language&pageSize=$pageSize&apiKey=$apiKey")
            }
            
            val articles = newsData["articles"] as? List<Map<String, Any>> ?: emptyList()
            
            articles.map { article ->
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
        } catch (e: Exception) {
            logger.error("뉴스 검색 중 오류", e)
            listOf(createDummyNews("뉴스 검색 중 오류: ${e.message}"))
        }
    }

    @Tool(description = "특정 출처의 뉴스를 조회합니다")
    fun getNewsBySource(
        @ToolParam(description = "뉴스 출처 (예: cnn, bbc-news, techcrunch)", required = true)
        source: String,
        @ToolParam(description = "조회할 뉴스 개수")
        pageSize: Int = 10
    ): List<NewsArticle> {
        if (apiKey == "dummy-key") {
            return listOf(createDummyNews("News API 키가 설정되지 않았습니다."))
        }
        
        return try {
            val newsData = executeWithRetry {
                fetchNewsData("$baseUrl/everything?sources=$source&pageSize=$pageSize&apiKey=$apiKey")
            }
            
            val articles = newsData["articles"] as? List<Map<String, Any>> ?: emptyList()
            
            articles.map { article ->
                NewsArticle(
                    title = article["title"] as? String ?: "제목 없음",
                    description = article["description"] as? String ?: "",
                    author = article["author"] as? String ?: "작성자 미상",
                    source = (article["source"] as? Map<String, Any>)?.get("name") as? String ?: source,
                    url = article["url"] as? String ?: "",
                    urlToImage = article["urlToImage"] as? String ?: "",
                    publishedAt = article["publishedAt"] as? String ?: "",
                    content = article["content"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            logger.error("출처별 뉴스 조회 중 오류", e)
            listOf(createDummyNews("출처별 뉴스 조회 중 오류: ${e.message}"))
        }
    }

    private fun createDummyNews(errorMessage: String) = NewsArticle(
        title = "테스트 뉴스 (설정 필요)",
        description = errorMessage,
        author = "시스템",
        source = "테스트",
        url = "https://example.com",
        urlToImage = "",
        publishedAt = "2024-01-01T00:00:00Z",
        content = errorMessage
    )
    
    private fun <T> executeWithRetry(operation: () -> T): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logger.warn("뉴스 API 호출 시도 ${attempt + 1}/$maxRetries 실패: ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    val delay = (1000 * Math.pow(2.0, attempt.toDouble())).toLong()
                    Thread.sleep(delay)
                }
            }
        }
        
        throw lastException ?: RuntimeException("뉴스 API 호출이 $maxRetries 번 모두 실패했습니다")
    }
    
    private fun fetchNewsData(url: String): Map<String, Any> {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "MCP-Monkeys-News/1.0")
            .addHeader("X-Api-Key", apiKey)
            .build()

        client.newCall(request).execute().use { response ->
            when (response.code) {
                200 -> {
                    val jsonResponse = response.body?.string() ?: "{}"
                    return mapper.readValue(jsonResponse)
                }
                400 -> throw IllegalArgumentException("잘못된 요청입니다. 파라미터를 확인해주세요.")
                401 -> throw IllegalArgumentException("잘못된 API 키입니다. NEWS_API_KEY를 확인해주세요.")
                426 -> throw RuntimeException("뉴스 API 업그레이드가 필요합니다. 개발자 플랜으로 업그레이드하세요.")
                429 -> throw RuntimeException("API 사용량 한도를 초과했습니다. 잠시 후 다시 시도해주세요.")
                500 -> throw RuntimeException("뉴스 서비스 일시 장애입니다. 잠시 후 다시 시도해주세요.")
                else -> throw RuntimeException("예상치 못한 오류입니다: ${response.code} ${response.message}")
            }
        }
    }
}

data class NewsArticle(
    val title: String,
    val description: String,
    val author: String,
    val source: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: String,
    val content: String
)