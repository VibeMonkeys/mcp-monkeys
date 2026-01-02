package com.monkeys.employee.domain.model

import java.math.BigDecimal

data class EmployeeStats(
    val totalEmployees: Int,
    val activeEmployees: Int,
    val onLeaveEmployees: Int,
    val resignedEmployees: Int,
    val totalDepartments: Long,
    val totalPositions: Long,
    val averageSalary: BigDecimal,
    val averageYearsOfService: Double
)
