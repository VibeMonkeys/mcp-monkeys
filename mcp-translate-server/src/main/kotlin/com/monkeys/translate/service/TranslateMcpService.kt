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

        logger.info("번역 요청: text={}, source={}, target={}, service={}", 
            maskText(text), sourceLanguage, targetLanguage, preferredService)

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
                error = "빈 텍스트는 분석할 수 없습니다"
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
        service: String = "auto"
    ): List<SupportedLanguage> {
        val preferredService = when {
            service == "google" && googleApiKey != "dummy-key" -> "google"
            service == "libre" -> "libre"
            googleApiKey != "dummy-key" -> "google"
            else -> "libre"
        }

        return when (preferredService) {
            "google" -> getSupportedLanguagesFromGoogle()
            else -> getSupportedLanguagesFromLibre()
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
        
        if (textList.isEmpty()) {
            return listOf(TranslationResult(
                originalText = texts,
                translatedText = "",
                sourceLang = sourceLanguage,
                targetLang = targetLanguage,
                confidence = 0.0,
                service = "none",
                error = "유효한 텍스트가 없습니다"
            ))
        }

        return textList.map { text ->
            translateText(text, sourceLanguage, targetLanguage)
        }
    }

    // Google Translate API 구현
    private fun translateWithGoogle(text: String, sourceLang: String, targetLang: String): TranslationResult {
        if (googleApiKey == "dummy-key") {
            return createDummyTranslation(text, sourceLang, targetLang, "Google Translate API 키가 설정되지 않았습니다", "google")
        }

        return try {
            val translationData = executeWithRetry {
                val url = "https://translation.googleapis.com/language/translate/v2?key=$googleApiKey"
                val requestBody = mapOf(
                    "q" to text,
                    "source" to if (sourceLang == "auto") null else sourceLang,
                    "target" to targetLang,
                    "format" to "text"
                ).filterValues { it != null } as Map<String, Any>

                fetchTranslationData(url, requestBody, googleApiTimer, "google")
            }

            val translations = translationData["data"] as Map<String, Any>
            val translationsList = translations["translations"] as List<Map<String, Any>>
            val translation = translationsList.first()

            TranslationResult(
                originalText = text,
                translatedText = translation["translatedText"] as String,
                sourceLang = translation["detectedSourceLanguage"] as? String ?: sourceLang,
                targetLang = targetLang,
                confidence = 0.99, // Google API는 confidence를 제공하지 않음
                service = "google"
            )
        } catch (e: TranslateApiException) {
            logger.error("Google Translate API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("translate.google.error", "type", e.errorCode).increment()
            createDummyTranslation(text, sourceLang, targetLang, "Google 번역 실패: ${e.message}", "google")
        } catch (e: Exception) {
            logger.error("Google 번역 예상치 못한 오류", e)
            meterRegistry.counter("translate.google.error", "type", "UNEXPECTED").increment()
            createDummyTranslation(text, sourceLang, targetLang, "일시적 오류가 발생했습니다", "google")
        }
    }

    // LibreTranslate API 구현
    private fun translateWithLibre(text: String, sourceLang: String, targetLang: String): TranslationResult {
        // 테스트 환경에서 외부 API 호출을 방지하기 위한 더미 체크
        if (libreTranslateUrl.contains("libretranslate.de") && libreApiKey.isEmpty()) {
            return createDummyTranslation(text, sourceLang, targetLang, "LibreTranslate API 키가 설정되지 않았습니다", "libre")
        }
        
        return try {
            val translationData = executeWithRetry {
                val requestBody = mapOf(
                    "q" to text,
                    "source" to sourceLang,
                    "target" to targetLang,
                    "format" to "text"
                ) + if (libreApiKey.isNotEmpty()) mapOf("api_key" to libreApiKey) else emptyMap()

                fetchTranslationData("$libreTranslateUrl/translate", requestBody, libreApiTimer, "libre")
            }

            TranslationResult(
                originalText = text,
                translatedText = translationData["translatedText"] as String,
                sourceLang = translationData["detectedLanguage"] as? String ?: sourceLang,
                targetLang = targetLang,
                confidence = 0.95, // LibreTranslate 기본 confidence
                service = "libre"
            )
        } catch (e: TranslateApiException) {
            logger.error("LibreTranslate API 오류: {} (코드: {})", e.message, e.errorCode)
            meterRegistry.counter("translate.libre.error", "type", e.errorCode).increment()
            createDummyTranslation(text, sourceLang, targetLang, "LibreTranslate 번역 실패: ${e.message}", "libre")
        } catch (e: Exception) {
            logger.error("LibreTranslate 예상치 못한 오류", e)
            meterRegistry.counter("translate.libre.error", "type", "UNEXPECTED").increment()
            createDummyTranslation(text, sourceLang, targetLang, "일시적 오류가 발생했습니다", "libre")
        }
    }

    // 언어 감지 - Google
    private fun detectLanguageWithGoogle(text: String): LanguageDetectionResult {
        if (googleApiKey == "dummy-key") {
            return createDummyLanguageDetection(text, "Google API 키가 설정되지 않았습니다")
        }

        return try {
            val detectionData = executeWithRetry {
                val url = "https://translation.googleapis.com/language/translate/v2/detect?key=$googleApiKey"
                val requestBody = mapOf("q" to text)
                fetchTranslationData(url, requestBody, googleApiTimer, "google")
            }

            val data = detectionData["data"] as Map<String, Any>
            val detections = data["detections"] as List<List<Map<String, Any>>>
            val detection = detections.first().first()

            LanguageDetectionResult(
                text = text,
                detectedLanguage = detection["language"] as String,
                confidence = (detection["confidence"] as Number).toDouble(),
                service = "google"
            )
        } catch (e: Exception) {
            logger.error("Google 언어 감지 오류", e)
            createDummyLanguageDetection(text, "언어 감지 실패: ${e.message}")
        }
    }

    // 언어 감지 - LibreTranslate
    private fun detectLanguageWithLibre(text: String): LanguageDetectionResult {
        // 테스트 환경에서 외부 API 호출을 방지하기 위한 더미 체크
        if (libreTranslateUrl.contains("libretranslate.de") && libreApiKey.isEmpty()) {
            return createDummyLanguageDetection(text, "LibreTranslate API 키가 설정되지 않았습니다")
        }
        
        return try {
            val detectionData = executeWithRetry {
                val requestBody = mapOf("q" to text) + 
                    if (libreApiKey.isNotEmpty()) mapOf("api_key" to libreApiKey) else emptyMap()
                fetchTranslationData("$libreTranslateUrl/detect", requestBody, libreApiTimer, "libre")
            }

            val detections = detectionData as List<Map<String, Any>>
            val detection = detections.first()

            LanguageDetectionResult(
                text = text,
                detectedLanguage = detection["language"] as String,
                confidence = (detection["confidence"] as Number).toDouble(),
                service = "libre"
            )
        } catch (e: Exception) {
            logger.error("LibreTranslate 언어 감지 오류", e)
            createDummyLanguageDetection(text, "언어 감지 실패: ${e.message}")
        }
    }

    // 지원 언어 목록 - Google
    private fun getSupportedLanguagesFromGoogle(): List<SupportedLanguage> {
        if (googleApiKey == "dummy-key") {
            return createDummySupportedLanguages("Google API 키가 설정되지 않았습니다")
        }

        return try {
            val languagesData = executeWithRetry {
                val url = "https://translation.googleapis.com/language/translate/v2/languages?key=$googleApiKey&target=ko"
                fetchTranslationData(url, emptyMap(), googleApiTimer, "google")
            }

            val data = languagesData["data"] as Map<String, Any>
            val languages = data["languages"] as List<Map<String, Any>>

            languages.map { lang ->
                SupportedLanguage(
                    code = lang["language"] as String,
                    name = lang["name"] as String,
                    service = "google"
                )
            }
        } catch (e: Exception) {
            logger.error("Google 지원 언어 조회 오류", e)
            createDummySupportedLanguages("지원 언어 조회 실패: ${e.message}")
        }
    }

    // 지원 언어 목록 - LibreTranslate
    private fun getSupportedLanguagesFromLibre(): List<SupportedLanguage> {
        // 테스트 환경에서 외부 API 호출을 방지하기 위한 더미 체크
        if (libreTranslateUrl.contains("libretranslate.de") && libreApiKey.isEmpty()) {
            return createDummySupportedLanguages("LibreTranslate API 키가 설정되지 않았습니다")
        }
        
        return try {
            val languagesData = executeWithRetry {
                fetchTranslationData("$libreTranslateUrl/languages", emptyMap(), libreApiTimer, "libre", "GET")
            }

            val languages = languagesData as List<Map<String, Any>>
            languages.map { lang ->
                SupportedLanguage(
                    code = lang["code"] as String,
                    name = lang["name"] as String,
                    service = "libre"
                )
            }
        } catch (e: Exception) {
            logger.error("LibreTranslate 지원 언어 조회 오류", e)
            createDummySupportedLanguages("지원 언어 조회 실패: ${e.message}")
        }
    }

    // HTTP 요청 실행 및 JSON 파싱 (메트릭 포함)
    private fun fetchTranslationData(
        url: String, 
        requestBody: Map<String, Any>, 
        timer: Timer,
        serviceName: String,
        method: String = "POST"
    ): Map<String, Any> {
        val request = if (method == "GET") {
            Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MCP-Monkeys-Translate/1.0")
                .build()
        } else {
            val jsonBody = mapper.writeValueAsString(requestBody)
            Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MCP-Monkeys-Translate/1.0")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()
        }

        return timer.recordCallable {
            translateHttpClient.newCall(request).execute().use { response ->
                when (response.code) {
                    200 -> {
                        val jsonResponse = response.body?.string() ?: "{}"
                        meterRegistry.counter("translate.${serviceName}.api.success").increment()
                        mapper.readValue(jsonResponse)
                    }
                    400 -> {
                        meterRegistry.counter("translate.${serviceName}.api.error", "type", "bad_request").increment()
                        throw TranslateApiException("잘못된 요청", "BAD_REQUEST")
                    }
                    401, 403 -> {
                        meterRegistry.counter("translate.${serviceName}.api.error", "type", "auth").increment()
                        throw TranslateApiException("인증 실패", "INVALID_API_KEY")
                    }
                    429 -> {
                        meterRegistry.counter("translate.${serviceName}.api.error", "type", "rate_limit").increment()
                        throw TranslateApiException("API 사용량 한도 초과", "RATE_LIMIT_EXCEEDED")
                    }
                    500, 502, 503, 504 -> {
                        meterRegistry.counter("translate.${serviceName}.api.error", "type", "server_error").increment()
                        throw TranslateApiException("서버 오류", "SERVER_ERROR")
                    }
                    else -> {
                        meterRegistry.counter("translate.${serviceName}.api.error", "type", "unknown").increment()
                        throw TranslateApiException("알 수 없는 오류", "UNKNOWN_ERROR")
                    }
                }
            }
        } ?: throw TranslateApiException("응답 파싱 실패", "PARSE_ERROR")
    }

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

    // 유틸리티 함수들
    private fun createDummyTranslation(text: String, sourceLang: String, targetLang: String, error: String, service: String) = 
        TranslationResult(
            originalText = text,
            translatedText = "테스트 번역 결과: $text",
            sourceLang = sourceLang,
            targetLang = targetLang,
            confidence = 0.5,
            service = service,
            error = error
        )

    private fun createDummyLanguageDetection(text: String, error: String) = 
        LanguageDetectionResult(
            text = text,
            detectedLanguage = "ko",
            confidence = 0.5,
            service = "dummy",
            error = error
        )

    private fun createDummySupportedLanguages(error: String) = 
        listOf(
            SupportedLanguage("ko", "한국어", "dummy"),
            SupportedLanguage("en", "영어", "dummy"),
            SupportedLanguage("ja", "일본어", "dummy")
        )

    private fun maskText(text: String): String {
        return if (text.length > 50) "${text.take(47)}..." else text
    }

    private fun maskApiKey(apiKey: String): String {
        return if (apiKey.length > 8) {
            "${apiKey.take(4)}****${apiKey.takeLast(4)}"
        } else {
            "****"
        }
    }
}

// 예외 클래스들
class TranslateApiException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

// 데이터 클래스들
data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val confidence: Double,
    val service: String,
    val error: String? = null
)

data class LanguageDetectionResult(
    val text: String,
    val detectedLanguage: String,
    val confidence: Double,
    val service: String,
    val error: String? = null
)

data class SupportedLanguage(
    val code: String,
    val name: String,
    val service: String
)