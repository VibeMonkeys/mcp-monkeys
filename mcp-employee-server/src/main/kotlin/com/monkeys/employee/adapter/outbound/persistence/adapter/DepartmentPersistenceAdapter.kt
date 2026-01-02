package com.monkeys.employee.adapter.outbound.persistence.adapter

import com.monkeys.employee.adapter.outbound.persistence.repository.DepartmentJpaRepository
import com.monkeys.employee.application.port.outbound.DepartmentRepository
import com.monkeys.employee.domain.model.Department
import org.springframework.stereotype.Component

@Component
class DepartmentPersistenceAdapter(
    private val departmentJpaRepository: DepartmentJpaRepository
) : DepartmentRepository {

    override fun findById(id: Long): Department? =
        departmentJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)

    override fun findAll(): List<Department> =
        departmentJpaRepository.findAll().map { it.toDomain() }

    override fun count(): Long =
        departmentJpaRepository.count()
}
