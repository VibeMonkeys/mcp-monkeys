package com.monkeys.github.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

@Service
class GitHubMcpService(
    @Value("\${github.token:dummy-token}") private val githubToken: String
) {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val baseUrl = "https://api.github.com"

    @Tool(description = "GitHub 저장소의 이슈 목록을 조회합니다")
    fun getIssues(
        @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
        repository: String,
        @ToolParam(description = "이슈 상태 (open, closed, all)")
        state: String = "open",
        @ToolParam(description = "조회할 이슈 개수 (최대 100)")
        limit: Int = 10
    ): List<GitHubIssue> {
        return try {
            val url = "$baseUrl/repos/$repository/issues?state=$state&per_page=$limit"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $githubToken")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return listOf(createDummyIssue("API 호출 실패: ${response.code}"))
            }

            val jsonResponse = response.body?.string() ?: "[]"
            val issues: List<Map<String, Any>> = mapper.readValue(jsonResponse)
            
            issues.map { issue ->
                GitHubIssue(
                    number = issue["number"] as Int,
                    title = issue["title"] as String,
                    body = issue["body"] as String?,
                    state = issue["state"] as String,
                    author = (issue["user"] as? Map<String, Any>)?.get("login") as String?,
                    createdAt = issue["created_at"] as String?,
                    labels = (issue["labels"] as? List<Map<String, Any>>)
                        ?.map { it["name"] as String } ?: emptyList()
                )
            }
        } catch (e: Exception) {
            listOf(createDummyIssue("GitHub 이슈 조회 중 오류: ${e.message}"))
        }
    }

    @Tool(description = "GitHub 저장소의 Pull Request 목록을 조회합니다")
    fun getPullRequests(
        @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
        repository: String,
        @ToolParam(description = "PR 상태 (open, closed, all)")
        state: String = "open",
        @ToolParam(description = "조회할 PR 개수 (최대 100)")
        limit: Int = 10
    ): List<GitHubPullRequest> {
        return try {
            val url = "$baseUrl/repos/$repository/pulls?state=$state&per_page=$limit"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $githubToken")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return listOf(createDummyPR("API 호출 실패: ${response.code}"))
            }

            val jsonResponse = response.body?.string() ?: "[]"
            val prs: List<Map<String, Any>> = mapper.readValue(jsonResponse)
            
            prs.map { pr ->
                GitHubPullRequest(
                    number = pr["number"] as Int,
                    title = pr["title"] as String,
                    body = pr["body"] as String?,
                    state = pr["state"] as String,
                    head = (pr["head"] as Map<String, Any>)["ref"] as String,
                    base = (pr["base"] as Map<String, Any>)["ref"] as String,
                    author = (pr["user"] as? Map<String, Any>)?.get("login") as String?,
                    createdAt = pr["created_at"] as String?
                )
            }
        } catch (e: Exception) {
            listOf(createDummyPR("GitHub PR 조회 중 오류: ${e.message}"))
        }
    }

    @Tool(description = "GitHub 저장소 정보를 조회합니다")
    fun getRepository(
        @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
        repository: String
    ): GitHubRepository {
        return try {
            val url = "$baseUrl/repos/$repository"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $githubToken")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return createDummyRepository("API 호출 실패: ${response.code}")
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val repo: Map<String, Any> = mapper.readValue(jsonResponse)
            
            GitHubRepository(
                name = repo["name"] as String,
                fullName = repo["full_name"] as String,
                description = repo["description"] as String?,
                isPrivate = repo["private"] as Boolean,
                language = repo["language"] as String?,
                starCount = repo["stargazers_count"] as Int,
                forkCount = repo["forks_count"] as Int
            )
        } catch (e: Exception) {
            createDummyRepository("GitHub 저장소 조회 중 오류: ${e.message}")
        }
    }

    @Tool(description = "GitHub 저장소에 새로운 이슈를 생성합니다")
    fun createIssue(
        @ToolParam(description = "저장소 이름 (owner/repo 형식)", required = true)
        repository: String,
        @ToolParam(description = "이슈 제목", required = true)
        title: String,
        @ToolParam(description = "이슈 내용")
        body: String = "",
        @ToolParam(description = "라벨 목록 (쉼표로 구분)")
        labels: String = ""
    ): GitHubIssue {
        return try {
            val url = "$baseUrl/repos/$repository/issues"
            val requestBody = mapOf(
                "title" to title,
                "body" to body,
                "labels" to if (labels.isNotEmpty()) labels.split(",").map { it.trim() } else emptyList()
            )
            
            val json = mapper.writeValueAsString(requestBody)
            val request = Request.Builder()
                .url(url)
                .post(json.toRequestBody("application/json".toMediaType()))
                .header("Authorization", "Bearer $githubToken")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return createDummyIssue("이슈 생성 실패: ${response.code}")
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val issue: Map<String, Any> = mapper.readValue(jsonResponse)
            
            GitHubIssue(
                number = issue["number"] as Int,
                title = issue["title"] as String,
                body = issue["body"] as String?,
                state = issue["state"] as String,
                author = (issue["user"] as? Map<String, Any>)?.get("login") as String?,
                createdAt = issue["created_at"] as String?,
                labels = (issue["labels"] as? List<Map<String, Any>>)
                    ?.map { it["name"] as String } ?: emptyList()
            )
        } catch (e: Exception) {
            createDummyIssue("이슈 생성 중 오류: ${e.message}")
        }
    }

    // 더미 데이터 생성 함수들
    private fun createDummyIssue(errorMessage: String) = GitHubIssue(
        number = -1,
        title = "테스트 이슈 (API 토큰 설정 필요)",
        body = errorMessage,
        state = "open",
        author = "system",
        createdAt = "2024-01-01T00:00:00Z",
        labels = listOf("test")
    )

    private fun createDummyPR(errorMessage: String) = GitHubPullRequest(
        number = -1,
        title = "테스트 PR (API 토큰 설정 필요)",
        body = errorMessage,
        state = "open",
        head = "feature/test",
        base = "main",
        author = "system",
        createdAt = "2024-01-01T00:00:00Z"
    )

    private fun createDummyRepository(errorMessage: String) = GitHubRepository(
        name = "test-repo",
        fullName = "test-owner/test-repo",
        description = errorMessage,
        isPrivate = false,
        language = "Kotlin",
        starCount = 0,
        forkCount = 0
    )
}