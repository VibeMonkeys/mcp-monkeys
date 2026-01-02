package com.monkeys.employee.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Position(
    val id: Long = 0,
    val name: String,
    val level: Int,
    val description: String? = null,
    val minSalary: BigDecimal = BigDecimal.ZERO,
    val maxSalary: BigDecimal = BigDecimal.ZERO,
    val employeeCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
