package com.monkeys.library

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibraryServerApplication

fun main(args: Array<String>) {
    runApplication<LibraryServerApplication>(*args)
}
