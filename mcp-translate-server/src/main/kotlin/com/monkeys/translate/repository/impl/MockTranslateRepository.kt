package com.monkeys.translate.repository.impl

import com.monkeys.translate.repository.TranslateRepository
import com.monkeys.shared.dto.TranslateRequest
import com.monkeys.shared.dto.TranslateResult
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*

/**
 * Mock 번역 데이터 제공 Repository
 * 외부 API 의존성 없이 하드코딩된 번역 사전 제공
 * 데모 및 테스트 환경에서 사용
 */
@Repository
@Profile("!external-api")
class MockTranslateRepository : TranslateRepository {
    
    private val logger = LoggerFactory.getLogger(MockTranslateRepository::class.java)
    
    // 한영 번역 사전
    private val koreanToEnglish = mapOf(
        "안녕하세요" to "Hello",
        "안녕" to "Hi",
        "감사합니다" to "Thank you",
        "고맙습니다" to "Thank you",
        "죄송합니다" to "I'm sorry",
        "미안합니다" to "I'm sorry",
        "사랑해요" to "I love you",
        "사랑" to "Love",
        "친구" to "Friend",
        "가족" to "Family",
        "집" to "House",
        "학교" to "School",
        "회사" to "Company",
        "음식" to "Food",
        "물" to "Water",
        "커피" to "Coffee",
        "차" to "Tea",
        "책" to "Book",
        "컴퓨터" to "Computer",
        "휴대폰" to "Mobile phone",
        "자동차" to "Car",
        "비행기" to "Airplane",
        "기차" to "Train",
        "버스" to "Bus",
        "시간" to "Time",
        "날짜" to "Date",
        "오늘" to "Today",
        "내일" to "Tomorrow",
        "어제" to "Yesterday",
        "지금" to "Now",
        "여기" to "Here",
        "거기" to "There",
        "어디" to "Where",
        "언제" to "When",
        "누구" to "Who",
        "무엇" to "What",
        "왜" to "Why",
        "어떻게" to "How",
        "좋아요" to "Good",
        "나빠요" to "Bad",
        "큰" to "Big",
        "작은" to "Small",
        "예쁜" to "Pretty",
        "멋진" to "Cool",
        "빠른" to "Fast",
        "느린" to "Slow",
        "뜨거운" to "Hot",
        "차가운" to "Cold",
        "맛있는" to "Delicious",
        "재미있는" to "Interesting",
        "날씨" to "Weather",
        "맑음" to "Clear",
        "흐림" to "Cloudy",
        "비" to "Rain",
        "눈" to "Snow",
        "바람" to "Wind",
        "태양" to "Sun",
        "달" to "Moon",
        "별" to "Star",
        "하늘" to "Sky",
        "바다" to "Sea",
        "산" to "Mountain",
        "강" to "River",
        "꽃" to "Flower",
        "나무" to "Tree",
        "동물" to "Animal",
        "개" to "Dog",
        "고양이" to "Cat",
        "새" to "Bird",
        "물고기" to "Fish"
    )
    
    // 영한 번역 사전 (한영 사전을 뒤집어서 생성)
    private val englishToKorean = koreanToEnglish.entries.associate { it.value to it.key }
    
    // 일반적인 문장 번역 패턴
    private val sentencePatterns = mapOf(
        // 한국어 -> 영어
        "안녕하세요!" to "Hello!",
        "안녕히 가세요" to "Goodbye",
        "안녕히 계세요" to "Goodbye",
        "만나서 반갑습니다" to "Nice to meet you",
        "이름이 무엇입니까?" to "What is your name?",
        "몇 살이세요?" to "How old are you?",
        "어디서 왔어요?" to "Where are you from?",
        "한국어를 배우고 있어요" to "I am learning Korean",
        "영어를 배우고 있어요" to "I am learning English",
        "도움이 필요해요" to "I need help",
        "이해해요" to "I understand",
        "모르겠어요" to "I don't know",
        "다시 말해주세요" to "Please say it again",
        "천천히 말해주세요" to "Please speak slowly",
        "한국 음식을 좋아해요" to "I like Korean food",
        "김치가 맵아요" to "Kimchi is spicy",
        "한국이 아름다워요" to "Korea is beautiful",
        "서울에 살아요" to "I live in Seoul",
        "지금 몇 시예요?" to "What time is it now?",
        "날씨가 좋아요" to "The weather is nice",
        "오늘은 바빠요" to "I am busy today",
        "내일 시간 있어요?" to "Do you have time tomorrow?",
        
        // 영어 -> 한국어  
        "Hello!" to "안녕하세요!",
        "Goodbye" to "안녕히 가세요",
        "Thank you very much" to "정말 감사합니다",
        "You're welcome" to "천만에요",
        "Excuse me" to "실례합니다",
        "I'm sorry" to "죄송합니다",
        "Nice to meet you" to "만나서 반갑습니다",
        "What is your name?" to "이름이 무엇입니까?",
        "How are you?" to "어떻게 지내세요?",
        "I'm fine" to "잘 지내요",
        "Where are you from?" to "어디서 왔어요?",
        "I'm from Korea" to "저는 한국에서 왔어요",
        "I don't understand" to "이해 못하겠어요",
        "Please help me" to "도와주세요",
        "How much is this?" to "이것이 얼마예요?",
        "Where is the bathroom?" to "화장실이 어디예요?",
        "I love you" to "사랑해요",
        "Good morning" to "좋은 아침",
        "Good night" to "안녕히 주무세요",
        "See you later" to "나중에 봐요",
        "Have a good day" to "좋은 하루 보내세요"
    )
    
