package com.monkeys.translate.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class TranslateMcpService {

    @Tool(description = "텍스트를 번역합니다 (개발 예정)")
    fun translateText(
        @ToolParam(description = "번역할 텍스트", required = true)
        text: String,
        @ToolParam(description = "소스 언어")
        sourceLanguage: String = "auto",
        @ToolParam(description = "대상 언어")
        targetLanguage: String = "ko"
    ): String {
        return "번역 서비스가 개발 중입니다. 원본: $text"
    }
}