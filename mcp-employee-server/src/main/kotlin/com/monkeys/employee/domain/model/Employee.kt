package com.monkeys.employee.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

data class Employee(
    val id: Long = 0,
    val employeeNumber: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val department: Department? = null,
    val position: Position? = null,
    val hireDate: LocalDate,
    val resignDate: LocalDate? = null,
    val salary: BigDecimal = BigDecimal.ZERO,
    val status: EmployeeStatus = EmployeeStatus.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun changeDepartment(newDepartment: Department) = copy(
        department = newDepartment,
        updatedAt = LocalDateTime.now()
    )

    fun changePosition(newPosition: Position) = copy(
        position = newPosition,
        updatedAt = LocalDateTime.now()
    )

    fun updateSalary(newSalary: BigDecimal) = copy(
        salary = newSalary,
        updatedAt = LocalDateTime.now()
    )

    fun takeLeave(): Employee {
        if (status != EmployeeStatus.ACTIVE) return this
        return copy(status = EmployeeStatus.ON_LEAVE, updatedAt = LocalDateTime.now())
    }

    fun returnFromLeave(): Employee {
        if (status != EmployeeStatus.ON_LEAVE) return this
        return copy(status = EmployeeStatus.ACTIVE, updatedAt = LocalDateTime.now())
    }

    fun resign(date: LocalDate = LocalDate.now()) = copy(
        status = EmployeeStatus.RESIGNED,
        resignDate = date,
        updatedAt = LocalDateTime.now()
    )

    fun getYearsOfService(): Int {
        val endDate = resignDate ?: LocalDate.now()
        return Period.between(hireDate, endDate).years
    }
}

enum class EmployeeStatus {
    ACTIVE, ON_LEAVE, RESIGNED
}
