package com.monkeys.translate.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit

@Service
class TranslateMcpService(
    @Value("\${google.translate.api.key:dummy-key}") private val googleApiKey: String,
    @Value("\${libretranslate.api.url:https://libretranslate.de}") private val libreTranslateUrl: String,
    @Value("\${libretranslate.api.key:}") private val libreApiKey: String,
    private val translateHttpClient: OkHttpClient,
    private val meterRegistry: MeterRegistry
) {
    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(TranslateMcpService::class.java)
    private val maxRetries = 3
    
    // 메트릭 타이머
    private val googleApiTimer = Timer.builder("translate.google.api.request")
        .description("Google Translate API 요청 시간")
        .register(meterRegistry)
    
    private val libreApiTimer = Timer.builder("translate.libre.api.request")
        .description("LibreTranslate API 요청 시간")
        .register(meterRegistry)

    @Tool(description = "텍스트를 번역합니다. Google Translate API 또는 LibreTranslate를 사용합니다")
    fun translateText(
        @ToolParam(description = "번역할 텍스트", required = true)
        text: String,
        @ToolParam(description = "소스 언어 (auto, ko, en, ja, zh, es, fr, de 등)")
        sourceLanguage: String = "auto",
        @ToolParam(description = "대상 언어 (ko, en, ja, zh, es, fr, de 등)")
        targetLanguage: String = "ko",
        @ToolParam(description = "번역 서비스 (google, libre, auto)")
        service: String = "auto"
    ): TranslationResult {
        if (text.isBlank()) {
            return TranslationResult(
                originalText = text,
                translatedText = text,
                sourceLang = sourceLanguage,
                targetLang = targetLanguage,
                confidence = 0.0,
                service = "none",
                error = "빈 텍스트는 번역할 수 없습니다"
            )
        }

        val preferredService = when {
            service == "google" -> "google"
            service == "libre" -> "libre"
            googleApiKey != "dummy-key" -> "google"
            else -> "libre"
        }

        return when (preferredService) {
            "google" -> translateWithGoogle(text, sourceLanguage, targetLanguage)
            else -> translateWithLibre(text, sourceLanguage, targetLanguage)
        }
    }

    @Tool(description = "언어를 감지합니다")
    fun detectLanguage(
        @ToolParam(description = "언어를 감지할 텍스트", required = true)
        text: String
    ): LanguageDetectionResult {
        if (text.isBlank()) {
            return LanguageDetectionResult(
                text = text,
                detectedLanguage = "unknown",
                confidence = 0.0,
                service = "none",
                error = "빈 텍스트의 언어는 감지할 수 없습니다"
            )
        }

        return if (googleApiKey != "dummy-key") {
            detectLanguageWithGoogle(text)
        } else {
            detectLanguageWithLibre(text)
        }
    }

    @Tool(description = "지원되는 언어 목록을 조회합니다")
    fun getSupportedLanguages(
        @ToolParam(description = "번역 서비스 (google, libre)")
        service: String = "google"
    ): List<SupportedLanguage> {
        return when (service) {
            "google" -> getSupportedLanguagesGoogle()
            "libre" -> getSupportedLanguagesLibre()
            else -> getSupportedLanguagesGoogle()
        }
    }

    @Tool(description = "여러 텍스트를 일괄 번역합니다")
    fun batchTranslate(
        @ToolParam(description = "번역할 텍스트 목록 (쉼표로 구분)", required = true)
        texts: String,
        @ToolParam(description = "소스 언어")
        sourceLanguage: String = "auto",
        @ToolParam(description = "대상 언어")
        targetLanguage: String = "ko"
    ): List<TranslationResult> {
        val textList = texts.split(",").map { it.trim() }.filter { it.isNotBlank() }
        
        return textList.map { text ->
            translateText(text, sourceLanguage, targetLanguage)
        }
    }

    private fun translateWithGoogle(text: String, sourceLang: String, targetLang: String): TranslationResult {
        if (googleApiKey == "dummy-key") {
            return createDummyTranslation(text, sourceLang, targetLang, "Google Translate API 키가 설정되지 않았습니다")
        }

        return try {
            executeWithRetry {
                val url = "https://translation.googleapis.com/language/translate/v2?key=$googleApiKey"
                val requestBody = mapOf(
                    "q" to text,
                    "source" to if (sourceLang == "auto") null else sourceLang,
                    "target" to targetLang,
                    "format" to "text"
                ).filterValues { it != null }

                val json = mapper.writeValueAsString(requestBody)
                val request = Request.Builder()
                    .url(url)
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .addHeader("User-Agent", "MCP-Monkeys-Translate/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    when (response.code) {
                        200 -> {
                            val responseBody = response.body?.string() ?: "{}"
                            val result: Map<String, Any> = mapper.readValue(responseBody)
                            val data = result["data"] as Map<String, Any>
                            val translations = data["translations"] as List<Map<String, Any>>
                            val translation = translations.first()
                            
                            TranslationResult(
                                originalText = text,
                                translatedText = translation["translatedText"] as String,
                                sourceLang = translation["detectedSourceLanguage"] as? String ?: sourceLang,
                                targetLang = targetLang,
                                confidence = 0.95,
                                service = "google",
                                error = null
                            )
                        }
                        400 -> throw IllegalArgumentException("잘못된 요청입니다. 언어 코드를 확인해주세요.")
                        403 -> throw IllegalArgumentException("Google Translate API 키가 유효하지 않습니다.")
                        429 -> throw RuntimeException("API 사용량 한도를 초과했습니다.")
                        else -> throw RuntimeException("Google Translate API 오류: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Google Translate 오류", e)
            createDummyTranslation(text, sourceLang, targetLang, "Google Translate 오류: ${e.message}")
        }
    }

    private fun translateWithLibre(text: String, sourceLang: String, targetLang: String): TranslationResult {
        return try {
            executeWithRetry {
                val url = "$libreTranslateUrl/translate"
                val requestBody = mutableMapOf(
                    "q" to text,
                    "source" to if (sourceLang == "auto") "auto" else sourceLang,
                    "target" to targetLang,
                    "format" to "text"
                )
                
                if (libreApiKey.isNotBlank()) {
                    requestBody["api_key"] = libreApiKey
                }

                val json = mapper.writeValueAsString(requestBody)
                val request = Request.Builder()
                    .url(url)
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .addHeader("User-Agent", "MCP-Monkeys-Translate/1.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    when (response.code) {
                        200 -> {
                            val responseBody = response.body?.string() ?: "{}"
                            val result: Map<String, Any> = mapper.readValue(responseBody)
                            
                            TranslationResult(
                                originalText = text,
                                translatedText = result["translatedText"] as String,
                                sourceLang = result["detectedLanguage"] as? String ?: sourceLang,
                                targetLang = targetLang,
                                confidence = 0.85,
                                service = "libre",
                                error = null
                            )
                        }
                        400 -> throw IllegalArgumentException("잘못된 요청입니다. 언어 코드를 확인해주세요.")
                        403 -> throw IllegalArgumentException("LibreTranslate API 키가 필요합니다.")
                        429 -> throw RuntimeException("API 사용량 한도를 초과했습니다.")
                        500 -> throw RuntimeException("LibreTranslate 서버 오류입니다.")
                        else -> throw RuntimeException("LibreTranslate API 오류: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("LibreTranslate 오류", e)
            createDummyTranslation(text, sourceLang, targetLang, "LibreTranslate 오류: ${e.message}")
        }
    }

    private fun detectLanguageWithGoogle(text: String): LanguageDetectionResult {
        if (googleApiKey == "dummy-key") {
            return LanguageDetectionResult(
                text = text,
                detectedLanguage = "unknown",
                confidence = 0.0,
                service = "google",
                error = "Google Translate API 키가 설정되지 않았습니다"
            )
        }

        return try {
            executeWithRetry {
                val url = "https://translation.googleapis.com/language/translate/v2/detect?key=$googleApiKey"
                val requestBody = mapOf("q" to text)
                val json = mapper.writeValueAsString(requestBody)
                
                val request = Request.Builder()
                    .url(url)
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "{}"
                        val result: Map<String, Any> = mapper.readValue(responseBody)
                        val data = result["data"] as Map<String, Any>
                        val detections = data["detections"] as List<List<Map<String, Any>>>
                        val detection = detections.first().first()
                        
                        LanguageDetectionResult(
                            text = text,
                            detectedLanguage = detection["language"] as String,
                            confidence = (detection["confidence"] as Number).toDouble(),
                            service = "google",
                            error = null
                        )
                    } else {
                        throw RuntimeException("언어 감지 실패: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Google 언어 감지 오류", e)
            LanguageDetectionResult(
                text = text,
                detectedLanguage = "unknown",
                confidence = 0.0,
                service = "google",
                error = e.message
            )
        }
    }

    private fun detectLanguageWithLibre(text: String): LanguageDetectionResult {
        return try {
            executeWithRetry {
                val url = "$libreTranslateUrl/detect"
                val requestBody = mutableMapOf("q" to text)
                
                if (libreApiKey.isNotBlank()) {
                    requestBody["api_key"] = libreApiKey
                }

                val json = mapper.writeValueAsString(requestBody)
                val request = Request.Builder()
                    .url(url)
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "[]"
                        val result: List<Map<String, Any>> = mapper.readValue(responseBody)
                        val detection = result.first()
                        
                        LanguageDetectionResult(
                            text = text,
                            detectedLanguage = detection["language"] as String,
                            confidence = (detection["confidence"] as Number).toDouble(),
                            service = "libre",
                            error = null
                        )
                    } else {
                        throw RuntimeException("언어 감지 실패: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("LibreTranslate 언어 감지 오류", e)
            LanguageDetectionResult(
                text = text,
                detectedLanguage = guessLanguage(text),
                confidence = 0.5,
                service = "libre",
                error = e.message
            )
        }
    }

    private fun getSupportedLanguagesGoogle(): List<SupportedLanguage> {
        if (googleApiKey == "dummy-key") {
            return getCommonLanguages()
        }

        return try {
            executeWithRetry {
                val url = "https://translation.googleapis.com/language/translate/v2/languages?key=$googleApiKey&target=ko"
                val request = Request.Builder().url(url).build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "{}"
                        val result: Map<String, Any> = mapper.readValue(responseBody)
                        val data = result["data"] as Map<String, Any>
                        val languages = data["languages"] as List<Map<String, Any>>
                        
                        languages.map { lang ->
                            SupportedLanguage(
                                code = lang["language"] as String,
                                name = lang["name"] as String,
                                service = "google"
                            )
                        }
                    } else {
                        throw RuntimeException("언어 목록 조회 실패: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Google 지원 언어 조회 오류", e)
            getCommonLanguages()
        }
    }

    private fun getSupportedLanguagesLibre(): List<SupportedLanguage> {
        return try {
            executeWithRetry {
                val url = "$libreTranslateUrl/languages"
                val request = Request.Builder().url(url).build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "[]"
                        val result: List<Map<String, Any>> = mapper.readValue(responseBody)
                        
                        result.map { lang ->
                            SupportedLanguage(
                                code = lang["code"] as String,
                                name = lang["name"] as String,
                                service = "libre"
                            )
                        }
                    } else {
                        throw RuntimeException("언어 목록 조회 실패: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("LibreTranslate 지원 언어 조회 오류", e)
            getCommonLanguages()
        }
    }

    private fun <T> executeWithRetry(operation: () -> T): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logger.warn("번역 API 호출 시도 ${attempt + 1}/$maxRetries 실패: ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    val delay = (1000 * Math.pow(2.0, attempt.toDouble())).toLong()
                    Thread.sleep(delay)
                }
            }
        }
        
        throw lastException ?: RuntimeException("번역 API 호출이 $maxRetries 번 모두 실패했습니다")
    }

    private fun createDummyTranslation(text: String, sourceLang: String, targetLang: String, error: String): TranslationResult {
        return TranslationResult(
            originalText = text,
            translatedText = "번역 테스트: $text",
            sourceLang = sourceLang,
            targetLang = targetLang,
            confidence = 0.0,
            service = "dummy",
            error = error
        )
    }

    private fun guessLanguage(text: String): String {
        return when {
            text.matches(Regex(".*[가-힣].*")) -> "ko"
            text.matches(Regex(".*[ひらがなカタカナー].*")) -> "ja"
            text.matches(Regex(".*[一-龯].*")) -> "zh"
            else -> "en"
        }
    }

    private fun getCommonLanguages(): List<SupportedLanguage> {
        return listOf(
            SupportedLanguage("ko", "한국어", "common"),
            SupportedLanguage("en", "English", "common"),
            SupportedLanguage("ja", "日本語", "common"),
            SupportedLanguage("zh", "中文", "common"),
            SupportedLanguage("es", "Español", "common"),
            SupportedLanguage("fr", "Français", "common"),
            SupportedLanguage("de", "Deutsch", "common"),
            SupportedLanguage("ru", "Русский", "common"),
            SupportedLanguage("pt", "Português", "common"),
            SupportedLanguage("it", "Italiano", "common")
        )
    }
}

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val confidence: Double,
    val service: String,
    val error: String?
)

data class LanguageDetectionResult(
    val text: String,
    val detectedLanguage: String,
    val confidence: Double,
    val service: String,
    val error: String?
)

data class SupportedLanguage(
    val code: String,
    val name: String,
    val service: String
)