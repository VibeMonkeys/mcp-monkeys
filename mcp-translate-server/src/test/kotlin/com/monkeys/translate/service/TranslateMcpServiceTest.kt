package com.monkeys.translate.service

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

@DisplayName("TranslateMcpService 단위 테스트")
class TranslateMcpServiceTest {

    private lateinit var translateService: TranslateService
    private lateinit var translateMcpService: TranslateMcpService

    @BeforeEach
    fun setUp() {
        translateService = mockk()
        translateMcpService = TranslateMcpService(translateService)
    }

    @Nested
    @DisplayName("텍스트 번역 테스트")
    inner class TranslateTextTest {

        @Test
        @DisplayName("텍스트 번역 성공")
        fun `should translate text successfully`() {
            // Given
            val expectedResult = TranslationResult(
                originalText = "Hello World",
                translatedText = "안녕하세요 세계",
                sourceLanguage = "en",
                targetLanguage = "ko",
                confidenceScore = 0.95,
                detectedLanguage = null
            )
            every { translateService.translateText(any()) } returns expectedResult

            // When
            val result = translateMcpService.translateText("Hello World", "en", "ko")

            // Then
            assertNotNull(result)
            assertEquals("Hello World", result.originalText)
            assertEquals("안녕하세요 세계", result.translatedText)
            assertEquals("en", result.sourceLanguage)
            assertEquals("ko", result.targetLanguage)
            verify { translateService.translateText(any()) }
        }

        @Test
        @DisplayName("자동 언어 감지 번역")
        fun `should translate with auto language detection`() {
            // Given
            val expectedResult = TranslationResult(
                originalText = "Bonjour",
                translatedText = "안녕하세요",
                sourceLanguage = "fr",
                targetLanguage = "ko",
                confidenceScore = 0.9,
                detectedLanguage = "fr"
            )
            every { translateService.translateText(any()) } returns expectedResult

            // When
            val result = translateMcpService.translateText("Bonjour", "auto", "ko")

            // Then
            assertNotNull(result)
            assertEquals("fr", result.sourceLanguage)
            assertEquals("fr", result.detectedLanguage)
        }
    }

    @Nested
    @DisplayName("언어 감지 테스트")
    inner class DetectLanguageTest {

        @Test
        @DisplayName("언어 감지 성공")
        fun `should detect language successfully`() {
            // Given
            val expectedResult = LanguageDetectionResult(
                languageCode = "ko",
                languageName = "Korean",
                confidence = 0.98
            )
            every { translateService.detectLanguage(any()) } returns expectedResult

            // When
            val result = translateMcpService.detectLanguage("안녕하세요")

            // Then
            assertNotNull(result)
            assertEquals("ko", result.languageCode)
            assertEquals("Korean", result.languageName)
            assertEquals(0.98, result.confidence)
            verify { translateService.detectLanguage(any()) }
        }

        @Test
        @DisplayName("영어 언어 감지")
        fun `should detect English language`() {
            // Given
            val expectedResult = LanguageDetectionResult(
                languageCode = "en",
                languageName = "English",
                confidence = 0.95
            )
            every { translateService.detectLanguage(any()) } returns expectedResult

            // When
            val result = translateMcpService.detectLanguage("Hello World")

            // Then
            assertNotNull(result)
            assertEquals("en", result.languageCode)
        }
    }

    @Nested
    @DisplayName("지원 언어 목록 테스트")
    inner class GetSupportedLanguagesTest {

        @Test
        @DisplayName("지원 언어 목록 조회 성공")
        fun `should get supported languages successfully`() {
            // Given
            val expectedLanguages = listOf(
                SupportedLanguage(
                    code = "ko",
                    name = "Korean",
                    nameKo = "한국어",
                    nativeName = "한국어"
                ),
                SupportedLanguage(
                    code = "en",
                    name = "English",
                    nameKo = "영어",
                    nativeName = "English"
                ),
                SupportedLanguage(
                    code = "ja",
                    name = "Japanese",
                    nameKo = "일본어",
                    nativeName = "日本語"
                )
            )
            every { translateService.getSupportedLanguages() } returns expectedLanguages

            // When
            val result = translateMcpService.getSupportedLanguages()

            // Then
            assertNotNull(result)
            assertEquals(3, result.size)
            assertEquals("ko", result[0].code)
            assertEquals("Korean", result[0].name)
            verify { translateService.getSupportedLanguages() }
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
                originalText = "테스트",
                translatedText = "Test",
                sourceLanguage = "ko",
                targetLanguage = "en",
                confidenceScore = 0.95,
                detectedLanguage = "ko"
            )

            // Then
            assertEquals("테스트", result.originalText)
            assertEquals("Test", result.translatedText)
            assertEquals("ko", result.sourceLanguage)
            assertEquals("en", result.targetLanguage)
            assertEquals(0.95, result.confidenceScore)
            assertEquals("ko", result.detectedLanguage)
        }

        @Test
        @DisplayName("LanguageDetectionResult 데이터 구조")
        fun `should have correct LanguageDetectionResult structure`() {
            // Given
            val result = LanguageDetectionResult(
                languageCode = "zh",
                languageName = "Chinese",
                confidence = 0.88
            )

            // Then
            assertEquals("zh", result.languageCode)
            assertEquals("Chinese", result.languageName)
            assertEquals(0.88, result.confidence)
        }

        @Test
        @DisplayName("SupportedLanguage 데이터 구조")
        fun `should have correct SupportedLanguage structure`() {
            // Given
            val language = SupportedLanguage(
                code = "fr",
                name = "French",
                nameKo = "프랑스어",
                nativeName = "Français"
            )

            // Then
            assertEquals("fr", language.code)
            assertEquals("French", language.name)
            assertEquals("프랑스어", language.nameKo)
            assertEquals("Français", language.nativeName)
        }
    }
}
