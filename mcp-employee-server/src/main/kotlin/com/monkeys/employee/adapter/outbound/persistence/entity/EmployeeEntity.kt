package com.monkeys.employee.adapter.outbound.persistence.entity

import com.monkeys.employee.domain.model.Employee
import com.monkeys.employee.domain.model.EmployeeStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "employees")
class EmployeeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "employee_number", nullable = false, unique = true)
    val employeeNumber: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column
    val phone: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    val department: DepartmentEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    val position: PositionEntity? = null,

    @Column(name = "hire_date", nullable = false)
    val hireDate: LocalDate,

    @Column(name = "resign_date")
    val resignDate: LocalDate? = null,

    @Column(nullable = false)
    val salary: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: EmployeeStatus = EmployeeStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Employee = Employee(
        id = id,
        employeeNumber = employeeNumber,
        name = name,
        email = email,
        phone = phone,
        department = department?.toDomain(),
        position = position?.toDomain(),
        hireDate = hireDate,
        resignDate = resignDate,
        salary = salary,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(
            employee: Employee,
            departmentEntity: DepartmentEntity? = null,
            positionEntity: PositionEntity? = null
        ): EmployeeEntity = EmployeeEntity(
            id = employee.id,
            employeeNumber = employee.employeeNumber,
            name = employee.name,
            email = employee.email,
            phone = employee.phone,
            department = departmentEntity,
            position = positionEntity,
            hireDate = employee.hireDate,
            resignDate = employee.resignDate,
            salary = employee.salary,
            status = employee.status,
            createdAt = employee.createdAt,
            updatedAt = employee.updatedAt
        )
    }
}
