package com.monkeys.calendar

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CalendarMcpServerApplication

fun main(args: Array<String>) {
    runApplication<CalendarMcpServerApplication>(*args)
}