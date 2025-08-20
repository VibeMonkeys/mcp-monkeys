package com.monkeys.jira.service

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
import java.util.*

@Service
class JiraMcpService(
    @Value("\${jira.url:https://your-domain.atlassian.net}") private val jiraUrl: String,
    @Value("\${jira.email:dummy@email.com}") private val jiraEmail: String,
    @Value("\${jira.token:dummy-token}") private val jiraToken: String
) {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()

    @Tool(description = "Jira 프로젝트의 이슈 목록을 조회합니다")
    fun getIssues(
        @ToolParam(description = "프로젝트 키 (예: PROJ)", required = true)
        projectKey: String,
        @ToolParam(description = "이슈 타입 (Bug, Task, Story 등)")
        issueType: String = "",
        @ToolParam(description = "이슈 상태 (To Do, In Progress, Done 등)")
        status: String = "",
        @ToolParam(description = "조회할 이슈 개수 (최대 100)")
        maxResults: Int = 10
    ): List<JiraIssue> {
        return try {
            var jql = "project = $projectKey"
            if (issueType.isNotEmpty()) jql += " AND issuetype = '$issueType'"
            if (status.isNotEmpty()) jql += " AND status = '$status'"
            jql += " ORDER BY created DESC"

            val url = "$jiraUrl/rest/api/3/search?jql=${java.net.URLEncoder.encode(jql, "UTF-8")}&maxResults=$maxResults"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$jiraEmail:$jiraToken".toByteArray())}")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return listOf(createDummyIssue("API 호출 실패: ${response.code}"))
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val searchResult: Map<String, Any> = mapper.readValue(jsonResponse)
            val issues = searchResult["issues"] as? List<Map<String, Any>> ?: emptyList()
            
            issues.map { issue ->
                val fields = issue["fields"] as Map<String, Any>
                JiraIssue(
                    key = issue["key"] as String,
                    summary = fields["summary"] as String,
                    description = fields["description"] as String?,
                    issueType = (fields["issuetype"] as? Map<String, Any>)?.get("name") as String? ?: "Unknown",
                    status = (fields["status"] as? Map<String, Any>)?.get("name") as String? ?: "Unknown",
                    priority = (fields["priority"] as? Map<String, Any>)?.get("name") as String? ?: "Medium",
                    assignee = (fields["assignee"] as? Map<String, Any>)?.get("displayName") as String?,
                    reporter = (fields["reporter"] as? Map<String, Any>)?.get("displayName") as String?,
                    createdAt = fields["created"] as String?,
                    updatedAt = fields["updated"] as String?,
                    labels = fields["labels"] as? List<String> ?: emptyList()
                )
            }
        } catch (e: Exception) {
            listOf(createDummyIssue("Jira 이슈 조회 중 오류: ${e.message}"))
        }
    }

    @Tool(description = "Jira에 새로운 이슈를 생성합니다")
    fun createIssue(
        @ToolParam(description = "프로젝트 키 (예: PROJ)", required = true)
        projectKey: String,
        @ToolParam(description = "이슈 제목", required = true)
        summary: String,
        @ToolParam(description = "이슈 설명")
        description: String = "",
        @ToolParam(description = "이슈 타입 (Bug, Task, Story 등)")
        issueType: String = "Task",
        @ToolParam(description = "우선순위 (Highest, High, Medium, Low, Lowest)")
        priority: String = "Medium"
    ): JiraIssue {
        return try {
            val url = "$jiraUrl/rest/api/3/issue"
            val requestBody = mapOf(
                "fields" to mapOf(
                    "project" to mapOf("key" to projectKey),
                    "summary" to summary,
                    "description" to mapOf(
                        "type" to "doc",
                        "version" to 1,
                        "content" to listOf(
                            mapOf(
                                "type" to "paragraph",
                                "content" to listOf(
                                    mapOf(
                                        "text" to description,
                                        "type" to "text"
                                    )
                                )
                            )
                        )
                    ),
                    "issuetype" to mapOf("name" to issueType),
                    "priority" to mapOf("name" to priority)
                )
            )
            
            val json = mapper.writeValueAsString(requestBody)
            val request = Request.Builder()
                .url(url)
                .post(json.toRequestBody("application/json".toMediaType()))
                .header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$jiraEmail:$jiraToken".toByteArray())}")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return createDummyIssue("이슈 생성 실패: ${response.code}")
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val issue: Map<String, Any> = mapper.readValue(jsonResponse)
            
            JiraIssue(
                key = issue["key"] as String,
                summary = summary,
                description = description,
                issueType = issueType,
                status = "To Do",
                priority = priority,
                assignee = null,
                reporter = jiraEmail,
                createdAt = java.time.Instant.now().toString(),
                updatedAt = java.time.Instant.now().toString(),
                labels = emptyList()
            )
        } catch (e: Exception) {
            createDummyIssue("이슈 생성 중 오류: ${e.message}")
        }
    }

    @Tool(description = "Jira 프로젝트 정보를 조회합니다")
    fun getProject(
        @ToolParam(description = "프로젝트 키 (예: PROJ)", required = true)
        projectKey: String
    ): JiraProject {
        return try {
            val url = "$jiraUrl/rest/api/3/project/$projectKey"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$jiraEmail:$jiraToken".toByteArray())}")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return createDummyProject("API 호출 실패: ${response.code}")
            }

            val jsonResponse = response.body?.string() ?: "{}"
            val project: Map<String, Any> = mapper.readValue(jsonResponse)
            
            JiraProject(
                key = project["key"] as String,
                name = project["name"] as String,
                description = project["description"] as String?,
                lead = (project["lead"] as? Map<String, Any>)?.get("displayName") as String?,
                projectType = (project["projectTypeKey"] as? String)
            )
        } catch (e: Exception) {
            createDummyProject("프로젝트 조회 중 오류: ${e.message}")
        }
    }

    @Tool(description = "Jira 프로젝트의 활성 스프린트 목록을 조회합니다")
    fun getActiveSprints(
        @ToolParam(description = "프로젝트 키 (예: PROJ)", required = true)
        projectKey: String
    ): List<JiraSprint> {
        return try {
            // 보드 정보를 먼저 조회
            val boardUrl = "$jiraUrl/rest/agile/1.0/board?projectKeyOrId=$projectKey"
            val boardRequest = Request.Builder()
                .url(boardUrl)
                .header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$jiraEmail:$jiraToken".toByteArray())}")
                .header("Accept", "application/json")
                .build()

            val boardResponse = client.newCall(boardRequest).execute()
            if (!boardResponse.isSuccessful) {
                return listOf(createDummySprint("보드 조회 실패: ${boardResponse.code}"))
            }

            val boardJson = boardResponse.body?.string() ?: "{}"
            val boardResult: Map<String, Any> = mapper.readValue(boardJson)
            val boards = boardResult["values"] as? List<Map<String, Any>> ?: emptyList()
            
            if (boards.isEmpty()) {
                return listOf(createDummySprint("활성 보드가 없습니다"))
            }
            
            val boardId = boards.first()["id"]
            
            // 스프린트 조회
            val sprintUrl = "$jiraUrl/rest/agile/1.0/board/$boardId/sprint?state=active"
            val sprintRequest = Request.Builder()
                .url(sprintUrl)
                .header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$jiraEmail:$jiraToken".toByteArray())}")
                .header("Accept", "application/json")
                .build()

            val sprintResponse = client.newCall(sprintRequest).execute()
            if (!sprintResponse.isSuccessful) {
                return listOf(createDummySprint("스프린트 조회 실패: ${sprintResponse.code}"))
            }

            val sprintJson = sprintResponse.body?.string() ?: "{}"
            val sprintResult: Map<String, Any> = mapper.readValue(sprintJson)
            val sprints = sprintResult["values"] as? List<Map<String, Any>> ?: emptyList()
            
            sprints.map { sprint ->
                JiraSprint(
                    id = (sprint["id"] as Number).toLong(),
                    name = sprint["name"] as String,
                    state = sprint["state"] as String,
                    startDate = sprint["startDate"] as String?,
                    endDate = sprint["endDate"] as String?,
                    completeDate = sprint["completeDate"] as String?
                )
            }
        } catch (e: Exception) {
            listOf(createDummySprint("스프린트 조회 중 오류: ${e.message}"))
        }
    }

    // 더미 데이터 생성 함수들
    private fun createDummyIssue(errorMessage: String) = JiraIssue(
        key = "TEST-1",
        summary = "테스트 이슈 (API 설정 필요)",
        description = errorMessage,
        issueType = "Task",
        status = "To Do",
        priority = "Medium",
        assignee = null,
        reporter = "system",
        createdAt = "2024-01-01T00:00:00.000+0000",
        updatedAt = "2024-01-01T00:00:00.000+0000",
        labels = listOf("test")
    )

    private fun createDummyProject(errorMessage: String) = JiraProject(
        key = "TEST",
        name = "테스트 프로젝트",
        description = errorMessage,
        lead = "system",
        projectType = "software"
    )

    private fun createDummySprint(errorMessage: String) = JiraSprint(
        id = -1,
        name = "테스트 스프린트",
        state = "active",
        startDate = "2024-01-01T00:00:00.000Z",
        endDate = "2024-01-14T00:00:00.000Z",
        completeDate = null
    )
}