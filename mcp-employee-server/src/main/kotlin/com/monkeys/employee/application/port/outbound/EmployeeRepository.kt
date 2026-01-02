package com.monkeys.employee.application.port.outbound

import com.monkeys.employee.domain.model.Employee
import com.monkeys.employee.domain.model.EmployeeStatus
import java.math.BigDecimal

interface EmployeeRepository {
    fun findById(id: Long): Employee?
    fun findByEmployeeNumber(employeeNumber: String): Employee?
    fun findByNameContaining(name: String): List<Employee>
    fun findByDepartmentId(departmentId: Long): List<Employee>
    fun findByStatus(status: EmployeeStatus): List<Employee>
    fun findAll(): List<Employee>
    fun save(employee: Employee): Employee
    fun count(): Long
    fun countByStatus(status: EmployeeStatus): Long
    fun calculateAverageSalary(): BigDecimal?
    fun calculateAverageYearsOfService(): Double?
}
