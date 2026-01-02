package com.monkeys.employee.application.port.outbound

import com.monkeys.employee.domain.model.Department

interface DepartmentRepository {
    fun findById(id: Long): Department?
    fun findAll(): List<Department>
    fun count(): Long
}
