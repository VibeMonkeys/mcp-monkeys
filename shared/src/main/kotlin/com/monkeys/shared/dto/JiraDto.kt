package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("Jira 이슈 정보")
data class JiraIssue(
    @JsonProperty(required = true)
    @JsonPropertyDescription("이슈 키 (예: PROJ-123)")
    val key: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("이슈 제목")
    val summary: String,
    
    @JsonPropertyDescription("이슈 설명")
    val description: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("이슈 타입 (Bug, Task, Story 등)")
    val issueType: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("이슈 상태 (To Do, In Progress, Done 등)")
    val status: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("우선순위 (Highest, High, Medium, Low, Lowest)")
    val priority: String,
    
    @JsonPropertyDescription("담당자")
    val assignee: String?,
    
    @JsonPropertyDescription("리포터")
    val reporter: String?,
    
    @JsonPropertyDescription("생성일")
    val createdAt: String?,
    
    @JsonPropertyDescription("업데이트일")
    val updatedAt: String?,
    
    @JsonPropertyDescription("라벨 목록")
    val labels: List<String> = emptyList()
)

@JsonClassDescription("Jira 프로젝트 정보")
data class JiraProject(
    @JsonProperty(required = true)
    @JsonPropertyDescription("프로젝트 키")
    val key: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("프로젝트 이름")
    val name: String,
    
    @JsonPropertyDescription("프로젝트 설명")
    val description: String?,
    
    @JsonPropertyDescription("프로젝트 리더")
    val lead: String?,
    
    @JsonPropertyDescription("프로젝트 타입")
    val projectType: String?
)

@JsonClassDescription("Jira 스프린트 정보")
data class JiraSprint(
    @JsonProperty(required = true)
    @JsonPropertyDescription("스프린트 ID")
    val id: Long,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("스프린트 이름")
    val name: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("스프린트 상태 (future, active, closed)")
    val state: String,
    
    @JsonPropertyDescription("시작일")
    val startDate: String?,
    
    @JsonPropertyDescription("종료일")
    val endDate: String?,
    
    @JsonPropertyDescription("완료일")
    val completeDate: String?
)