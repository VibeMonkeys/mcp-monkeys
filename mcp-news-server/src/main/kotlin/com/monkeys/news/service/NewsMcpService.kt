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
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(NewsMcpService::class.java)
    private val baseUrl = "https://newsapi.org/v2"

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
            val url = "$baseUrl/top-headlines?country=$country&category=$category&pageSize=$pageSize&apiKey=$apiKey"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                logger.error("뉴스 API 호출 실패: ${response.code} ${response.message}")
                return listOf(createDummyNews("뉴스를 가져올 수 없습니다: ${response.code}"))
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val newsData: Map<String, Any> = mapper.readValue(jsonResponse)
            
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
            val url = "$baseUrl/everything?q=$encodedQuery&sortBy=$sortBy&language=$language&pageSize=$pageSize&apiKey=$apiKey"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                logger.error("뉴스 검색 API 호출 실패: ${response.code} ${response.message}")
                return listOf(createDummyNews("뉴스 검색을 할 수 없습니다: ${response.code}"))
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val newsData: Map<String, Any> = mapper.readValue(jsonResponse)
            
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
            val url = "$baseUrl/everything?sources=$source&pageSize=$pageSize&apiKey=$apiKey"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                logger.error("출처별 뉴스 API 호출 실패: ${response.code} ${response.message}")
                return listOf(createDummyNews("출처별 뉴스를 가져올 수 없습니다: ${response.code}"))
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val newsData: Map<String, Any> = mapper.readValue(jsonResponse)
            
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