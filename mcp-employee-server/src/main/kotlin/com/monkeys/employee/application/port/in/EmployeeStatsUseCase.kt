package com.monkeys.employee.application.port.`in`

import com.monkeys.employee.domain.model.EmployeeStats

interface EmployeeStatsUseCase {
    fun getEmployeeStats(): EmployeeStats
}
