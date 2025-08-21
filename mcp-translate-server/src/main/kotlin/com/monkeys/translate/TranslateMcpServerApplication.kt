package com.monkeys.translate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TranslateMcpServerApplication

fun main(args: Array<String>) {
    runApplication<TranslateMcpServerApplication>(*args)
}