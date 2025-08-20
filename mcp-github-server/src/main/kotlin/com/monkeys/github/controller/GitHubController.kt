package com.monkeys.github.controller

import com.monkeys.github.service.GitHubMcpService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mcp/github")
class GitHubController(
    private val gitHubMcpService: GitHubMcpService
) {

    @GetMapping("/tools")
    fun getAvailableTools(): Map<String, String> {
        return mapOf(
            "getIssues" to "GitHub 저장소의 이슈 목록을 조회합니다",
            "getPullRequests" to "GitHub 저장소의 Pull Request 목록을 조회합니다",
            "getRepository" to "GitHub 저장소 정보를 조회합니다",
            "createIssue" to "GitHub 저장소에 새로운 이슈를 생성합니다"
        )
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "service" to "GitHub MCP Server",
            "version" to "1.0.0"
        )
    }
}