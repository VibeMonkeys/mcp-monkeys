package com.monkeys.employee.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.employee.entity.Employee
import com.monkeys.employee.entity.EmployeeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmployeeRepository : JpaRepository<Employee, Long>, KotlinJdslJpqlExecutor {
    fun findByEmployeeNumber(employeeNumber: String): Optional<Employee>
    fun findByEmail(email: String): Optional<Employee>
    fun findByStatus(status: EmployeeStatus): List<Employee>
    fun findByDepartmentId(departmentId: Long): List<Employee>
    fun findByPositionId(positionId: Long): List<Employee>
    fun findByNameContainingIgnoreCase(name: String): List<Employee>

    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.status = :status")
    fun findByDepartmentIdAndStatus(departmentId: Long, status: EmployeeStatus): List<Employee>

    // 통계용 count 쿼리
    fun countByStatus(status: EmployeeStatus): Long

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.status = 'ACTIVE'")
    fun findAverageSalaryOfActiveEmployees(): java.math.BigDecimal?
}
