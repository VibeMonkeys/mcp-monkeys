package com.monkeys.slack.controller

import com.monkeys.slack.service.SlackMcpService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mcp/slack")
class SlackController(
    private val slackMcpService: SlackMcpService
) {

    @GetMapping("/tools")
    fun getAvailableTools(): Map<String, String> {
        return mapOf(
            "sendMessage" to "Slack 채널에 메시지를 전송합니다",
            "getMessages" to "Slack 채널의 최근 메시지를 조회합니다",
            "getChannels" to "Slack 채널 목록을 조회합니다",
            "getUsers" to "Slack 워크스페이스의 사용자 목록을 조회합니다"
        )
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "service" to "Slack MCP Server",
            "version" to "1.0.0"
        )
    }
}