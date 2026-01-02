package com.monkeys.employee.adapter.`in`.mcp.dto

import com.monkeys.employee.domain.model.Employee
import com.monkeys.employee.domain.model.EmployeeStatus
import java.math.BigDecimal
import java.time.LocalDate

data class EmployeeDto(
    val id: Long,
    val employeeNumber: String,
    val name: String,
    val email: String,
    val phone: String?,
    val departmentName: String?,
    val positionName: String?,
    val hireDate: LocalDate,
    val resignDate: LocalDate?,
    val salary: BigDecimal,
    val status: EmployeeStatus,
    val yearsOfService: Int
) {
    companion object {
        fun fromDomain(employee: Employee): EmployeeDto = EmployeeDto(
            id = employee.id,
            employeeNumber = employee.employeeNumber,
            name = employee.name,
            email = employee.email,
            phone = employee.phone,
            departmentName = employee.department?.name,
            positionName = employee.position?.name,
            hireDate = employee.hireDate,
            resignDate = employee.resignDate,
            salary = employee.salary,
            status = employee.status,
            yearsOfService = employee.getYearsOfService()
        )
    }
}
