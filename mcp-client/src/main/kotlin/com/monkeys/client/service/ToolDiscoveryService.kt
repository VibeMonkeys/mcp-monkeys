package com.monkeys.client.service

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.ai.tool.ToolCallback
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * MCP 서버에서 동적으로 도구 정보를 조회하는 서비스
 */
@Service
class ToolDiscoveryService(
    private val toolCallbackProvider: SyncMcpToolCallbackProvider
) {

    private val logger = LoggerFactory.getLogger(ToolDiscoveryService::class.java)

    /**
     * 등록된 모든 도구 정보를 조회
     */
    fun getAvailableTools(): Map<String, List<ToolInfo>> {
        return try {
            val callbacks = toolCallbackProvider.getToolCallbacks()

            logger.info("등록된 MCP 도구 수: ${callbacks.size}")

            callbacks
                .mapNotNull { callback -> extractToolInfo(callback) }
                .groupBy { it.serverName }
                .mapValues { (_, tools) -> tools.sortedBy { it.name } }
        } catch (e: Exception) {
            logger.error("도구 목록 조회 실패", e)
            emptyMap()
        }
    }

    /**
     * ToolCallback에서 도구 정보 추출
     */
    private fun extractToolInfo(callback: ToolCallback): ToolInfo? {
        return try {
            val toolDefinition = callback.toolDefinition
            val name = toolDefinition.name()
            val description = toolDefinition.description() ?: ""

            // 도구 이름에서 서버 이름 추출 (예: "library_searchBooks" -> "Library")
            val serverName = extractServerName(name)

            ToolInfo(
                name = name,
                description = description,
                serverName = serverName
            )
        } catch (e: Exception) {
            logger.warn("도구 정보 추출 실패: ${callback.javaClass.simpleName}", e)
            null
        }
    }

    /**
     * 도구 이름에서 서버 이름 추출
     */
    private fun extractServerName(toolName: String): String {
        // 일반적으로 도구 이름은 "serverName_toolName" 형식
        // 또는 서버별로 그룹화된 경우 서버 이름 prefix가 있음
        val prefixMap = mapOf(
            "library" to "Library",
            "todo" to "Todo",
            "employee" to "Employee",
            "product" to "Product"
        )

        for ((prefix, serverName) in prefixMap) {
            if (toolName.lowercase().startsWith(prefix)) {
                return serverName
            }
        }

        // 언더스코어로 분리된 경우
        val parts = toolName.split("_")
        if (parts.size >= 2) {
            return parts[0].replaceFirstChar { it.uppercase() }
        }

        return "Unknown"
    }

    /**
     * 도구 정보 요약 조회 (서버별 도구 개수)
     */
    fun getToolsSummary(): Map<String, Int> {
        return getAvailableTools().mapValues { it.value.size }
    }

    /**
     * 특정 서버의 도구 목록 조회
     */
    fun getToolsByServer(serverName: String): List<ToolInfo> {
        return getAvailableTools()[serverName] ?: emptyList()
    }
}

/**
 * 도구 정보 데이터 클래스
 */
data class ToolInfo(
    val name: String,
    val description: String,
    val serverName: String
)
