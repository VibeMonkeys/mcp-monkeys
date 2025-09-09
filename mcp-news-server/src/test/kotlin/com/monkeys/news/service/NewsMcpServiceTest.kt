package com.monkeys.news.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("NewsMcpService 단위 테스트")
class NewsMcpServiceTest {

    @Nested
    @DisplayName("더미 API 키로 테스트")
    inner class DummyApiKeyTest {

        @Test
        @DisplayName("최신 뉴스 헤드라인 조회 - API 키 미설정")
        fun `should return dummy headlines when api key is not set`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val headlines = service.getTopHeadlines("kr")
            
            // Then
            assertNotNull(headlines)
            assertEquals(1, headlines.size)
            
            val headline = headlines[0]
            assertEquals("테스트 뉴스 제목 (headlines)", headline.title)
            assertTrue(headline.description.contains("API 키가 설정되지 않았습니다"))
            assertEquals("테스트 작성자", headline.author)
            assertEquals("테스트 출처", headline.source)
        }

        @Test
        @DisplayName("뉴스 검색 - API 키 미설정")
        fun `should return dummy search results when api key is not set`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val results = service.searchNews("테스트")
            
            // Then
            assertNotNull(results)
            assertEquals(1, results.size)
            
            val result = results[0]
            assertEquals("테스트 뉴스 제목 (search)", result.title)
            assertTrue(result.description.contains("News API 키가 설정되지 않았습니다"))
            assertEquals("테스트 작성자", result.author)
            assertEquals("테스트 출처", result.source)
        }

        @Test
        @DisplayName("출처별 뉴스 조회 - API 키 미설정")
        fun `should return dummy source news when api key is not set`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val sourceNews = service.getNewsBySource("cnn")
            
            // Then
            assertNotNull(sourceNews)
            assertEquals(1, sourceNews.size)
            
            val news = sourceNews[0]
            assertEquals("테스트 뉴스 제목 (source)", news.title)
            assertTrue(news.description.contains("News API 키가 설정되지 않았습니다"))
            assertEquals("테스트 작성자", news.author)
            assertEquals("테스트 출처", news.source)
        }
    }

    @Nested
    @DisplayName("매개변수 테스트")
    inner class ParameterTest {

        @Test
        @DisplayName("다양한 국가 코드로 헤드라인 조회")
        fun `should handle different country codes`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val krHeadlines = service.getTopHeadlines("kr", "business", 5)
            val usHeadlines = service.getTopHeadlines("us", "technology", 10)
            
            // Then
            assertEquals(1, krHeadlines.size)
            assertEquals(1, usHeadlines.size)
        }

        @Test
        @DisplayName("다양한 검색 매개변수 처리")
        fun `should handle different search parameters`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val relevancyResults = service.searchNews("AI", "relevancy", "en", 20)
            val popularityResults = service.searchNews("코로나", "popularity", "ko", 15)
            
            // Then
            assertEquals(1, relevancyResults.size)
            assertEquals(1, popularityResults.size)
        }

        @Test
        @DisplayName("다양한 출처 도메인 처리")
        fun `should handle different source domains`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val cnnNews = service.getNewsBySource("cnn", 10)
            val bbcNews = service.getNewsBySource("bbc-news", 5)
            val techcrunchNews = service.getNewsBySource("techcrunch", 15)
            
            // Then
            assertEquals(1, cnnNews.size)
            assertEquals(1, bbcNews.size)
            assertEquals(1, techcrunchNews.size)
        }
    }

    @Nested
    @DisplayName("데이터 검증 테스트")
    inner class DataValidationTest {

        @Test
        @DisplayName("뉴스 데이터 구조 검증")
        fun `should return proper news article structure`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val headlines = service.getTopHeadlines()
            
            // Then
            val article = headlines[0]
            assertNotNull(article.title)
            assertNotNull(article.description)
            assertNotNull(article.author)
            assertNotNull(article.source)
            assertNotNull(article.url)
            assertNotNull(article.urlToImage)
            assertNotNull(article.publishedAt)
            assertNotNull(article.content)
        }

        @Test
        @DisplayName("빈 검색어 처리")
        fun `should handle empty search query gracefully`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val emptyResults = service.searchNews("")
            val blankResults = service.searchNews("   ")
            
            // Then
            assertEquals(1, emptyResults.size)
            assertEquals(1, blankResults.size)
        }

        @Test
        @DisplayName("특수문자 포함 검색어 처리")
        fun `should handle special characters in search query`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val specialCharResults = service.searchNews("코로나19 & 백신 100% 효과")
            val urlEncodingResults = service.searchNews("https://example.com?param=test")
            
            // Then
            assertEquals(1, specialCharResults.size)
            assertEquals(1, urlEncodingResults.size)
        }

        @Test
        @DisplayName("기본값 매개변수 테스트")
        fun `should use default parameters correctly`() {
            // Given
            val service = NewsMcpService(
                "dummy-key", 
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val defaultHeadlines = service.getTopHeadlines()
            val defaultSearch = service.searchNews("test")
            val defaultSourceNews = service.getNewsBySource("cnn")
            
            // Then
            assertEquals(1, defaultHeadlines.size)
            assertEquals(1, defaultSearch.size)
            assertEquals(1, defaultSourceNews.size)
        }
    }
}