    override suspend fun translateText(request: TranslateRequest): TranslateResult = withContext(Dispatchers.IO) {
        logger.info("Mock 번역 요청: ${request.sourceLanguage} -> ${request.targetLanguage}, text='${request.text}'")
        
        val sourceText = request.text.trim()
        val translatedText = when {
            // 정확한 문장 패턴 매칭
            sentencePatterns.containsKey(sourceText) -> {
                sentencePatterns[sourceText]!!
            }
            
            // 한국어 -> 영어
            request.sourceLanguage == "ko" && request.targetLanguage == "en" -> {
                translateKoreanToEnglish(sourceText)
            }
            
            // 영어 -> 한국어
            request.sourceLanguage == "en" && request.targetLanguage == "ko" -> {
                translateEnglishToKorean(sourceText)
            }
            
            // 동일 언어
            request.sourceLanguage == request.targetLanguage -> {
                sourceText
            }
            
            // 지원하지 않는 언어 쌍
            else -> {
                "지원하지 않는 언어 조합입니다: ${request.sourceLanguage} -> ${request.targetLanguage}"
            }
        }
        
        val result = TranslateResult(
            originalText = request.text,
            translatedText = translatedText,
            sourceLanguage = request.sourceLanguage,
            targetLanguage = request.targetLanguage,
            confidence = if (translatedText.startsWith("지원하지 않는")) 0.0 else 0.95,
            detectedLanguage = request.sourceLanguage
        )
        
        logger.info("Mock 번역 결과: '${request.text}' -> '$translatedText' (confidence: ${result.confidence})")
        result
    }
    
    private fun translateKoreanToEnglish(text: String): String {
        // 완전 일치 확인
        koreanToEnglish[text]?.let { return it }
        
        // 단어별 번역 시도
        val words = text.split(" ")
        if (words.size > 1) {
            val translatedWords = words.map { word ->
                koreanToEnglish[word] ?: word
            }
            val hasTranslation = translatedWords.any { it in koreanToEnglish.values }
            if (hasTranslation) {
                return translatedWords.joinToString(" ")
            }
        }
        
        // 부분 일치 확인
        for ((korean, english) in koreanToEnglish) {
            if (text.contains(korean)) {
                return text.replace(korean, english)
            }
        }
        
        return "Translation not available for: $text"
    }
    
    private fun translateEnglishToKorean(text: String): String {
        // 완전 일치 확인
        englishToKorean[text]?.let { return it }
        
        // 대소문자 구분 없이 확인
        englishToKorean[text.lowercase()]?.let { return it }
        englishToKorean[text.lowercase().replaceFirstChar { it.uppercase() }]?.let { return it }
        
        // 단어별 번역 시도
        val words = text.split(" ")
        if (words.size > 1) {
            val translatedWords = words.map { word ->
                englishToKorean[word.lowercase()] 
                    ?: englishToKorean[word.lowercase().replaceFirstChar { it.uppercase() }]
                    ?: word
            }
            val hasTranslation = translatedWords.any { it in englishToKorean.values }
            if (hasTranslation) {
                return translatedWords.joinToString(" ")
            }
        }
        
        // 부분 일치 확인
        for ((english, korean) in englishToKorean) {
            if (text.contains(english, ignoreCase = true)) {
                return text.replace(english, korean, ignoreCase = true)
            }
        }
        
        return "번역을 사용할 수 없습니다: $text"
    }
    
    override suspend fun detectLanguage(text: String): String = withContext(Dispatchers.IO) {
        logger.info("Mock 언어 감지: text='$text'")
        
        val detectedLang = when {
            // 한국어 문자 포함 확인 (한글 유니코드 범위: AC00-D7A3)
            text.any { it.code in 0xAC00..0xD7A3 } -> "ko"
            
            // 영어 알파벳만 포함
            text.all { it.isLetter() && it.code < 128 || it.isWhitespace() || it.isDigit() } -> "en"
            
            // 기본값
            else -> "unknown"
        }
        
        logger.info("Mock 언어 감지 결과: '$text' -> $detectedLang")
        detectedLang
    }
    
    override suspend fun getSupportedLanguages(): Map<String, String> = withContext(Dispatchers.IO) {
        logger.info("Mock 지원 언어 목록 조회")
        mapOf(
            "ko" to "Korean",
            "en" to "English"
        )
    }
    
    override suspend fun checkApiHealth(): Boolean {
        logger.info("Mock Translate Repository 상태 확인")
        return true // Mock은 항상 정상
    }
}