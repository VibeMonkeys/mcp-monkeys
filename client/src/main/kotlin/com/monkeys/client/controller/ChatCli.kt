package com.monkeys.client.controller

import com.monkeys.client.service.McpService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ChatCli(private val mcpService: McpService) {

    @EventListener(ApplicationReadyEvent::class)
    fun startChat() {
        println("==================================================")
        println("MCP 대화형 클라이언트를 시작합니다. (현재: ${java.time.LocalDate.now()})")
        println("궁금한 것을 물어보세요. (종료하려면 'exit' 또는 'quit' 입력)")
        println("==================================================")

        val conversationHistory = mutableListOf<String>()

        while (true) {
            print("You: ")
            val userInput = readlnOrNull()

                        when {
                userInput.isNullOrBlank() -> continue
                                userInput.equals("/clear", ignoreCase = true) || userInput.equals("quit", ignoreCase = true) -> break
                userInput.equals("/clear", ignoreCase = true) -> {
                    conversationHistory.clear()
                    println("${ANSI_YELLOW}대화 기록이 초기화되었습니다.${ANSI_RESET}")
                    continue
                }
                                userInput.equals("/history", ignoreCase = true) -> {
                    if (conversationHistory.isEmpty()) {
                        println("${ANSI_YELLOW}아직 대화 기록이 없습니다.${ANSI_RESET}")
                    } else {
                        println("${ANSI_YELLOW}--- 대화 기록 시작 ---${ANSI_RESET}")
                        conversationHistory.forEach { println(it) }
                        println("${ANSI_YELLOW}--- 대화 기록 끝 ---${ANSI_RESET}")
                    }
                    continue
                }
                userInput.equals("/help", ignoreCase = true) -> {
                    println("${ANSI_YELLOW}--- 사용 가능한 명령어 ---${ANSI_RESET}")
                    println("${ANSI_YELLOW}/clear: 대화 기록을 초기화합니다.${ANSI_RESET}")
                    println("${ANSI_YELLOW}/history: 현재 대화 기록을 표시합니다.${ANSI_RESET}")
                    println("${ANSI_YELLOW}/help: 사용 가능한 명령어를 표시합니다.${ANSI_RESET}")
                    println("${ANSI_YELLOW}exit/quit: 클라이언트를 종료합니다.${ANSI_RESET}")
                    println("${ANSI_YELLOW}------------------------${ANSI_RESET}")
                    continue
                }
            }

            val thinkingMessage = "${ANSI_GREEN}Gemini: ...생각 중...${ANSI_RESET}"
            }

            val thinkingMessage = "Gemini: ...생각 중..."
            print(thinkingMessage)

            try {
                // 서비스 호출 시 대화 기록을 함께 전달합니다.
                val response = mcpService.getAndProcessData(userInput, conversationHistory).block()
                
                // 응답을 받으면, 질문과 답변을 모두 기록에 추가합니다.
                conversationHistory.add("USER: $userInput")
                conversationHistory.add("MODEL: $response")

                // "생각 중..." 메시지를 지우고 응답 출력
                print("" + " ".repeat(thinkingMessage.length) + "") // 메시지 길이만큼 공백으로 덮어쓰기
                println("Gemini: $response")
            } catch (e: Exception) {
                // "생각 중..." 메시지를 지우고 오류 메시지 출력
                print("" + " ".repeat(thinkingMessage.length) + "")
                println("
[오류] 요청 처리 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요. (${e.localizedMessage})")
            }
        }

        println("==================================================")
        println("클라이언트를 종료합니다.")
        println("==================================================")
    }
}
