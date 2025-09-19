package com.monkeys.slack.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * Slack MCP Tool Provider - Weather 서버 패턴 완전 동일
 * MCP 프로토콜용 Tool 어노테이션만 담당 - 얇은 어댑터 계층
 * 예외 처리는 GlobalExceptionHandler가 담당
 */
@Service
class SlackMcpService(
    private val slackService: SlackService
) {
    private val logger = LoggerFactory.getLogger(SlackMcpService::class.java)

    @Tool(description = "Slack 채널에서 유사한 질문을 검색하여 기존 답변을 찾습니다")
    suspend fun searchSimilarQuestion(
        @ToolParam(description = "검색할 질문", required = true)
        question: String,
        @ToolParam(description = "검색할 채널 (기본값: general)")
        channel: String = "general",
        @ToolParam(description = "유사도 임계값 (0.0~1.0, 기본값: 0.7)")
        threshold: Double = 0.7
    ): SlackQAResult {
        logger.info("MCP Tool 호출: searchSimilarQuestion - question='$question', channel=$channel")
        
        val request = SlackQARequest(
            question = question,
            channel = channel,
            threshold = threshold
        )
        return slackService.searchSimilarQuestion(request)
    }

    @Tool(description = "Slack 채널의 Q&A 히스토리를 조회합니다")
    suspend fun getChannelHistory(
        @ToolParam(description = "조회할 채널 (기본값: general)", required = true)
        channel: String = "general",
        @ToolParam(description = "조회할 개수 (기본값: 20)")
        limit: Int = 20
    ): List<SlackQAEntry> {
        logger.info("MCP Tool 호출: getChannelHistory - channel=$channel, limit=$limit")
        
        return slackService.getChannelHistory(channel, limit)
    }

    @Tool(description = "Slack 채널의 Q&A 통계를 조회합니다")
    suspend fun getChannelStats(
        @ToolParam(description = "조회할 채널 (기본값: general)", required = true)
        channel: String = "general"
    ): SlackChannelStats {
        logger.info("MCP Tool 호출: getChannelStats - channel=$channel")
        
        return slackService.getChannelStats(channel)
    }
}