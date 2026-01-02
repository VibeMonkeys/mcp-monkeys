package com.monkeys.employee.adapter.`in`.mcp.dto

import com.monkeys.employee.domain.model.EmployeeStats
import java.math.BigDecimal

data class EmployeeStatsDto(
    val totalEmployees: Int,
    val activeEmployees: Int,
    val onLeaveEmployees: Int,
    val resignedEmployees: Int,
    val totalDepartments: Long,
    val totalPositions: Long,
    val averageSalary: BigDecimal,
    val averageYearsOfService: Double
) {
    companion object {
        fun fromDomain(stats: EmployeeStats): EmployeeStatsDto = EmployeeStatsDto(
            totalEmployees = stats.totalEmployees,
            activeEmployees = stats.activeEmployees,
            onLeaveEmployees = stats.onLeaveEmployees,
            resignedEmployees = stats.resignedEmployees,
            totalDepartments = stats.totalDepartments,
            totalPositions = stats.totalPositions,
            averageSalary = stats.averageSalary,
            averageYearsOfService = stats.averageYearsOfService
        )
    }
}
