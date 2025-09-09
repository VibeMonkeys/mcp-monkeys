package com.monkeys.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.monkeys.client", "com.monkeys.shared"])
class McpClientApplication

fun main(args: Array<String>) {
    runApplication<McpClientApplication>(*args)
}