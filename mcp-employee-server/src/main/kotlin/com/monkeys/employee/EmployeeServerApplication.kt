package com.monkeys.employee

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EmployeeServerApplication

fun main(args: Array<String>) {
    runApplication<EmployeeServerApplication>(*args)
}
