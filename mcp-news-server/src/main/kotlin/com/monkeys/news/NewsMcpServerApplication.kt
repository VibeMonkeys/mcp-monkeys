package com.monkeys.news

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NewsMcpServerApplication

fun main(args: Array<String>) {
    runApplication<NewsMcpServerApplication>(*args)
}