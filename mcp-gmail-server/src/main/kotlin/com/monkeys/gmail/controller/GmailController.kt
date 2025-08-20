package com.monkeys.gmail.controller

import com.monkeys.gmail.service.GmailMcpService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mcp/gmail")
class GmailController(
    private val gmailMcpService: GmailMcpService
) {

    @GetMapping("/tools")
    fun getAvailableTools(): Map<String, String> {
        return mapOf(
            "getMessages" to "Gmail 받은편지함의 메일 목록을 조회합니다",
            "sendMessage" to "Gmail로 메일을 발송합니다",
            "getLabels" to "Gmail 라벨 목록을 조회합니다"
        )
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "service" to "Gmail MCP Server",
            "version" to "1.0.0"
        )
    }
}