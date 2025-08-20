package com.monkeys.slack.service

import com.monkeys.shared.dto.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SlackMcpService {

    @Tool(description = "Slack 채널에 메시지를 전송합니다")
    fun sendMessage(
        @ToolParam(description = "채널 이름 또는 ID", required = true)
        channel: String,
        @ToolParam(description = "전송할 메시지", required = true)
        text: String
    ): String {
        // 실제로는 Slack API 호출
        return "메시지가 성공적으로 전송되었습니다. (채널: $channel, 내용: $text) - Slack API 연동 필요"
    }

    @Tool(description = "Slack 채널의 최근 메시지를 조회합니다")
    fun getMessages(
        @ToolParam(description = "채널 이름 또는 ID", required = true)
        channel: String,
        @ToolParam(description = "조회할 메시지 개수")
        limit: Int = 10
    ): List<SlackMessage> {
        // 더미 데이터 반환 (실제로는 Slack API 연동 필요)
        return listOf(
            SlackMessage(
                timestamp = Instant.now().minusSeconds(3600).toString(),
                text = "새로운 배포가 완료되었습니다! 🚀",
                user = "U123456",
                channel = channel,
                username = "deploy-bot",
                threadTimestamp = null,
                reactions = listOf(
                    SlackReaction("rocket", 3, listOf("U123456", "U789012", "U345678"))
                )
            ),
            SlackMessage(
                timestamp = Instant.now().minusSeconds(7200).toString(),
                text = "오늘 스탠드업 미팅은 10시에 진행됩니다.",
                user = "U789012",
                channel = channel,
                username = "팀리더",
                threadTimestamp = null,
                reactions = listOf(
                    SlackReaction("thumbsup", 5, listOf("U123456", "U345678", "U567890", "U890123", "U234567"))
                )
            ),
            SlackMessage(
                timestamp = Instant.now().minusSeconds(10800).toString(),
                text = "버그 수정이 완료되어 테스트 서버에 배포했습니다.",
                user = "U345678",
                channel = channel,
                username = "개발자A",
                threadTimestamp = null
            )
        ).take(limit)
    }

    @Tool(description = "Slack 채널 목록을 조회합니다")
    fun getChannels(): List<SlackChannel> {
        return listOf(
            SlackChannel(
                id = "C123456",
                name = "general",
                purpose = "회사 전체 공지사항",
                isPublic = true,
                memberCount = 150,
                createdAt = "2023-01-01T00:00:00Z"
            ),
            SlackChannel(
                id = "C789012",
                name = "dev-team",
                purpose = "개발팀 업무 소통",
                isPublic = false,
                memberCount = 12,
                createdAt = "2023-03-15T00:00:00Z"
            ),
            SlackChannel(
                id = "C345678",
                name = "random",
                purpose = "자유로운 대화",
                isPublic = true,
                memberCount = 89,
                createdAt = "2023-01-01T00:00:00Z"
            )
        )
    }

    @Tool(description = "Slack 워크스페이스의 사용자 목록을 조회합니다")
    fun getUsers(): List<SlackUser> {
        return listOf(
            SlackUser(
                id = "U123456",
                name = "john.doe",
                realName = "John Doe",
                email = "john.doe@company.com",
                profileImage = "https://avatars.slack-edge.com/2023/U123456.jpg",
                isOnline = true,
                statusText = "집중 중 🎯"
            ),
            SlackUser(
                id = "U789012", 
                name = "jane.smith",
                realName = "Jane Smith",
                email = "jane.smith@company.com",
                profileImage = "https://avatars.slack-edge.com/2023/U789012.jpg",
                isOnline = false,
                statusText = "회의 중"
            ),
            SlackUser(
                id = "U345678",
                name = "bob.wilson",
                realName = "Bob Wilson", 
                email = "bob.wilson@company.com",
                profileImage = null,
                isOnline = true,
                statusText = null
            )
        )
    }
}