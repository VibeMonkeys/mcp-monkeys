package com.monkeys.news.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import okhttp3.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

@Service
class NewsMcpService(
    @Value("\${news.api.key:dummy-key}") private val apiKey: String,
    private val newsHttpClient: OkHttpClient,
    private val meterRegistry: MeterRegistry
) {
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(NewsMcpService::class.java)
    private val baseUrl = "https://newsapi.org/v2"
    private val maxRetries = 3
    
    // 메트릭 타이머
    private val newsApiTimer = Timer.builder("news.api.request")
        .description("News API 요청 시간")
        .register(meterRegistry)

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
            logger.warn("News API 호출 실패: API 키가 설정되지 않음")
            return listOf(createDummyNews("API 키가 설정되지 않았습니다.", "headlines"))
        }
        
        logger.info("뉴스 헤드라인 조회: country={}, category={}, pageSize={}, apiKey={}", 
            country, category, pageSize, maskApiKey(apiKey))
        
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
        } catch (e: NewsApiException) {
            logger.error("News API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("news.service.error", "type", e.errorCode).increment()
            listOf(createDummyNews("뉴스 조회 실패: ${e.message}", "headlines"))
        } catch (e: Exception) {
            logger.error("예상치 못한 오류", e)
            meterRegistry.counter("news.service.error", "type", "UNEXPECTED").increment()
            listOf(createDummyNews("일시적 오류가 발생했습니다", "headlines"))
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
            return listOf(createDummyNews("News API 키가 설정되지 않았습니다.", "search"))
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
        } catch (e: NewsApiException) {
            logger.error("News Search API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("news.search.error", "type", e.errorCode).increment()
            listOf(createDummyNews("뉴스 검색 실패: ${e.message}", "search"))
        } catch (e: Exception) {
            logger.error("예상치 못한 검색 오류", e)
            meterRegistry.counter("news.search.error", "type", "UNEXPECTED").increment()
            listOf(createDummyNews("일시적 오류가 발생했습니다", "search"))
        }
    }

    @Tool(description = "특정 출처의 뉴스를 조회합니다")
    fun getNewsBySource(
        @ToolParam(description = "출처 도메인 (예: cnn.com, bbc-news, techcrunch)", required = true)
        source: String,
        @ToolParam(description = "조회할 뉴스 개수")
        pageSize: Int = 10
    ): List<NewsArticle> {
        if (apiKey == "dummy-key") {
            return listOf(createDummyNews("News API 키가 설정되지 않았습니다.", "source"))
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
                    source = (article["source"] as? Map<String, Any>)?.get("name") as? String ?: "출처 미상",
                    url = article["url"] as? String ?: "",
                    urlToImage = article["urlToImage"] as? String ?: "",
                    publishedAt = article["publishedAt"] as? String ?: "",
                    content = article["content"] as? String ?: ""
                )
            }
        } catch (e: NewsApiException) {
            logger.error("News Source API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("news.source.error", "type", e.errorCode).increment()
            listOf(createDummyNews("특정 출처 뉴스 조회 실패: ${e.message}", "source"))
        } catch (e: Exception) {
            logger.error("예상치 못한 출처 조회 오류", e)
            meterRegistry.counter("news.source.error", "type", "UNEXPECTED").increment()
            listOf(createDummyNews("일시적 오류가 발생했습니다", "source"))
        }
    }

    // 더미 데이터 생성 함수
    private fun createDummyNews(errorMessage: String, type: String) = NewsArticle(
        title = "테스트 뉴스 제목 ($type)",
        description = "테스트 뉴스 내용입니다. $errorMessage",
        author = "테스트 작성자",
        source = "테스트 출처",
        url = "https://example.com/test-news",
        urlToImage = "",
        publishedAt = "2024-01-01T12:00:00Z",
        content = "테스트 뉴스 전체 내용입니다."
    )
    
    // 재시도 로직을 포함한 API 호출
    private fun <T> executeWithRetry(operation: () -> T): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logger.warn("API 호출 시도 ${attempt + 1}/$maxRetries 실패: ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    // 지수 백오프: 1초, 2초, 4초
                    val delay = (1000 * Math.pow(2.0, attempt.toDouble())).toLong()
                    Thread.sleep(delay)
                }
            }
        }
        
        throw lastException ?: RuntimeException("API 호출이 $maxRetries 번 모두 실패했습니다")
    }
    
    // HTTP 요청 실행 및 JSON 파싱 (메트릭 포함)
    private fun fetchNewsData(url: String): Map<String, Any> {
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "MCP-Monkeys-News/1.0")
            .build()

        return newsApiTimer.recordCallable {
            newsHttpClient.newCall(request).execute().use { response ->
                when (response.code) {
                    200 -> {
                        val jsonResponse = response.body?.string() ?: "{}"
                        meterRegistry.counter("news.api.success").increment()
                        mapper.readValue(jsonResponse)
                    }
                    401 -> {
                        meterRegistry.counter("news.api.error", "type", "auth").increment()
                        throw NewsApiException("인증 실패", "INVALID_API_KEY")
                    }
                    429 -> {
                        meterRegistry.counter("news.api.error", "type", "rate_limit").increment()
                        throw NewsApiException("API 사용량 한도 초과", "RATE_LIMIT_EXCEEDED")
                    }
                    426 -> {
                        meterRegistry.counter("news.api.error", "type", "upgrade_required").increment()
                        throw NewsApiException("요금제 업그레이드 필요", "UPGRADE_REQUIRED")
                    }
                    500, 502, 503, 504 -> {
                        meterRegistry.counter("news.api.error", "type", "server_error").increment()
                        throw NewsApiException("서버 오류", "SERVER_ERROR")
                    }
                    else -> {
                        meterRegistry.counter("news.api.error", "type", "unknown").increment()
                        throw NewsApiException("알 수 없는 오류", "UNKNOWN_ERROR")
                    }
                }
            }
        } ?: throw NewsApiException("응답 파싱 실패", "PARSE_ERROR")
    }
    
    // API 키 마스킹 유틸리티
    private fun maskApiKey(apiKey: String): String {
        return if (apiKey.length > 8) {
            "${apiKey.take(4)}****${apiKey.takeLast(4)}"
        } else {
            "****"
        }
    }
}

// 커스텀 예외 클래스
class NewsApiException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

// 뉴스 정보 데이터 클래스
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