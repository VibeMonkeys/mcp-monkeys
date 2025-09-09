package com.monkeys.translate.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("TranslateMcpService 단위 테스트")
class TranslateMcpServiceTest {

    @Nested
    @DisplayName("더미 API 키로 테스트")
    inner class DummyApiKeyTest {

        @Test
        @DisplayName("텍스트 번역 - API 키 미설정")
        fun `should return dummy translation when api key is not set`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.translateText("Hello World", "en", "ko")
            
            // Then
            assertNotNull(result)
            assertEquals("Hello World", result.originalText)
            assertEquals("테스트 번역 결과: Hello World", result.translatedText)
            assertEquals("en", result.sourceLang)
            assertEquals("ko", result.targetLang)
            assertEquals("libre", result.service)
            assertTrue(result.error!!.contains("LibreTranslate API 키가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("언어 감지 - API 키 미설정")
        fun `should return dummy language detection when api key is not set`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.detectLanguage("안녕하세요")
            
            // Then
            assertNotNull(result)
            assertEquals("안녕하세요", result.text)
            assertEquals("ko", result.detectedLanguage)
            assertEquals(0.5, result.confidence)
            assertEquals("dummy", result.service)
            assertNotNull(result.error)
        }

        @Test
        @DisplayName("지원 언어 목록 조회 - API 키 미설정")
        fun `should return dummy supported languages when api key is not set`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val languages = service.getSupportedLanguages()
            
            // Then
            assertNotNull(languages)
            assertEquals(3, languages.size)
            assertEquals("ko", languages[0].code)
            assertEquals("한국어", languages[0].name)
            assertEquals("dummy", languages[0].service)
        }

        @Test
        @DisplayName("일괄 번역 - API 키 미설정")
        fun `should return dummy batch translation when api key is not set`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val results = service.batchTranslate("Hello, Good Morning, Thank you", "en", "ko")
            
            // Then
            assertNotNull(results)
            assertEquals(3, results.size)
            results.forEach { result ->
                assertTrue(result.translatedText.startsWith("테스트 번역 결과:"))
                assertEquals("libre", result.service)
                assertNotNull(result.error)
            }
        }
    }

    @Nested
    @DisplayName("매개변수 처리 테스트")
    inner class ParameterTest {

        @Test
        @DisplayName("서비스 선택 로직 - Google API 키 있을 때")
        fun `should prefer google when google api key is available`() {
            // Given
            val service = TranslateMcpService(
                "real-google-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.translateText("Hello", "en", "ko", "auto")
            
            // Then
            assertNotNull(result)
            assertEquals("google", result.service)
        }

        @Test
        @DisplayName("서비스 강제 선택 - LibreTranslate")
        fun `should use libre when explicitly specified`() {
            // Given
            val service = TranslateMcpService(
                "real-google-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.translateText("Hello", "en", "ko", "libre")
            
            // Then
            assertNotNull(result)
            assertEquals("libre", result.service)
        }

        @Test
        @DisplayName("빈 텍스트 처리")
        fun `should handle empty text gracefully`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val emptyResult = service.translateText("", "en", "ko")
            val blankResult = service.translateText("   ", "en", "ko")
            
            // Then
            assertEquals("", emptyResult.translatedText)
            assertEquals("none", emptyResult.service)
            assertEquals("빈 텍스트는 번역할 수 없습니다", emptyResult.error)
            
            assertEquals("   ", blankResult.translatedText)
            assertEquals("none", blankResult.service)
        }

        @Test
        @DisplayName("언어 감지 - 빈 텍스트")
        fun `should handle empty text for language detection`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.detectLanguage("")
            
            // Then
            assertEquals("unknown", result.detectedLanguage)
            assertEquals(0.0, result.confidence)
            assertEquals("none", result.service)
            assertEquals("빈 텍스트는 분석할 수 없습니다", result.error)
        }
    }

    @Nested
    @DisplayName("일괄 번역 기능 테스트")
    inner class BatchTranslationTest {

        @Test
        @DisplayName("쉼표로 구분된 텍스트 처리")
        fun `should split comma separated texts correctly`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val results = service.batchTranslate("Hello, World, Test", "en", "ko")
            
            // Then
            assertEquals(3, results.size)
            assertEquals("Hello", results[0].originalText)
            assertEquals("World", results[1].originalText) 
            assertEquals("Test", results[2].originalText)
        }

        @Test
        @DisplayName("공백이 포함된 텍스트 처리")
        fun `should handle whitespace in batch texts`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val results = service.batchTranslate(" Hello , World ,  Test  ", "en", "ko")
            
            // Then
            assertEquals(3, results.size)
            assertEquals("Hello", results[0].originalText)
            assertEquals("World", results[1].originalText)
            assertEquals("Test", results[2].originalText)
        }

        @Test
        @DisplayName("빈 일괄 번역 텍스트 처리")
        fun `should handle empty batch translation input`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val emptyResults = service.batchTranslate("", "en", "ko")
            val whitespaceResults = service.batchTranslate(" , , ", "en", "ko")
            
            // Then
            assertEquals(1, emptyResults.size)
            assertEquals("유효한 텍스트가 없습니다", emptyResults[0].error)
            
            assertEquals(1, whitespaceResults.size)
            assertEquals("유효한 텍스트가 없습니다", whitespaceResults[0].error)
        }
    }

    @Nested
    @DisplayName("데이터 구조 검증 테스트")
    inner class DataStructureTest {

        @Test
        @DisplayName("번역 결과 데이터 구조 확인")
        fun `should return proper translation result structure`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.translateText("Hello", "en", "ko")
            
            // Then
            assertNotNull(result.originalText)
            assertNotNull(result.translatedText)
            assertNotNull(result.sourceLang)
            assertNotNull(result.targetLang)
            assertNotNull(result.service)
            assertTrue(result.confidence >= 0.0)
        }

        @Test
        @DisplayName("언어 감지 결과 데이터 구조 확인")
        fun `should return proper language detection result structure`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val result = service.detectLanguage("Hello World")
            
            // Then
            assertNotNull(result.text)
            assertNotNull(result.detectedLanguage)
            assertNotNull(result.service)
            assertTrue(result.confidence >= 0.0)
        }

        @Test
        @DisplayName("지원 언어 목록 데이터 구조 확인")
        fun `should return proper supported languages structure`() {
            // Given
            val service = TranslateMcpService(
                "dummy-key", 
                "https://libretranslate.de", 
                "",
                OkHttpClient(),
                SimpleMeterRegistry()
            )
            
            // When
            val languages = service.getSupportedLanguages()
            
            // Then
            languages.forEach { lang ->
                assertNotNull(lang.code)
                assertNotNull(lang.name)
                assertNotNull(lang.service)
                assertTrue(lang.code.isNotEmpty())
                assertTrue(lang.name.isNotEmpty())
            }
        }
    }
}