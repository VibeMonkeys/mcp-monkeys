package com.monkeys.news.service

import com.monkeys.shared.dto.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("NewsMcpService 단위 테스트")
class NewsMcpServiceTest {

    private lateinit var newsService: NewsService
    private lateinit var newsMcpService: NewsMcpService

    @BeforeEach
    fun setUp() {
        newsService = mockk()
        newsMcpService = NewsMcpService(newsService)
    }

    @Nested
    @DisplayName("헤드라인 조회 테스트")
    inner class GetTopHeadlinesTest {

        @Test
        @DisplayName("한국 뉴스 헤드라인 조회 성공")
        fun `should return top headlines for Korea`() {
            // Given
            val expectedArticles = listOf(
                NewsArticle(
                    title = "테스트 뉴스 제목",
                    description = "테스트 뉴스 설명",
                    url = "https://example.com/news/1",
                    urlToImage = "https://example.com/image.jpg",
                    publishedAt = "2024-01-01T12:00:00Z",
                    source = "Test Source",
                    author = "Test Author",
                    content = "테스트 뉴스 내용"
                )
            )
            every { newsService.getTopHeadlines(any()) } returns expectedArticles

            // When
            val result = newsMcpService.getTopHeadlines("kr", "general", 10)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("테스트 뉴스 제목", result[0].title)
            verify { newsService.getTopHeadlines(any()) }
        }

        @Test
        @DisplayName("다른 카테고리 뉴스 조회")
        fun `should handle different categories`() {
            // Given
            val expectedArticles = listOf(
                NewsArticle(
                    title = "기술 뉴스",
                    description = "기술 관련 뉴스",
                    url = "https://example.com/tech/1",
                    urlToImage = "",
                    publishedAt = "2024-01-01T12:00:00Z",
                    source = "Tech Source",
                    author = "Unknown",
                    content = "기술 뉴스 내용"
                )
            )
            every { newsService.getTopHeadlines(any()) } returns expectedArticles

            // When
            val result = newsMcpService.getTopHeadlines("us", "technology", 5)

            // Then
            assertNotNull(result)
            assertEquals("기술 뉴스", result[0].title)
        }
    }

    @Nested
    @DisplayName("뉴스 검색 테스트")
    inner class SearchNewsTest {

        @Test
        @DisplayName("키워드로 뉴스 검색 성공")
        fun `should search news by keyword`() {
            // Given
            val expectedArticles = listOf(
                NewsArticle(
                    title = "AI 관련 뉴스",
                    description = "인공지능 기술 발전",
                    url = "https://example.com/ai/1",
                    urlToImage = "https://example.com/ai.jpg",
                    publishedAt = "2024-01-01T12:00:00Z",
                    source = "AI News",
                    author = "AI Reporter",
                    content = "AI 뉴스 내용"
                )
            )
            every { newsService.searchNews(any()) } returns expectedArticles

            // When
            val result = newsMcpService.searchNews("AI", "relevancy", "ko", 10)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("AI 관련 뉴스", result[0].title)
            verify { newsService.searchNews(any()) }
        }
    }

    @Nested
    @DisplayName("출처별 뉴스 조회 테스트")
    inner class GetNewsBySourceTest {

        @Test
        @DisplayName("특정 출처 뉴스 조회 성공")
        fun `should get news by source`() {
            // Given
            val expectedArticles = listOf(
                NewsArticle(
                    title = "BBC 뉴스",
                    description = "BBC에서 보도한 뉴스",
                    url = "https://bbc.com/news/1",
                    urlToImage = "https://bbc.com/image.jpg",
                    publishedAt = "2024-01-01T12:00:00Z",
                    source = "BBC News",
                    author = "BBC Reporter",
                    content = "BBC 뉴스 내용"
                )
            )
            every { newsService.getNewsBySource(any()) } returns expectedArticles

            // When
            val result = newsMcpService.getNewsBySource("bbc-news", 10)

            // Then
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals("BBC News", result[0].source)
            verify { newsService.getNewsBySource(any()) }
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
                title = "테스트 제목",
                description = "테스트 설명",
                url = "https://example.com/news",
                urlToImage = "https://example.com/image.jpg",
                publishedAt = "2024-01-01T12:00:00Z",
                source = "Test Source",
                author = "Test Author",
                content = "테스트 내용"
            )

            // Then
            assertEquals("테스트 제목", article.title)
            assertEquals("테스트 설명", article.description)
            assertEquals("https://example.com/news", article.url)
            assertEquals("https://example.com/image.jpg", article.urlToImage)
            assertEquals("2024-01-01T12:00:00Z", article.publishedAt)
            assertEquals("Test Source", article.source)
            assertEquals("Test Author", article.author)
            assertEquals("테스트 내용", article.content)
        }
    }
}
