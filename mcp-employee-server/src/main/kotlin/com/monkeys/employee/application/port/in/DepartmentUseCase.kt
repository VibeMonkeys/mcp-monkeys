package com.monkeys.employee.application.port.`in`

import com.monkeys.employee.domain.model.Department
import com.monkeys.employee.domain.model.Employee

interface DepartmentUseCase {
    fun findAllDepartments(): List<Department>
    fun findDepartmentById(id: Long): Department?
    fun findEmployeesByDepartment(departmentId: Long): List<Employee>
}
