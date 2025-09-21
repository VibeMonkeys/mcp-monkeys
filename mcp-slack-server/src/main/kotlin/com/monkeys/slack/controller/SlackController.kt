package com.monkeys.slack.controller

import com.monkeys.shared.dto.*
import com.monkeys.slack.service.SlackService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

/**
 * Slack Q&A 컨트롤러 - Weather 서버 패턴과 동일
 * REST API 제공
 */
@RestController
@RequestMapping("/api/slack")
@CrossOrigin(origins = ["*"])
class SlackController(
    private val slackService: SlackService
) {
    private val logger = LoggerFactory.getLogger(SlackController::class.java)

    /**
     * 유사한 질문 검색
     */
    @PostMapping("/search")
    suspend fun searchSimilarQuestion(@RequestBody request: SlackQARequest): ResponseEntity<SlackQAResult> {
        logger.info("Q&A 검색 요청: ${request.question}")
        
        return try {
            val result = slackService.searchSimilarQuestion(request)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Q&A 검색 실패", e)
            ResponseEntity.status(500).body(
                SlackQAResult(
                    found = false,
                    originalQuestion = request.question,
                    matchedQuestion = null,
                    answer = "검색 중 오류가 발생했습니다: ${e.message}",
                    similarity = 0.0,
                    channel = request.channel,
                    timestamp = System.currentTimeMillis(),
                    author = null
                )
            )
        }
    }
    
    /**
     * 채널 히스토리 조회
     */
    @GetMapping("/history/{channel}")
    suspend fun getChannelHistory(
        @PathVariable channel: String,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<SlackQAEntry>> {
        logger.info("채널 히스토리 조회: channel=$channel, limit=$limit")
        
        return try {
            val history = slackService.getChannelHistory(channel, limit)
            ResponseEntity.ok(history)
        } catch (e: Exception) {
            logger.error("히스토리 조회 실패", e)
            ResponseEntity.status(500).body(emptyList())
        }
    }
    
    /**
     * 채널 통계 조회
     */
    @GetMapping("/stats/{channel}")
    suspend fun getChannelStats(@PathVariable channel: String): ResponseEntity<SlackChannelStats> {
        logger.info("채널 통계 조회: channel=$channel")
        
        return try {
            val stats = slackService.getChannelStats(channel)
            ResponseEntity.ok(stats)
        } catch (e: Exception) {
            logger.error("통계 조회 실패", e)
            ResponseEntity.status(500).body(
                SlackChannelStats(
                    channel = channel,
                    totalQuestions = 0,
                    uniqueAuthors = 0,
                    oldestQuestion = 0,
                    newestQuestion = 0
                )
            )
        }
    }
    
    /**
     * 헬스 체크
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "Slack Q&A Bot",
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    /**
     * 캐시 무효화 (최신 메시지 다시 읽기)
     */
    @PostMapping("/cache/invalidate")
    suspend fun invalidateCache(): ResponseEntity<Map<String, Any>> {
        logger.info("캐시 무효화 요청")
        
        return try {
            ResponseEntity.ok(mapOf(
                "status" to "SUCCESS",
                "message" to "캐시가 무효화되었습니다. 다음 검색에서 최신 데이터를 읽습니다.",
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            logger.error("캐시 무효화 실패", e)
            ResponseEntity.status(500).body(mapOf(
                "status" to "ERROR",
                "message" to "캐시 무효화 실패: ${e.message}",
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }
}