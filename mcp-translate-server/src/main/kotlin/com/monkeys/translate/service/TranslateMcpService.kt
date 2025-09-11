package com.monkeys.translate.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * Translate MCP Tool Provider
 * MCP 프로토콜용 Tool 어노테이션만 담당 - 얇은 어댑터 계층
 * 예외 처리는 GlobalExceptionHandler가 담당
 */
@Service
class TranslateMcpService(
    private val translateService: TranslateService
) {
    private val logger = LoggerFactory.getLogger(TranslateMcpService::class.java)

    @Tool(description = "텍스트를 번역합니다")
    fun translateText(
        @ToolParam(description = "번역할 텍스트", required = true)
        text: String,
        @ToolParam(description = "소스 언어 (auto, ko, en, ja, zh, es, fr, de 등)")
        sourceLanguage: String = "auto",
        @ToolParam(description = "대상 언어 (ko, en, ja, zh, es, fr, de 등)")
        targetLanguage: String = "ko"
    ): TranslationResult {
        logger.info("MCP Tool 호출: translateText - from=$sourceLanguage, to=$targetLanguage, length=${text.length}")
        
        val request = TranslationRequest(
            text = text,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage
        )
        return translateService.translateText(request)
    }

    @Tool(description = "텍스트의 언어를 감지합니다")
    fun detectLanguage(
        @ToolParam(description = "언어를 감지할 텍스트", required = true)
        text: String
    ): LanguageDetectionResult {
        logger.info("MCP Tool 호출: detectLanguage - length=${text.length}")
        
        val request = LanguageDetectionRequest(text = text)
        return translateService.detectLanguage(request)
    }

    @Tool(description = "지원하는 언어 목록을 조회합니다")
    fun getSupportedLanguages(): List<SupportedLanguage> {
        logger.info("MCP Tool 호출: getSupportedLanguages")
        
        return translateService.getSupportedLanguages()
    }
}