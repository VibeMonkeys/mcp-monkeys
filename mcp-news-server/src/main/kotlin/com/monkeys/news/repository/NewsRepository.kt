package com.monkeys.news.repository

import com.monkeys.shared.dto.*

/**
 * 뉴스 데이터 액세스 인터페이스
 * Repository 패턴 적용
 */
interface NewsRepository {
    
    /**
     * 최신 헤드라인 조회
     */
    suspend fun getTopHeadlines(request: NewsHeadlinesRequest): List<NewsArticle>
    
    /**
     * 뉴스 검색
     */
    suspend fun searchNews(request: NewsSearchRequest): List<NewsArticle>
    
    /**
     * 출처별 뉴스 조회
     */
    suspend fun getNewsBySource(request: NewsBySourceRequest): List<NewsArticle>
    
    /**
     * API 상태 확인
     */
    suspend fun checkApiHealth(): Boolean
}