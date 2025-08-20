package com.monkeys.slack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SlackMcpServerApplication

fun main(args: Array<String>) {
    runApplication<SlackMcpServerApplication>(*args)
}