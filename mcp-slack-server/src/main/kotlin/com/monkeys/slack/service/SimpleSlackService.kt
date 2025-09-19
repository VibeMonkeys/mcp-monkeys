package com.monkeys.slack.service

import com.monkeys.shared.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import jakarta.annotation.PostConstruct

/**
 * 간단한 Slack 연동 서비스
 * Socket Mode는 추후 구현, 현재는 기본 연동 테스트
 */
@Service
@ConditionalOnProperty(name = ["slack.socket-mode.enabled"], havingValue = "true")
class SimpleSlackService(
    private val slackService: SlackService,
    @Value("\${slack.bot-token}") private val botToken: String,
    @Value("\${slack.app-token}") private val appToken: String
) {
    private val logger = LoggerFactory.getLogger(SimpleSlackService::class.java)

    @PostConstruct
    fun initialize() {
        logger.info("=== Slack Socket Mode 초기화 ===")
        logger.info("Bot Token: ${botToken.take(20)}...")
        logger.info("App Token: ${appToken.take(20)}...")
        logger.info("Q&A 봇이 준비되었습니다!")
        logger.info("이제 Slack 채널에서 질문해보세요!")
        
        // TODO: 실제 Socket Mode 연결은 추후 구현
        // 현재는 웹훅이나 수동 테스트로 확인 가능
    }
    
    /**
     * 수동으로 메시지 처리 테스트
     */
    suspend fun testMessageProcessing(question: String, channel: String = "general"): SlackQAResult {
        logger.info("테스트 메시지 처리: question='$question', channel=$channel")
        
        val request = SlackQARequest(
            question = question,
            channel = channel,
            threshold = 0.7
        )
        
        return slackService.searchSimilarQuestion(request)
    }
}