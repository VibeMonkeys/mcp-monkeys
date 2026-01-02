package com.monkeys.employee.adapter.outbound.persistence.entity

import com.monkeys.employee.domain.model.Department
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "departments")
class DepartmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false, unique = true)
    val code: String,

    @Column
    val description: String? = null,

    @Column(name = "manager_id")
    val managerId: Long? = null,

    @Column(name = "manager_name")
    val managerName: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Transient
    var employeeCount: Int = 0

    fun toDomain(): Department = Department(
        id = id,
        name = name,
        code = code,
        description = description,
        managerId = managerId,
        managerName = managerName,
        employeeCount = employeeCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(department: Department): DepartmentEntity = DepartmentEntity(
            id = department.id,
            name = department.name,
            code = department.code,
            description = department.description,
            managerId = department.managerId,
            managerName = department.managerName,
            createdAt = department.createdAt,
            updatedAt = department.updatedAt
        )
    }
}
