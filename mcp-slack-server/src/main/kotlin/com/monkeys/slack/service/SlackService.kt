package com.monkeys.slack.service

import com.monkeys.shared.dto.*
import com.monkeys.slack.repository.SlackRepository
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * Slack Q&A 서비스 - Weather 서버 패턴과 동일
 * 비즈니스 로직 담당
 */
@Service
class SlackService(
    private val slackRepository: SlackRepository
) {
    private val logger = LoggerFactory.getLogger(SlackService::class.java)

    /**
     * 유사한 질문 검색
     */
    suspend fun searchSimilarQuestion(request: SlackQARequest): SlackQAResult {
        logger.info("유사 질문 검색: question='${request.question}', channel=${request.channel}")
        
        val matches = slackRepository.searchSimilarQuestions(
            question = request.question,
            channel = request.channel,
            threshold = request.threshold
        )
        
        // 답변이 있는 것을 우선 선택, 그 다음 유사도 순
        val bestMatch = matches
            .filter { it.qaEntry.answer.isNotEmpty() } // 답변이 있는 것만
            .maxByOrNull { it.similarity } 
            ?: matches.maxByOrNull { it.similarity } // 답변이 없으면 최고 유사도
        
        return if (bestMatch != null) {
            logger.debug("최적 매치 발견: question='${bestMatch.qaEntry.question}', answer='${bestMatch.qaEntry.answer}'")
            SlackQAResult(
                found = true,
                originalQuestion = request.question,
                matchedQuestion = bestMatch.qaEntry.question,
                answer = bestMatch.qaEntry.answer,
                similarity = bestMatch.similarity,
                channel = bestMatch.qaEntry.channel,
                timestamp = bestMatch.qaEntry.timestamp,
                author = bestMatch.qaEntry.author
            )
        } else {
            SlackQAResult(
                found = false,
                originalQuestion = request.question,
                matchedQuestion = null,
                answer = "",
                similarity = 0.0,
                channel = request.channel,
                timestamp = System.currentTimeMillis(),
                author = null
            )
        }
    }
    
    /**
     * 채널 Q&A 히스토리 조회
     */
    suspend fun getChannelHistory(channel: String, limit: Int = 50): List<SlackQAEntry> {
        logger.info("채널 히스토리 조회: channel=$channel, limit=$limit")
        return slackRepository.getChannelQAHistory(channel, limit)
    }
    
    /**
     * 새로운 Q&A 추가
     */
    suspend fun addQAEntry(entry: SlackQAEntry): Boolean {
        logger.info("Q&A 추가: channel=${entry.channel}, author=${entry.author}")
        return slackRepository.addQAEntry(entry)
    }
    
    /**
     * 채널 통계 조회
     */
    suspend fun getChannelStats(channel: String): SlackChannelStats {
        logger.info("채널 통계 조회: channel=$channel")
        return slackRepository.getChannelStats(channel)
    }
}