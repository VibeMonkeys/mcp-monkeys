package com.monkeys.slack.repository

import com.monkeys.shared.dto.*

/**
 * Slack Repository Interface - Weather 서버 패턴과 동일
 * Q&A 데이터 조회 및 검색을 담당
 */
interface SlackRepository {
    
    /**
     * 채널별 Q&A 히스토리 조회
     */
    suspend fun getChannelQAHistory(channel: String, limit: Int = 50): List<SlackQAEntry>
    
    /**
     * 질문 유사도 검색
     */
    suspend fun searchSimilarQuestions(question: String, channel: String, threshold: Double): List<SlackQAMatch>
    
    /**
     * 새로운 Q&A 추가
     */
    suspend fun addQAEntry(entry: SlackQAEntry): Boolean
    
    /**
     * 채널 통계 조회
     */
    suspend fun getChannelStats(channel: String): SlackChannelStats
    
    /**
     * 채널 캐시 무효화 (새로운 Q&A 반영)
     */
    fun invalidateCache(channel: String)
    
    /**
     * 답변 선택을 위한 Intent 분석 (AI 기반 답변 선택용)
     */
    suspend fun analyzeIntentForAnswer(prompt: String): com.monkeys.slack.client.IntentAnalysisResult?
    
    /**
     * Gemini API를 직접 호출하여 답변 재가공
     */
    suspend fun reformatAnswerWithGemini(prompt: String): String
}