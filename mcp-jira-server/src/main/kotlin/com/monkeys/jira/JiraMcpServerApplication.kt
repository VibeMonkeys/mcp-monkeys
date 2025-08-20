package com.monkeys.jira

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JiraMcpServerApplication

fun main(args: Array<String>) {
    runApplication<JiraMcpServerApplication>(*args)
}