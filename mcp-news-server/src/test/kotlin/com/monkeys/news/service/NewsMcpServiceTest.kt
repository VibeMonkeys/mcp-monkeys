package com.monkeys.news.service

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
            val service = NewsMcpService("dummy-key")
            
            // When
            val headlines = service.getTopHeadlines("kr")
            
            // Then
            assertNotNull(headlines)
            assertEquals(1, headlines.size)
            
            val headline = headlines[0]
            assertEquals("테스트 뉴스 (설정 필요)", headline.title)
            assertTrue(headline.description.contains("NEWS_API_KEY"))
            assertEquals("시스템", headline.author)
            assertEquals("https://example.com", headline.url)
            assertTrue(headline.publishedAt.isNotEmpty())
        }

        @Test
        @DisplayName("뉴스 검색 - API 키 미설정")
        fun `should return dummy search results when api key is not set`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val searchResults = service.searchNews("코로나", "ko", "popularity")
            
            // Then
            assertNotNull(searchResults)
            assertEquals(1, searchResults.size)
            
            val article = searchResults[0]
            assertEquals("테스트 뉴스 (설정 필요)", article.title)
            assertTrue(article.description.contains("News API 키가 설정되지 않았습니다"))
            assertTrue(article.content.contains("News API 키가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("특정 소스 뉴스 조회 - API 키 미설정")
        fun `should return dummy source news when api key is not set`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val sourceNews = service.getNewsBySource("bbc-news")
            
            // Then
            assertNotNull(sourceNews)
            assertEquals(1, sourceNews.size)
            
            val article = sourceNews[0]
            assertEquals("테스트 뉴스 (설정 필요)", article.title)
            assertTrue(article.description.contains("News API 키가 설정되지 않았습니다"))
        }
    }

    @Nested
    @DisplayName("파라미터 처리 테스트")
    inner class ParameterHandlingTest {

        @Test
        @DisplayName("최신 뉴스 - 다른 국가 코드")
        fun `should handle different country codes`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val usNews = service.getTopHeadlines("us")
            val jpNews = service.getTopHeadlines("jp")
            
            // Then
            assertNotNull(usNews)
            assertNotNull(jpNews)
            // 더미 데이터는 국가별로 다른 응답을 하지 않지만 호출이 성공해야 함
        }

        @Test
        @DisplayName("최신 뉴스 - 카테고리별")
        fun `should handle different categories`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val techNews = service.getTopHeadlines("kr", "technology")
            val sportsNews = service.getTopHeadlines("kr", "sports")
            
            // Then
            assertNotNull(techNews)
            assertNotNull(sportsNews)
            // 더미 데이터는 카테고리별로 다른 응답을 하지 않지만 호출이 성공해야 함
        }

        @Test
        @DisplayName("뉴스 검색 - 다른 정렬 방식")
        fun `should handle different sort orders`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val popularNews = service.searchNews("AI", "ko", "popularity")
            val relevantNews = service.searchNews("AI", "ko", "relevancy")
            val recentNews = service.searchNews("AI", "ko", "publishedAt")
            
            // Then
            // 더미 데이터에서는 모두 같은 결과를 반환하지만 파라미터가 전달되는지 확인
            assertNotNull(popularNews)
            assertNotNull(relevantNews)
            assertNotNull(recentNews)
        }

        @Test
        @DisplayName("뉴스 검색 - 다른 페이지 크기")
        fun `should handle different page sizes`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val smallPage = service.searchNews("테스트", "ko", "popularity", 5)
            val defaultPage = service.searchNews("테스트", "ko", "popularity")
            
            // Then
            assertNotNull(smallPage)
            assertNotNull(defaultPage)
            // 더미 데이터는 항상 1개를 반환하지만 실제 구현에서는 페이지 크기가 적용됨
        }
    }

    @Nested
    @DisplayName("데이터 구조 검증")
    inner class DataStructureTest {

        @Test
        @DisplayName("NewsArticle 데이터 구조")
        fun `should have correct NewsArticle structure`() {
            // Given
            val article = NewsArticle(
                title = "테스트 뉴스 제목",
                description = "테스트 뉴스 설명",
                content = "테스트 뉴스 전체 내용",
                author = "테스트 기자",
                source = "테스트 소스",
                url = "https://example.com/news/1",
                urlToImage = "https://example.com/image.jpg",
                publishedAt = "2024-01-01T12:00:00Z"
            )
            
            // Then
            assertEquals("테스트 뉴스 제목", article.title)
            assertEquals("테스트 뉴스 설명", article.description)
            assertEquals("테스트 뉴스 전체 내용", article.content)
            assertEquals("테스트 기자", article.author)
            assertEquals("테스트 소스", article.source)
            assertEquals("https://example.com/news/1", article.url)
            assertEquals("https://example.com/image.jpg", article.urlToImage)
            assertEquals("2024-01-01T12:00:00Z", article.publishedAt)
        }
    }

    @Nested
    @DisplayName("에러 처리 테스트")
    inner class ErrorHandlingTest {

        @Test
        @DisplayName("빈 검색어 처리")
        fun `should handle empty search query`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val results = service.searchNews("", "ko")
            
            // Then
            assertNotNull(results)
            assertTrue(results.isNotEmpty())
            // 더미 데이터는 빈 검색어도 결과를 반환
        }

        @Test
        @DisplayName("잘못된 언어 코드 처리")
        fun `should handle invalid language code`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val results = service.searchNews("test", "invalid-lang")
            
            // Then
            assertNotNull(results)
            // 서비스가 예외를 던지지 않고 더미 데이터를 반환해야 함
        }

        @Test
        @DisplayName("잘못된 소스명 처리")
        fun `should handle invalid source name`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val results = service.getNewsBySource("invalid-source-name")
            
            // Then
            assertNotNull(results)
            assertEquals(1, results.size)
            assertTrue(results[0].description.contains("News API 키가 설정되지 않았습니다"))
        }
    }

    @Nested
    @DisplayName("캐싱 및 성능 테스트")
    inner class PerformanceTest {

        @Test
        @DisplayName("연속 호출 처리")
        fun `should handle consecutive calls`() {
            // Given
            val service = NewsMcpService("dummy-key")
            
            // When
            val result1 = service.getTopHeadlines("kr")
            val result2 = service.getTopHeadlines("kr")
            val result3 = service.getTopHeadlines("kr")
            
            // Then
            assertNotNull(result1)
            assertNotNull(result2)
            assertNotNull(result3)
            
            // 모든 호출이 성공적으로 완료되어야 함
            assertEquals(result1.size, result2.size)
            assertEquals(result2.size, result3.size)
        }
    }
}