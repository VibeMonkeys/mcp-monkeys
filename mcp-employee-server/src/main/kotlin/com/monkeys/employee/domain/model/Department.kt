package com.monkeys.employee.domain.model

import java.time.LocalDateTime

data class Department(
    val id: Long = 0,
    val name: String,
    val code: String,
    val description: String? = null,
    val managerId: Long? = null,
    val managerName: String? = null,
    val employeeCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
