package com.monkeys.news.repository.impl

import com.monkeys.news.repository.NewsRepository
import com.monkeys.shared.dto.NewsInfo
import com.monkeys.shared.dto.NewsRequest
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Mock 뉴스 데이터 제공 Repository
 * 외부 API 의존성 없이 하드코딩된 뉴스 정보 제공
 * 데모 및 테스트 환경에서 사용
 */
@Repository
@Profile("!external-api")
class MockNewsRepository : NewsRepository {
    
    private val logger = LoggerFactory.getLogger(MockNewsRepository::class.java)
    
    // 카테고리별 Mock 뉴스 데이터
    private val mockTechNews = listOf(
        NewsInfo(
            title = "ChatGPT-4o의 새로운 멀티모달 기능 공개",
            description = "OpenAI가 텍스트, 이미지, 음성을 통합 처리하는 차세대 AI 모델을 발표했습니다.",
            url = "https://example.com/tech/chatgpt-4o",
            author = "김기술",
            publishedAt = "2024-11-15T10:30:00Z",
            source = "테크리뷰",
            category = "technology",
            content = "OpenAI의 최신 AI 모델 ChatGPT-4o가 멀티모달 처리 능력을 대폭 향상시켰다고 발표했습니다..."
        ),
        NewsInfo(
            title = "애플, M4 칩 탑재 MacBook Pro 라인업 출시",
            description = "3나노 공정 기술을 적용한 M4 칩으로 성능과 전력 효율성을 대폭 개선했습니다.",
            url = "https://example.com/tech/m4-macbook",
            author = "박하드웨어",
            publishedAt = "2024-11-14T14:20:00Z",
            source = "애플인사이더",
            category = "technology",
            content = "애플이 차세대 M4 칩을 탑재한 MacBook Pro를 공식 발표했습니다..."
        ),
        NewsInfo(
            title = "구글, 제미나이 AI 모델에 실시간 웹 검색 기능 추가",
            description = "제미나이가 실시간으로 웹을 검색해 최신 정보를 제공하는 기능이 업데이트되었습니다.",
            url = "https://example.com/tech/gemini-web-search",
            author = "이인공지능",
            publishedAt = "2024-11-13T09:15:00Z",
            source = "AI뉴스",
            category = "technology",
            content = "구글의 제미나이 AI가 실시간 웹 검색 기능을 지원하기 시작했습니다..."
        )
    )
    
    private val mockBusinessNews = listOf(
        NewsInfo(
            title = "삼성전자, 3분기 영업이익 전년 대비 277% 증가",
            description = "메모리 반도체 수요 회복과 AI 칩 수요 증가로 실적이 크게 개선되었습니다.",
            url = "https://example.com/business/samsung-earnings",
            author = "최경제",
            publishedAt = "2024-11-12T16:45:00Z",
            source = "경제일보",
            category = "business",
            content = "삼성전자가 3분기 연결기준 영업이익이 전년 동기 대비 277% 증가했다고 발표했습니다..."
        ),
        NewsInfo(
            title = "SK하이닉스, HBM3E 양산 본격화로 AI 메모리 시장 선점",
            description = "차세대 고대역폭 메모리 HBM3E의 대량 생산을 시작하며 AI 시장 공략에 나섰습니다.",
            url = "https://example.com/business/sk-hynix-hbm",
            author = "김반도체",
            publishedAt = "2024-11-11T11:30:00Z",
            source = "반도체 투데이",
            category = "business",
            content = "SK하이닉스가 AI 가속기용 고성능 메모리 HBM3E의 본격 양산에 들어갔습니다..."
        )
    )
    
    private val mockSportsNews = listOf(
        NewsInfo(
            title = "손흥민, 토트넘 vs 맨시티전에서 결승골 성공",
            description = "손흥민이 프리미어리그 정상급 팀과의 경기에서 또 한 번 결정적인 골을 넣었습니다.",
            url = "https://example.com/sports/son-goal",
            author = "박축구",
            publishedAt = "2024-11-10T22:30:00Z",
            source = "스포츠월드",
            category = "sports",
            content = "토트넘의 손흥민이 맨체스터 시티와의 빅매치에서 결승골을 기록했습니다..."
        ),
        NewsInfo(
            title = "류현진, 토론토 블루제이스와 2년 연장 계약 체결",
            description = "류현진이 메이저리그에서의 활약을 이어갈 수 있게 되었습니다.",
            url = "https://example.com/sports/ryu-contract",
            author = "이야구",
            publishedAt = "2024-11-09T15:20:00Z",
            source = "베이스볼코리아",
            category = "sports",
            content = "류현진이 토론토 블루제이스와 2년 연장 계약을 체결했다고 구단이 발표했습니다..."
        )
    )
    
