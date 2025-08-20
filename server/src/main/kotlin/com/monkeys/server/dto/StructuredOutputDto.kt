package com.monkeys.server.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.time.LocalDateTime

@JsonClassDescription("날씨 정보를 나타내는 데이터 구조")
@JsonPropertyOrder("location", "temperature", "description", "humidity", "timestamp")
data class WeatherInfo(
    @JsonProperty(required = true)
    @JsonPropertyDescription("위치 (도시명 또는 지역명)")
    val location: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("온도 (섭씨)")
    val temperature: Double,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("날씨 설명 (맑음, 흐림, 비 등)")
    val description: String,
    
    @JsonPropertyDescription("습도 (퍼센트)")
    val humidity: Int?,
    
    @JsonPropertyDescription("정보 생성 시간")
    val timestamp: String = LocalDateTime.now().toString()
)

@JsonClassDescription("사용자 프로필 정보")
@JsonPropertyOrder("name", "age", "email", "interests", "skills")
data class UserProfile(
    @JsonProperty(required = true)
    @JsonPropertyDescription("사용자 이름")
    val name: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("나이")
    val age: Int,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("이메일 주소")
    val email: String,
    
    @JsonPropertyDescription("관심사 목록")
    val interests: List<String> = emptyList(),
    
    @JsonPropertyDescription("기술 스택")
    val skills: List<Skill> = emptyList()
)

@JsonClassDescription("기술 정보")
data class Skill(
    @JsonProperty(required = true)
    @JsonPropertyDescription("기술명")
    val name: String,
    
    @JsonPropertyDescription("숙련도 (1-5)")
    val level: Int?,
    
    @JsonPropertyDescription("사용 경험 (년)")
    val experience: Int?
)

@JsonClassDescription("작업 정보를 나타내는 데이터 구조")
@JsonPropertyOrder("id", "title", "description", "priority", "status", "assignee")
data class TaskInfo(
    @JsonProperty(required = true)
    @JsonPropertyDescription("작업 ID")
    val id: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("작업 제목")
    val title: String,
    
    @JsonPropertyDescription("작업 상세 설명")
    val description: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("우선순위 (HIGH, MEDIUM, LOW)")
    val priority: TaskPriority,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("작업 상태 (TODO, IN_PROGRESS, DONE)")
    val status: TaskStatus,
    
    @JsonPropertyDescription("담당자")
    val assignee: String?
)

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}

enum class TaskStatus {
    TODO, IN_PROGRESS, DONE
}

@JsonClassDescription("분석 결과 정보")
@JsonPropertyOrder("summary", "details", "recommendations")
data class AnalysisResult(
    @JsonProperty(required = true)
    @JsonPropertyDescription("분석 요약")
    val summary: String,
    
    @JsonPropertyDescription("상세 분석 내용")
    val details: List<AnalysisDetail> = emptyList(),
    
    @JsonPropertyDescription("추천사항")
    val recommendations: List<String> = emptyList()
)

@JsonClassDescription("분석 상세 항목")
data class AnalysisDetail(
    @JsonProperty(required = true)
    @JsonPropertyDescription("항목명")
    val category: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("점수 (0-100)")
    val score: Double,
    
    @JsonPropertyDescription("설명")
    val description: String?
)