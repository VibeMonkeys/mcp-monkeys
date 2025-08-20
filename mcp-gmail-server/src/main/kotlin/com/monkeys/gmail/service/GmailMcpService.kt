package com.monkeys.gmail.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class GmailMcpService {

    @Tool(description = "Gmail 받은편지함의 메일 목록을 조회합니다")
    fun getMessages(
        @ToolParam(description = "조회할 메일 개수")
        maxResults: Int = 10,
        @ToolParam(description = "읽지 않은 메일만 조회")
        unreadOnly: Boolean = false
    ): List<GmailMessage> {
        // 더미 데이터 반환 (실제로는 Gmail API 연동 필요)
        return listOf(
            GmailMessage(
                id = "msg-001",
                subject = "프로젝트 진행 상황 공유",
                from = "colleague@company.com",
                to = listOf("me@company.com"),
                body = "안녕하세요, 이번 주 프로젝트 진행 상황을 공유드립니다.",
                isRead = false,
                receivedAt = "2024-01-20T09:30:00Z",
                labels = listOf("INBOX", "IMPORTANT")
            ),
            GmailMessage(
                id = "msg-002", 
                subject = "회의 일정 변경 안내",
                from = "manager@company.com",
                to = listOf("me@company.com"),
                body = "오늘 예정된 회의가 내일 오후 2시로 변경되었습니다.",
                isRead = true,
                receivedAt = "2024-01-20T08:15:00Z",
                labels = listOf("INBOX")
            )
        ).filter { if (unreadOnly) !it.isRead else true }
         .take(maxResults)
    }

    @Tool(description = "Gmail로 메일을 발송합니다")
    fun sendMessage(
        @ToolParam(description = "수신자 이메일", required = true)
        to: String,
        @ToolParam(description = "메일 제목", required = true)
        subject: String,
        @ToolParam(description = "메일 내용", required = true)
        body: String
    ): String {
        // 실제로는 Gmail API 호출
        return "메일이 성공적으로 발송되었습니다. (수신자: $to, 제목: $subject) - Gmail API 연동 필요"
    }

    @Tool(description = "Gmail 라벨 목록을 조회합니다")
    fun getLabels(): List<GmailLabel> {
        return listOf(
            GmailLabel("INBOX", "받은편지함", "system", 5, 25),
            GmailLabel("SENT", "보낸편지함", "system", 0, 15),
            GmailLabel("DRAFT", "임시보관함", "system", 2, 3),
            GmailLabel("IMPORTANT", "중요", "user", 3, 8)
        )
    }
}