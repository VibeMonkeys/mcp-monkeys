package com.monkeys.translate.repository.impl

import com.monkeys.translate.repository.TranslateRepository
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
 * Google Translate API를 통한 번역 서비스 구현체
 * Repository 패턴 - 외부 API와의 데이터 액세스 담당
 * 공통 ApiClient 사용으로 HTTP 처리 표준화
 */
@Repository
@Profile("external-api")
class GoogleTranslateRepository(
    @Value("\${google.translate.api.key:dummy-key}") private val apiKey: String,
    translateHttpClient: OkHttpClient,
    meterRegistry: MeterRegistry
) : TranslateRepository {
    
    private val logger = LoggerFactory.getLogger(GoogleTranslateRepository::class.java)
    private val baseUrl = "https://translation.googleapis.com/language/translate/v2"
    
    // 공통 ApiClient 사용으로 HTTP 처리 표준화
    private val apiClient = ApiClient(translateHttpClient, meterRegistry, "translate")
    
    override suspend fun translateText(request: TranslationRequest): TranslationResult = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val encodedText = URLEncoder.encode(request.text, "UTF-8")
        val url = buildString {
            append("$baseUrl?key=$apiKey")
            append("&q=$encodedText")
            append("&target=${request.targetLanguage}")
            if (request.sourceLanguage != "auto") {
                append("&source=${request.sourceLanguage}")
            }
            append("&format=text")
        }
        
        val response = apiClient.get(url).getDataOrThrow()
        parseTranslationResult(response, request)
    }
    
    override suspend fun detectLanguage(request: LanguageDetectionRequest): LanguageDetectionResult = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val encodedText = URLEncoder.encode(request.text, "UTF-8")
        val url = "$baseUrl/detect?key=$apiKey&q=$encodedText"
        
        val response = apiClient.get(url).getDataOrThrow()
        parseLanguageDetection(response)
    }
    
    override suspend fun getSupportedLanguages(): List<SupportedLanguage> = withContext(Dispatchers.IO) {
        validateApiKey()
        
        val url = "$baseUrl/languages?key=$apiKey&target=ko"
        val response = apiClient.get(url).getDataOrThrow()
        
        parseSupportedLanguages(response)
    }
    
    override suspend fun checkApiHealth(): Boolean = withContext(Dispatchers.IO) {
        if (apiKey == "dummy-key") return@withContext false
        
        try {
            val testRequest = TranslationRequest(
                text = "Hello",
                sourceLanguage = "en",
                targetLanguage = "ko"
            )
            translateText(testRequest)
            true
        } catch (e: Exception) {
            logger.error("Translate API health check failed", e)
            false
        }
    }
    
    private fun validateApiKey() {
        if (apiKey == "dummy-key") {
            throw ApiException("Google Translate API 키가 설정되지 않음", "MISSING_API_KEY")
        }
    }
    
    /**
     * Google Translate API 응답을 TranslationResult로 변환
     */
    private fun parseTranslationResult(response: Map<String, Any>, request: TranslationRequest): TranslationResult {
        val data = response["data"] as Map<String, Any>
        val translations = data["translations"] as List<Map<String, Any>>
        val translation = translations.first()
        
        return TranslationResult(
            originalText = request.text,
            translatedText = translation["translatedText"] as String,
            sourceLanguage = request.sourceLanguage,
            targetLanguage = request.targetLanguage,
            detectedLanguage = translation["detectedSourceLanguage"] as? String
        )
    }
    
    /**
     * 언어 감지 결과 파싱
     */
    private fun parseLanguageDetection(response: Map<String, Any>): LanguageDetectionResult {
        val data = response["data"] as Map<String, Any>
        val detections = data["detections"] as List<List<Map<String, Any>>>
        val detection = detections.first().first()
        
        return LanguageDetectionResult(
            languageCode = detection["language"] as String,
            languageName = getLanguageName(detection["language"] as String),
            confidence = (detection["confidence"] as Number).toDouble()
        )
    }
    
    /**
     * 지원 언어 목록 파싱
     */
    private fun parseSupportedLanguages(response: Map<String, Any>): List<SupportedLanguage> {
        val data = response["data"] as Map<String, Any>
        val languages = data["languages"] as List<Map<String, Any>>
        
        return languages.map { lang ->
            SupportedLanguage(
                code = lang["language"] as String,
                name = getLanguageName(lang["language"] as String),
                nameKo = lang["name"] as? String ?: getLanguageName(lang["language"] as String),
                nativeName = getLanguageName(lang["language"] as String)
            )
        }
    }
    
    /**
     * 언어 코드에서 언어명 얻기 (간단한 매핑)
     */
    private fun getLanguageName(code: String): String {
        return when (code) {
            "ko" -> "Korean"
            "en" -> "English"
            "ja" -> "Japanese"
            "zh" -> "Chinese"
            "es" -> "Spanish"
            "fr" -> "French"
            "de" -> "German"
            "ru" -> "Russian"
            "it" -> "Italian"
            "pt" -> "Portuguese"
            else -> code.uppercase()
        }
    }
}