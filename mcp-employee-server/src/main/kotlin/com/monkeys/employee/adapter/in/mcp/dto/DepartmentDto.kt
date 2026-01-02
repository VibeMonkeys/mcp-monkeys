package com.monkeys.employee.adapter.`in`.mcp.dto

import com.monkeys.employee.domain.model.Department

data class DepartmentDto(
    val id: Long,
    val name: String,
    val code: String,
    val description: String?,
    val managerId: Long?,
    val managerName: String?,
    val employeeCount: Int
) {
    companion object {
        fun fromDomain(department: Department): DepartmentDto = DepartmentDto(
            id = department.id,
            name = department.name,
            code = department.code,
            description = department.description,
            managerId = department.managerId,
            managerName = department.managerName,
            employeeCount = department.employeeCount
        )
    }
}