    private val mockGeneralNews = listOf(
        NewsInfo(
            title = "전국 첫눈 소식, 서울 한강 결빙 주의보",
            description = "올겨울 첫 한파와 함께 전국 곳곳에 눈이 내리기 시작했습니다.",
            url = "https://example.com/general/first-snow",
            author = "날씨팀",
            publishedAt = "2024-11-08T07:00:00Z",
            source = "기상청뉴스",
            category = "general",
            content = "기상청은 올겨울 첫 한파특보를 발령하며 전국에 첫눈이 내릴 것이라고 예보했습니다..."
        ),
        NewsInfo(
            title = "2024 수능 응시생 50만명 돌파, 역대 최다",
            description = "올해 대학수학능력시험 응시생이 50만명을 넘어서며 치열한 경쟁이 예상됩니다.",
            url = "https://example.com/general/suneung-2024",
            author = "교육부",
            publishedAt = "2024-11-07T18:30:00Z",
            source = "교육일보",
            category = "general",
            content = "2024학년도 대학수학능력시험 응시원서 접수가 마감되었습니다..."
        )
    )
    
    override suspend fun getNews(request: NewsRequest): List<NewsInfo> = withContext(Dispatchers.IO) {
        logger.info("Mock 뉴스 조회: category=${request.category}, country=${request.country}, pageSize=${request.pageSize}")
        
        val allNews = when (request.category?.lowercase()) {
            "technology", "tech" -> mockTechNews
            "business" -> mockBusinessNews
            "sports" -> mockSportsNews
            "general" -> mockGeneralNews
            else -> mockTechNews + mockBusinessNews + mockSportsNews + mockGeneralNews
        }
        
        // 페이징 처리
        val startIndex = (request.page - 1) * request.pageSize
        val endIndex = minOf(startIndex + request.pageSize, allNews.size)
        
        val results = if (startIndex < allNews.size) {
            allNews.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        logger.info("Mock 뉴스 응답: ${results.size}개 기사")
        results
    }
    
    override suspend fun getTopHeadlines(request: NewsRequest): List<NewsInfo> = withContext(Dispatchers.IO) {
        logger.info("Mock 헤드라인 뉴스 조회: country=${request.country}, category=${request.category}")
        
        // 헤드라인은 각 카테고리에서 최신 뉴스 1개씩 선택
        val headlines = mutableListOf<NewsInfo>()
        
        if (request.category == null || request.category.equals("technology", ignoreCase = true)) {
            headlines.addAll(mockTechNews.take(1))
        }
        if (request.category == null || request.category.equals("business", ignoreCase = true)) {
            headlines.addAll(mockBusinessNews.take(1))
        }
        if (request.category == null || request.category.equals("sports", ignoreCase = true)) {
            headlines.addAll(mockSportsNews.take(1))
        }
        if (request.category == null || request.category.equals("general", ignoreCase = true)) {
            headlines.addAll(mockGeneralNews.take(1))
        }
        
        val results = headlines.take(request.pageSize)
        logger.info("Mock 헤드라인 응답: ${results.size}개 기사")
        results
    }
    
    override suspend fun searchNews(query: String, language: String, sortBy: String, pageSize: Int, page: Int): List<NewsInfo> = withContext(Dispatchers.IO) {
        logger.info("Mock 뉴스 검색: query=$query, language=$language, sortBy=$sortBy")
        
        val allNews = mockTechNews + mockBusinessNews + mockSportsNews + mockGeneralNews
        
        // 제목이나 설명에서 검색어 포함된 뉴스 필터링
        val searchResults = allNews.filter { news ->
            news.title.contains(query, ignoreCase = true) ||
            news.description.contains(query, ignoreCase = true) ||
            news.content.contains(query, ignoreCase = true)
        }
        
        // 정렬 처리
        val sortedResults = when (sortBy) {
            "popularity" -> searchResults.sortedByDescending { it.source } // Mock: 소스명으로 인기도 대체
            "publishedAt" -> searchResults.sortedByDescending { it.publishedAt }
            else -> searchResults
        }
        
        // 페이징 처리
        val startIndex = (page - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, sortedResults.size)
        
        val results = if (startIndex < sortedResults.size) {
            sortedResults.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        logger.info("Mock 검색 응답: query='$query'에 대해 ${results.size}개 기사")
        results
    }
    
    override suspend fun checkApiHealth(): Boolean {
        logger.info("Mock News Repository 상태 확인")
        return true // Mock은 항상 정상
    }
}