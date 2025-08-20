package com.monkeys.server.service

import com.monkeys.server.dto.*
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class AdvancedToolService(
    private val chatClient: ChatClient
) {

    @Tool(description = "사용자 입력을 기반으로 구조화된 작업 정보를 생성합니다")
    fun generateTaskInfo(
        @ToolParam(description = "작업에 대한 설명", required = true)
        taskDescription: String,
        @ToolParam(description = "담당자 이름")
        assignee: String? = null
    ): TaskInfo {
        val converter = BeanOutputConverter(TaskInfo::class.java)
        
        val prompt = """
        다음 작업 설명을 바탕으로 TaskInfo 구조체를 생성해주세요:
        
        작업 설명: $taskDescription
        담당자: ${assignee ?: "미정"}
        
        적절한 ID, 제목, 우선순위, 상태를 설정해주세요.
        ID는 UUID 형식으로, 상태는 TODO로 설정해주세요.
        
        ${converter.format}
        """.trimIndent()
        
        return try {
            val response = chatClient.prompt()
                .user(prompt)
                .call()
                .content() ?: throw IllegalStateException("응답을 받을 수 없습니다")
            
            converter.convert(response) ?: throw IllegalStateException("변환에 실패했습니다")
        } catch (e: Exception) {
            // 기본값 반환
            TaskInfo(
                id = UUID.randomUUID().toString(),
                title = taskDescription.take(50),
                description = taskDescription,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO,
                assignee = assignee
            )
        }
    }

    @Tool(description = "사용자 정보를 기반으로 구조화된 프로필을 생성합니다")
    fun generateUserProfile(
        @ToolParam(description = "사용자에 대한 설명 또는 정보", required = true)
        userDescription: String
    ): UserProfile {
        val converter = BeanOutputConverter(UserProfile::class.java)
        
        val prompt = """
        다음 사용자 정보를 바탕으로 UserProfile 구조체를 생성해주세요:
        
        사용자 정보: $userDescription
        
        적절한 이름, 나이, 이메일, 관심사, 기술 스택을 추출하거나 생성해주세요.
        이메일이 명시되지 않았다면 적절한 이메일을 생성해주세요.
        
        ${converter.format}
        """.trimIndent()
        
        return try {
            val response = chatClient.prompt()
                .user(prompt)
                .call()
                .content() ?: throw IllegalStateException("응답을 받을 수 없습니다")
            
            converter.convert(response) ?: throw IllegalStateException("변환에 실패했습니다")
        } catch (e: Exception) {
            // 기본값 반환
            UserProfile(
                name = "사용자",
                age = 25,
                email = "user@example.com",
                interests = listOf("기술", "학습"),
                skills = emptyList()
            )
        }
    }

    @Tool(description = "텍스트를 분석하여 구조화된 결과를 제공합니다")
    fun analyzeText(
        @ToolParam(description = "분석할 텍스트", required = true)
        text: String,
        @ToolParam(description = "분석 유형 (SENTIMENT, KEYWORD, SUMMARY)")
        analysisType: String = "SUMMARY"
    ): AnalysisResult {
        val converter = BeanOutputConverter(AnalysisResult::class.java)
        
        val analysisPrompt = when (analysisType.uppercase()) {
            "SENTIMENT" -> "다음 텍스트의 감정 분석을 수행하세요"
            "KEYWORD" -> "다음 텍스트에서 주요 키워드를 추출하고 분석하세요"
            "SUMMARY" -> "다음 텍스트를 요약하고 주요 포인트를 분석하세요"
            else -> "다음 텍스트를 종합적으로 분석하세요"
        }
        
        val prompt = """
        $analysisPrompt:
        
        텍스트: $text
        
        분석 유형: $analysisType
        
        분석 결과를 AnalysisResult 형태로 구조화해서 제공해주세요.
        각 카테고리별로 점수(0-100)와 설명을 포함하고, 개선사항이나 추천사항도 제공해주세요.
        
        ${converter.format}
        """.trimIndent()
        
        return try {
            val response = chatClient.prompt()
                .user(prompt)
                .call()
                .content() ?: throw IllegalStateException("응답을 받을 수 없습니다")
            
            converter.convert(response) ?: throw IllegalStateException("변환에 실패했습니다")
        } catch (e: Exception) {
            // 기본값 반환
            AnalysisResult(
                summary = "텍스트 분석 중 오류가 발생했습니다: ${e.message}",
                details = listOf(
                    AnalysisDetail("오류", 0.0, "분석을 완료할 수 없습니다")
                ),
                recommendations = listOf("다시 시도해 주세요")
            )
        }
    }

    @Tool(description = "가상의 날씨 정보를 생성합니다 (실제 API 연동 없이 테스트용)")
    fun generateWeatherInfo(
        @ToolParam(description = "날씨 정보를 가져올 위치", required = true)
        location: String
    ): WeatherInfo {
        // 실제로는 외부 API를 호출하겠지만, 여기서는 가상 데이터 생성
        val temperatures = listOf(-5.0, 0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0)
        val descriptions = listOf("맑음", "흐림", "비", "눈", "안개", "바람", "폭우", "폭설")
        val humidities = listOf(30, 40, 50, 60, 70, 80, 90)
        
        return WeatherInfo(
            location = location,
            temperature = temperatures.random(),
            description = descriptions.random(),
            humidity = humidities.random(),
            timestamp = LocalDateTime.now().toString()
        )
    }

    @Tool(description = "여러 도시의 날씨 정보를 한 번에 조회합니다")
    fun getMultiCityWeather(
        @ToolParam(description = "날씨를 조회할 도시들 (쉼표로 구분)", required = true)
        cities: String
    ): List<WeatherInfo> {
        val cityList = cities.split(",").map { it.trim() }
        return cityList.map { city ->
            generateWeatherInfo(city)
        }
    }
}