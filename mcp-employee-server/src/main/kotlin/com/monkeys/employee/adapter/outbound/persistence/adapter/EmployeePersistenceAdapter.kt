package com.monkeys.employee.adapter.outbound.persistence.adapter

import com.monkeys.employee.adapter.outbound.persistence.entity.DepartmentEntity
import com.monkeys.employee.adapter.outbound.persistence.entity.EmployeeEntity
import com.monkeys.employee.adapter.outbound.persistence.entity.PositionEntity
import com.monkeys.employee.adapter.outbound.persistence.repository.DepartmentJpaRepository
import com.monkeys.employee.adapter.outbound.persistence.repository.EmployeeJpaRepository
import com.monkeys.employee.adapter.outbound.persistence.repository.PositionJpaRepository
import com.monkeys.employee.application.port.outbound.EmployeeRepository
import com.monkeys.employee.domain.model.Employee
import com.monkeys.employee.domain.model.EmployeeStatus
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class EmployeePersistenceAdapter(
    private val employeeJpaRepository: EmployeeJpaRepository,
    private val departmentJpaRepository: DepartmentJpaRepository,
    private val positionJpaRepository: PositionJpaRepository
) : EmployeeRepository {

    override fun findById(id: Long): Employee? =
        employeeJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)

    override fun findByEmployeeNumber(employeeNumber: String): Employee? =
        employeeJpaRepository.findByEmployeeNumber(employeeNumber)?.toDomain()

    override fun findByNameContaining(name: String): List<Employee> =
        employeeJpaRepository.findByNameContaining(name).map { it.toDomain() }

    override fun findByDepartmentId(departmentId: Long): List<Employee> =
        employeeJpaRepository.findByDepartmentId(departmentId).map { it.toDomain() }

    override fun findByStatus(status: EmployeeStatus): List<Employee> =
        employeeJpaRepository.findByStatus(status).map { it.toDomain() }

    override fun findAll(): List<Employee> =
        employeeJpaRepository.findAll().map { it.toDomain() }

    override fun save(employee: Employee): Employee {
        val departmentEntity: DepartmentEntity? = employee.department?.let {
            departmentJpaRepository.findById(it.id).orElse(null)
        }
        val positionEntity: PositionEntity? = employee.position?.let {
            positionJpaRepository.findById(it.id).orElse(null)
        }

        val entity = EmployeeEntity.fromDomain(employee, departmentEntity, positionEntity)
        return employeeJpaRepository.save(entity).toDomain()
    }

    override fun count(): Long =
        employeeJpaRepository.count()

    override fun countByStatus(status: EmployeeStatus): Long =
        employeeJpaRepository.countByStatus(status)

    override fun calculateAverageSalary(): BigDecimal? =
        employeeJpaRepository.calculateAverageSalary()

    override fun calculateAverageYearsOfService(): Double? =
        employeeJpaRepository.calculateAverageYearsOfService()
}
