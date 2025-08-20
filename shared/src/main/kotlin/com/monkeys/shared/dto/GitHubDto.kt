package com.monkeys.shared.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("GitHub 이슈 정보")
data class GitHubIssue(
    @JsonProperty(required = true)
    @JsonPropertyDescription("이슈 번호")
    val number: Int,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("이슈 제목")
    val title: String,
    
    @JsonPropertyDescription("이슈 내용")
    val body: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("이슈 상태 (open, closed)")
    val state: String,
    
    @JsonPropertyDescription("작성자")
    val author: String?,
    
    @JsonPropertyDescription("생성일")
    val createdAt: String?,
    
    @JsonPropertyDescription("라벨 목록")
    val labels: List<String> = emptyList()
)

@JsonClassDescription("GitHub Pull Request 정보")
data class GitHubPullRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("PR 번호")
    val number: Int,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("PR 제목")
    val title: String,
    
    @JsonPropertyDescription("PR 내용")
    val body: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("PR 상태 (open, closed, merged)")
    val state: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("소스 브랜치")
    val head: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("타겟 브랜치")
    val base: String,
    
    @JsonPropertyDescription("작성자")
    val author: String?,
    
    @JsonPropertyDescription("생성일")
    val createdAt: String?
)

@JsonClassDescription("GitHub 저장소 정보")
data class GitHubRepository(
    @JsonProperty(required = true)
    @JsonPropertyDescription("저장소 이름")
    val name: String,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("저장소 전체 이름 (owner/repo)")
    val fullName: String,
    
    @JsonPropertyDescription("저장소 설명")
    val description: String?,
    
    @JsonProperty(required = true)
    @JsonPropertyDescription("공개 여부")
    val isPrivate: Boolean,
    
    @JsonPropertyDescription("주요 언어")
    val language: String?,
    
    @JsonPropertyDescription("스타 개수")
    val starCount: Int = 0,
    
    @JsonPropertyDescription("포크 개수")
    val forkCount: Int = 0
)