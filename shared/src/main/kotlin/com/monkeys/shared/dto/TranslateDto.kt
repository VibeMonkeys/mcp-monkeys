package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

/**
 * 번역 결과 DTO
 */
data class TranslationResult(
    @JsonPropertyDescription("원본 텍스트")
    val originalText: String,
    
    @JsonPropertyDescription("번역된 텍스트")
    val translatedText: String,
    
    @JsonPropertyDescription("원본 언어 코드")
    val sourceLanguage: String,
    
    @JsonPropertyDescription("번역 대상 언어 코드")
    val targetLanguage: String,
    
    @JsonPropertyDescription("신뢰도 점수 (0.0-1.0)")
    val confidenceScore: Double? = null,
    
    @JsonPropertyDescription("감지된 언어 (자동 감지 시)")
    val detectedLanguage: String? = null
)

/**
 * 번역 요청 DTO
 */
data class TranslationRequest(
    @JsonPropertyDescription("번역할 텍스트")
    val text: String,
    
    @JsonPropertyDescription("원본 언어 코드 (auto for 자동감지)")
    val sourceLanguage: String = "auto",
    
    @JsonPropertyDescription("번역 대상 언어 코드")
    val targetLanguage: String,
    
    @JsonPropertyDescription("번역 모드 (formal, informal, auto)")
    val mode: String = "auto"
)

/**
 * 언어 감지 요청 DTO
 */
data class LanguageDetectionRequest(
    @JsonPropertyDescription("언어를 감지할 텍스트")
    val text: String
)

/**
 * 언어 감지 결과 DTO
 */
data class LanguageDetectionResult(
    @JsonPropertyDescription("감지된 언어 코드")
    val languageCode: String,
    
    @JsonPropertyDescription("언어명")
    val languageName: String,
    
    @JsonPropertyDescription("신뢰도 점수 (0.0-1.0)")
    val confidence: Double
)

/**
 * 지원 언어 DTO
 */
data class SupportedLanguage(
    @JsonPropertyDescription("언어 코드")
    val code: String,
    
    @JsonPropertyDescription("언어명 (영어)")
    val name: String,
    
    @JsonPropertyDescription("언어명 (한국어)")
    val nameKo: String,
    
    @JsonPropertyDescription("언어명 (원어)")
    val nativeName: String
)

/**
 * 번역 응답 DTO (클라이언트용)
 */
data class TranslationResponse(
    @JsonPropertyDescription("원본 텍스트")
    val originalText: String,
    
    @JsonPropertyDescription("번역된 텍스트")
    val translatedText: String,
    
    @JsonPropertyDescription("원본 언어 코드")
    val fromLanguage: String,
    
    @JsonPropertyDescription("번역 대상 언어 코드")
    val toLanguage: String,
    
    @JsonPropertyDescription("신뢰도 점수 (0-1)")
    val confidenceScore: Double? = null,
    
    @JsonPropertyDescription("번역 서비스 제공자")
    val provider: String? = null
)

/**
 * 대량 번역 요청 DTO
 */
data class BulkTranslationRequest(
    @JsonPropertyDescription("번역할 텍스트 목록")
    val texts: List<String>,
    
    @JsonPropertyDescription("원본 언어 코드")
    val sourceLanguage: String,
    
    @JsonPropertyDescription("번역 대상 언어 코드")
    val targetLanguage: String,
    
    @JsonPropertyDescription("번역 모드")
    val mode: String = "auto"
)

/**
 * 대량 번역 결과 DTO
 */
data class BulkTranslationResult(
    @JsonPropertyDescription("번역 결과 목록")
    val translations: List<TranslationResult>,
    
    @JsonPropertyDescription("성공한 번역 수")
    val successCount: Int,
    
    @JsonPropertyDescription("실패한 번역 수")
    val failureCount: Int,
    
    @JsonPropertyDescription("전체 처리 시간 (ms)")
    val processingTime: Long
)