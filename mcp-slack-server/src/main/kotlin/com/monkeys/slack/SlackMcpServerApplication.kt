package com.monkeys.slack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Slack Q&A Bot MCP Server Application
 * Weather 서버 구조를 그대로 따름
 */
@SpringBootApplication
class SlackMcpServerApplication

fun main(args: Array<String>) {
    runApplication<SlackMcpServerApplication>(*args)
}