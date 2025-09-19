package com.monkeys.slack.config

import com.monkeys.slack.service.SlackMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Slack MCP Tool 설정 클래스 - Weather 서버와 동일한 구조
 * MCP 도구 등록 및 설정을 담당
 */
@Configuration
class ToolConfiguration {

    /**
     * Slack MCP Tools 등록
     * SlackMcpService의 @Tool 어노테이션이 있는 메서드들을 자동으로 스캔하여 등록
     */
    @Bean
    fun slackTools(slackMcpService: SlackMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(slackMcpService)
            .build()
    }
}