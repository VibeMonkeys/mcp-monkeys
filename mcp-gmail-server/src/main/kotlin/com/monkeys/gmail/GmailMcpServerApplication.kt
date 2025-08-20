package com.monkeys.gmail

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GmailMcpServerApplication

fun main(args: Array<String>) {
    runApplication<GmailMcpServerApplication>(*args)
}