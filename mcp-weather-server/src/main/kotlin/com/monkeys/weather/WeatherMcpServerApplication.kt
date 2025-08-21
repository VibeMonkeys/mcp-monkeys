package com.monkeys.weather

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WeatherMcpServerApplication

fun main(args: Array<String>) {
    runApplication<WeatherMcpServerApplication>(*args)
}