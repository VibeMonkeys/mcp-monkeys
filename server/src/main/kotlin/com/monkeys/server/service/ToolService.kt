package com.monkeys.server.service

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

@Service
class ToolService {

    @Tool(description = "현재 날짜와 시간을 다양한 형식으로 가져옵니다")
    fun getCurrentDateTime(
        @ToolParam(description = "날짜 형식 패턴 (예: 'yyyy-MM-dd HH:mm:ss', 'ISO', 'SHORT')")
        format: String = "yyyy-MM-dd HH:mm:ss"
    ): String {
        val now = LocalDateTime.now()
        return when (format.uppercase()) {
            "ISO" -> now.toString()
            "SHORT" -> now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            else -> now.format(DateTimeFormatter.ofPattern(format))
        }
    }

    @Tool(description = "고유한 UUID(범용 고유 식별자)를 생성합니다")
    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    @Tool(description = "지정된 범위 내에서 임의의 숫자를 생성합니다")
    fun generateRandomNumber(
        @ToolParam(description = "최소값 (포함)", required = true)
        min: Int,
        @ToolParam(description = "최대값 (포함)", required = true)  
        max: Int
    ): Int {
        return Random.nextInt(min, max + 1)
    }

    @Tool(description = "간단한 수학 연산을 계산합니다")
    fun calculate(
        @ToolParam(description = "첫 번째 숫자", required = true)
        a: Double,
        @ToolParam(description = "두 번째 숫자", required = true)
        b: Double,
        @ToolParam(description = "연산 타입: ADD, SUBTRACT, MULTIPLY, DIVIDE", required = true)
        operation: String
    ): String {
        return try {
            val result = when (operation.uppercase()) {
                "ADD" -> a + b
                "SUBTRACT" -> a - b
                "MULTIPLY" -> a * b
                "DIVIDE" -> {
                    if (b == 0.0) return "Error: Division by zero"
                    a / b
                }
                else -> return "Error: Unsupported operation. Use ADD, SUBTRACT, MULTIPLY, or DIVIDE"
            }
            "Result: $result"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    @Tool(description = "텍스트를 다양한 대소문자 형식으로 변환합니다")
    fun convertTextCase(
        @ToolParam(description = "변환할 텍스트", required = true)
        text: String,
        @ToolParam(description = "목표 형식: UPPER, LOWER, TITLE, CAMEL", required = true)
        targetCase: String
    ): String {
        return when (targetCase.uppercase()) {
            "UPPER" -> text.uppercase()
            "LOWER" -> text.lowercase()
            "TITLE" -> text.split(" ").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }
            "CAMEL" -> {
                val words = text.split(" ")
                words.first().lowercase() + words.drop(1).joinToString("") { 
                    it.lowercase().replaceFirstChar(Char::uppercase) 
                }
            }
            else -> "Error: Unsupported case. Use UPPER, LOWER, TITLE, or CAMEL"
        }
    }

    @Tool(description = "시스템 정보와 상태를 조회합니다")
    fun getSystemInfo(): String {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        val usedMemory = totalMemory - freeMemory
        
        return """
            System Information:
            - Java Version: ${System.getProperty("java.version")}
            - OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")}
            - Available Processors: ${runtime.availableProcessors()}
            - Memory Usage: ${usedMemory}MB / ${totalMemory}MB (Max: ${maxMemory}MB)
            - Current Time: ${LocalDateTime.now()}
        """.trimIndent()
    }
}