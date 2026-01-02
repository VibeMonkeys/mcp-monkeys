package com.monkeys.employee.adapter.outbound.persistence.repository

import com.monkeys.employee.adapter.outbound.persistence.entity.EmployeeEntity
import com.monkeys.employee.domain.model.EmployeeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface EmployeeJpaRepository : JpaRepository<EmployeeEntity, Long> {
    fun findByEmployeeNumber(employeeNumber: String): EmployeeEntity?
    fun findByNameContaining(name: String): List<EmployeeEntity>
    fun findByDepartmentId(departmentId: Long): List<EmployeeEntity>
    fun findByStatus(status: EmployeeStatus): List<EmployeeEntity>
    fun countByStatus(status: EmployeeStatus): Long

    @Query("SELECT AVG(e.salary) FROM EmployeeEntity e WHERE e.status = 'ACTIVE'")
    fun calculateAverageSalary(): BigDecimal?

    @Query("SELECT AVG(DATEDIFF(CURRENT_DATE, e.hireDate) / 365.0) FROM EmployeeEntity e WHERE e.status = 'ACTIVE'")
    fun calculateAverageYearsOfService(): Double?
}
