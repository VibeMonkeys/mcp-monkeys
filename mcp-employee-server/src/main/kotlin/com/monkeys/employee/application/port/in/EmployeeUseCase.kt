package com.monkeys.employee.application.port.`in`

import com.monkeys.employee.domain.model.Employee
import com.monkeys.employee.domain.model.EmployeeStatus
import java.math.BigDecimal
import java.time.LocalDate

interface EmployeeUseCase {
    fun searchEmployees(name: String): List<Employee>
    fun findEmployeeByNumber(employeeNumber: String): Employee?
    fun findEmployeeById(id: Long): Employee?
    fun findEmployeesByDepartment(departmentId: Long): List<Employee>
    fun findEmployeesByStatus(status: EmployeeStatus): List<Employee>
    fun changeDepartment(employeeId: Long, departmentId: Long): Employee?
    fun changePosition(employeeId: Long, positionId: Long): Employee?
    fun updateSalary(employeeId: Long, newSalary: BigDecimal): Employee?
    fun takeLeave(employeeId: Long): Employee?
    fun returnFromLeave(employeeId: Long): Employee?
    fun resign(employeeId: Long, resignDate: LocalDate): Employee?
}
