package com.monkeys.github

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GitHubMcpServerApplication

fun main(args: Array<String>) {
    runApplication<GitHubMcpServerApplication>(*args)
}