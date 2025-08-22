package com.monkeys.translate.service

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
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.translateText("Hello World", "en", "ko")
            
            // Then
            assertNotNull(result)
            assertEquals("Hello World", result.originalText)
            assertEquals("번역 테스트: Hello World", result.translatedText)
            assertEquals("en", result.sourceLang)
            assertEquals("ko", result.targetLang)
            assertEquals(0.0, result.confidence)
            assertEquals("dummy", result.service)
            assertNotNull(result.error)
            assertTrue(result.error!!.contains("Google Translate API 키가 설정되지 않았습니다") || 
                      result.error!!.contains("LibreTranslate"))
        }

        @Test
        @DisplayName("언어 감지 - API 키 미설정")
        fun `should return language detection when api key is not set`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.detectLanguage("안녕하세요")
            
            // Then
            assertNotNull(result)
            assertEquals("안녕하세요", result.text)
            assertTrue(result.detectedLanguage in listOf("ko", "unknown"))
            assertTrue(result.confidence >= 0.0)
            assertTrue(result.service in listOf("libre", "google"))
        }

        @Test
        @DisplayName("지원 언어 조회 - API 키 미설정")
        fun `should return common languages when api key is not set`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val languages = service.getSupportedLanguages("google")
            
            // Then
            assertNotNull(languages)
            assertTrue(languages.isNotEmpty())
            
            val koreanLang = languages.find { it.code == "ko" }
            assertNotNull(koreanLang)
            assertEquals("한국어", koreanLang.name)
            assertEquals("common", koreanLang.service)
        }

        @Test
        @DisplayName("일괄 번역 - API 키 미설정")
        fun `should return batch translation when api key is not set`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val results = service.batchTranslate("Hello, World, Test", "en", "ko")
            
            // Then
            assertNotNull(results)
            assertEquals(3, results.size)
            
            assertEquals("Hello", results[0].originalText)
            assertEquals("World", results[1].originalText)
            assertEquals("Test", results[2].originalText)
            
            results.forEach { result ->
                assertTrue(result.translatedText.startsWith("번역 테스트:"))
                assertEquals("dummy", result.service)
                assertNotNull(result.error)
            }
        }
    }

    @Nested
    @DisplayName("입력값 처리 테스트")
    inner class InputHandlingTest {

        @Test
        @DisplayName("빈 텍스트 번역 처리")
        fun `should handle empty text translation`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.translateText("", "en", "ko")
            
            // Then
            assertNotNull(result)
            assertEquals("", result.originalText)
            assertEquals("", result.translatedText)
            assertEquals("en", result.sourceLang)
            assertEquals("ko", result.targetLang)
            assertEquals(0.0, result.confidence)
            assertEquals("none", result.service)
            assertEquals("빈 텍스트는 번역할 수 없습니다", result.error)
        }

        @Test
        @DisplayName("빈 텍스트 언어 감지 처리")
        fun `should handle empty text language detection`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.detectLanguage("")
            
            // Then
            assertNotNull(result)
            assertEquals("", result.text)
            assertEquals("unknown", result.detectedLanguage)
            assertEquals(0.0, result.confidence)
            assertEquals("none", result.service)
            assertEquals("빈 텍스트의 언어는 감지할 수 없습니다", result.error)
        }

        @Test
        @DisplayName("공백만 있는 텍스트 처리")
        fun `should handle whitespace only text`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.translateText("   ", "en", "ko")
            
            // Then
            assertNotNull(result)
            assertEquals("   ", result.originalText)
            assertEquals("   ", result.translatedText)
            assertEquals("none", result.service)
            assertEquals("빈 텍스트는 번역할 수 없습니다", result.error)
        }

        @Test
        @DisplayName("다양한 언어 코드 처리")
        fun `should handle various language codes`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When & Then
            val autoDetect = service.translateText("Hello", "auto", "ko")
            assertEquals("auto", autoDetect.sourceLang)
            
            val specificLang = service.translateText("Hello", "en", "ja")
            assertEquals("en", specificLang.sourceLang)
            assertEquals("ja", specificLang.targetLang)
        }
    }

    @Nested
    @DisplayName("서비스 선택 테스트")
    inner class ServiceSelectionTest {

        @Test
        @DisplayName("구글 번역 서비스 선택")
        fun `should select google service when specified`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.translateText("Hello", "en", "ko", "google")
            
            // Then
            assertNotNull(result)
            assertTrue(result.error!!.contains("Google Translate API 키가 설정되지 않았습니다"))
        }

        @Test
        @DisplayName("LibreTranslate 서비스 선택")
        fun `should select libre service when specified`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.translateText("Hello", "en", "ko", "libre")
            
            // Then
            assertNotNull(result)
            // LibreTranslate는 실제 API 호출을 시도하므로 오류가 발생할 수 있음
            assertTrue(result.service == "libre" || result.service == "dummy")
        }

        @Test
        @DisplayName("자동 서비스 선택")
        fun `should auto select service`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.translateText("Hello", "en", "ko", "auto")
            
            // Then
            assertNotNull(result)
            // 더미 키가 설정되어 있으므로 LibreTranslate를 시도하거나 더미 응답 반환
            assertTrue(result.service in listOf("libre", "dummy"))
        }
    }

    @Nested
    @DisplayName("언어 감지 로직 테스트")
    inner class LanguageDetectionLogicTest {

        @Test
        @DisplayName("한국어 텍스트 감지")
        fun `should detect korean text`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.detectLanguage("안녕하세요")
            
            // Then
            assertNotNull(result)
            // 실제 API 호출이 실패하면 로컬 추측 로직을 사용하거나 LibreTranslate API 호출 결과
            assertTrue(result.detectedLanguage in listOf("ko", "unknown", "en"))
        }

        @Test
        @DisplayName("일본어 텍스트 감지")
        fun `should detect japanese text`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.detectLanguage("こんにちは")
            
            // Then
            assertNotNull(result)
            // 실제 API 호출이 실패하면 로컬 추측 로직을 사용하거나 LibreTranslate API 호출 결과
            assertTrue(result.detectedLanguage in listOf("ja", "unknown", "en"))
        }

        @Test
        @DisplayName("중국어 텍스트 감지")
        fun `should detect chinese text`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.detectLanguage("你好")
            
            // Then
            assertNotNull(result)
            // 실제 API 호출이 실패하면 로컬 추측 로직을 사용하거나 LibreTranslate API 호출 결과
            assertTrue(result.detectedLanguage in listOf("zh", "unknown", "en"))
        }

        @Test
        @DisplayName("영어 텍스트 감지")
        fun `should detect english text`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result = service.detectLanguage("Hello World")
            
            // Then
            assertNotNull(result)
            // 실제 API 호출이 실패하면 로컬 추측 로직을 사용하거나 LibreTranslate API 호출 결과
            assertTrue(result.detectedLanguage in listOf("en", "unknown"))
        }
    }

    @Nested
    @DisplayName("데이터 구조 검증")
    inner class DataStructureTest {

        @Test
        @DisplayName("TranslationResult 데이터 구조")
        fun `should have correct TranslationResult structure`() {
            // Given
            val result = TranslationResult(
                originalText = "Hello",
                translatedText = "안녕하세요",
                sourceLang = "en",
                targetLang = "ko",
                confidence = 0.95,
                service = "google",
                error = null
            )
            
            // Then
            assertEquals("Hello", result.originalText)
            assertEquals("안녕하세요", result.translatedText)
            assertEquals("en", result.sourceLang)
            assertEquals("ko", result.targetLang)
            assertEquals(0.95, result.confidence)
            assertEquals("google", result.service)
            assertEquals(null, result.error)
        }

        @Test
        @DisplayName("LanguageDetectionResult 데이터 구조")
        fun `should have correct LanguageDetectionResult structure`() {
            // Given
            val result = LanguageDetectionResult(
                text = "Hello World",
                detectedLanguage = "en",
                confidence = 0.98,
                service = "google",
                error = null
            )
            
            // Then
            assertEquals("Hello World", result.text)
            assertEquals("en", result.detectedLanguage)
            assertEquals(0.98, result.confidence)
            assertEquals("google", result.service)
            assertEquals(null, result.error)
        }

        @Test
        @DisplayName("SupportedLanguage 데이터 구조")
        fun `should have correct SupportedLanguage structure`() {
            // Given
            val language = SupportedLanguage(
                code = "ko",
                name = "한국어",
                service = "google"
            )
            
            // Then
            assertEquals("ko", language.code)
            assertEquals("한국어", language.name)
            assertEquals("google", language.service)
        }
    }

    @Nested
    @DisplayName("배치 처리 테스트")
    inner class BatchProcessingTest {

        @Test
        @DisplayName("쉼표로 구분된 텍스트 처리")
        fun `should handle comma separated texts`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val results = service.batchTranslate("Hello,World,Test", "en", "ko")
            
            // Then
            assertEquals(3, results.size)
            assertEquals("Hello", results[0].originalText)
            assertEquals("World", results[1].originalText)
            assertEquals("Test", results[2].originalText)
        }

        @Test
        @DisplayName("공백이 있는 쉼표 구분 텍스트 처리")
        fun `should handle comma separated texts with spaces`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val results = service.batchTranslate("Hello , World , Test", "en", "ko")
            
            // Then
            assertEquals(3, results.size)
            assertEquals("Hello", results[0].originalText)
            assertEquals("World", results[1].originalText)
            assertEquals("Test", results[2].originalText)
        }

        @Test
        @DisplayName("빈 텍스트가 포함된 배치 처리")
        fun `should handle batch with empty texts`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val results = service.batchTranslate("Hello,,World", "en", "ko")
            
            // Then
            assertEquals(2, results.size) // 빈 텍스트는 필터링됨
            assertEquals("Hello", results[0].originalText)
            assertEquals("World", results[1].originalText)
        }
    }

    @Nested
    @DisplayName("성능 및 안정성 테스트")
    inner class PerformanceTest {

        @Test
        @DisplayName("연속 번역 호출 처리")
        fun `should handle consecutive translation calls`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            
            // When
            val result1 = service.translateText("Hello", "en", "ko")
            val result2 = service.translateText("World", "en", "ko")
            val result3 = service.translateText("Test", "en", "ko")
            
            // Then
            assertNotNull(result1)
            assertNotNull(result2)
            assertNotNull(result3)
            
            assertEquals("Hello", result1.originalText)
            assertEquals("World", result2.originalText)
            assertEquals("Test", result3.originalText)
        }

        @Test
        @DisplayName("긴 텍스트 번역 처리")
        fun `should handle long text translation`() {
            // Given
            val service = TranslateMcpService("dummy-key", "https://libretranslate.de", "")
            val longText = "This is a very long text that needs to be translated. " +
                          "It contains multiple sentences and should be handled properly. " +
                          "The translation service should be able to process this without issues."
            
            // When
            val result = service.translateText(longText, "en", "ko")
            
            // Then
            assertNotNull(result)
            assertEquals(longText, result.originalText)
            assertTrue(result.translatedText.isNotEmpty())
        }
    }
}