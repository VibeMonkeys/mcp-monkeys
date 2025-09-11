package com.monkeys.translate.service

import com.monkeys.translate.repository.TranslateRepository
import com.monkeys.shared.dto.*
import com.monkeys.shared.exception.BusinessException
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import kotlinx.coroutines.runBlocking

/**
 * 번역 비즈니스 로직 서비스
 * 순수한 비즈니스 로직만 담당 - 외부 API나 프레젠테이션 관심사 제외
 */
@Service
class TranslateService(
    private val translateRepository: TranslateRepository
) {
    private val logger = LoggerFactory.getLogger(TranslateService::class.java)
    
    /**
     * 텍스트 번역
     */
    fun translateText(request: TranslationRequest): TranslationResult = runBlocking {
        validateTranslationRequest(request)
        
        logger.info("번역 요청: from=${request.sourceLanguage}, to=${request.targetLanguage}, length=${request.text.length}")
        
        try {
            translateRepository.translateText(request)
        } catch (e: Exception) {
            logger.error("번역 실패", e)
            throw BusinessException("텍스트 번역을 수행할 수 없습니다: ${e.message}", "TRANSLATION_FAILED", e)
        }
    }
    
    /**
     * 언어 감지
     */
    fun detectLanguage(request: LanguageDetectionRequest): LanguageDetectionResult = runBlocking {
        validateDetectionRequest(request)
        
        logger.info("언어 감지 요청: length=${request.text.length}")
        
        try {
            translateRepository.detectLanguage(request)
        } catch (e: Exception) {
            logger.error("언어 감지 실패", e)
            throw BusinessException("언어 감지를 수행할 수 없습니다: ${e.message}", "DETECTION_FAILED", e)
        }
    }
    
    /**
     * 지원 언어 목록 조회
     */
    fun getSupportedLanguages(): List<SupportedLanguage> = runBlocking {
        logger.info("지원 언어 목록 조회")
        
        try {
            translateRepository.getSupportedLanguages()
        } catch (e: Exception) {
            logger.error("지원 언어 목록 조회 실패", e)
            throw BusinessException("지원 언어 목록을 조회할 수 없습니다: ${e.message}", "LANGUAGES_FETCH_FAILED", e)
        }
    }
    
    /**
     * 클라이언트용 응답으로 변환
     */
    fun convertToResponse(result: TranslationResult): TranslationResponse {
        return TranslationResponse(
            originalText = result.originalText,
            translatedText = result.translatedText,
            fromLanguage = result.sourceLanguage,
            toLanguage = result.targetLanguage,
            confidenceScore = result.confidenceScore,
            provider = "Google Translate"
        )
    }
    
    /**
     * 서비스 상태 확인
     */
    fun checkServiceHealth(): Boolean = runBlocking {
        try {
            translateRepository.checkApiHealth()
        } catch (e: Exception) {
            logger.error("Translate service health check failed", e)
            false
        }
    }
    
    // 검증 메소드들
    private fun validateTranslationRequest(request: TranslationRequest) {
        if (request.text.isBlank()) {
            throw BusinessException("번역할 텍스트가 필요합니다", "TEXT_REQUIRED")
        }
        if (request.text.length > 5000) {
            throw BusinessException("번역할 텍스트가 너무 깁니다 (최대 5000자)", "TEXT_TOO_LONG")
        }
        if (request.targetLanguage.isBlank()) {
            throw BusinessException("대상 언어가 필요합니다", "TARGET_LANGUAGE_REQUIRED")
        }
    }
    
    private fun validateDetectionRequest(request: LanguageDetectionRequest) {
        if (request.text.isBlank()) {
            throw BusinessException("언어 감지할 텍스트가 필요합니다", "TEXT_REQUIRED")
        }
        if (request.text.length > 1000) {
            throw BusinessException("언어 감지할 텍스트가 너무 깁니다 (최대 1000자)", "TEXT_TOO_LONG")
        }
    }
}