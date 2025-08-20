package com.monkeys.jira.controller

import com.monkeys.jira.service.JiraMcpService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mcp/jira")
class JiraController(
    private val jiraMcpService: JiraMcpService
) {

    @GetMapping("/tools")
    fun getAvailableTools(): Map<String, String> {
        return mapOf(
            "getIssues" to "Jira 프로젝트의 이슈 목록을 조회합니다",
            "createIssue" to "Jira에 새로운 이슈를 생성합니다",
            "getProject" to "Jira 프로젝트 정보를 조회합니다",
            "getActiveSprints" to "Jira 프로젝트의 활성 스프린트 목록을 조회합니다"
        )
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "service" to "Jira MCP Server",
            "version" to "1.0.0"
        )
    }
}