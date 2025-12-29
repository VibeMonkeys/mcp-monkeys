package com.monkeys.product

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProductServerApplication

fun main(args: Array<String>) {
    runApplication<ProductServerApplication>(*args)
}
