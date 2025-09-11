package com.monkeys.translate.repository

import com.monkeys.shared.dto.*

/**
 * 번역 데이터 액세스 인터페이스
 * Repository 패턴 적용
 */
interface TranslateRepository {
    
    /**
     * 텍스트 번역
     */
    suspend fun translateText(request: TranslationRequest): TranslationResult
    
    /**
     * 언어 감지
     */
    suspend fun detectLanguage(request: LanguageDetectionRequest): LanguageDetectionResult
    
    /**
     * 지원 언어 목록 조회
     */
    suspend fun getSupportedLanguages(): List<SupportedLanguage>
    
    /**
     * API 상태 확인
     */
    suspend fun checkApiHealth(): Boolean
}