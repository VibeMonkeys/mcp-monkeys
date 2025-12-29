package com.monkeys.employee.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

enum class EmployeeStatus {
    ACTIVE,      // 재직 중
    ON_LEAVE,    // 휴직 중
    RESIGNED     // 퇴사
}

@Entity
@Table(name = "employees")
class Employee(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val employeeNumber: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    var phone: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    var department: Department? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    var position: Position? = null,

    @Column(nullable = false)
    var hireDate: LocalDate,

    var resignDate: LocalDate? = null,

    @Column(precision = 15, scale = 2)
    var salary: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EmployeeStatus = EmployeeStatus.ACTIVE,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, email: String, phone: String?) {
        this.name = name
        this.email = email
        this.phone = phone
        this.updatedAt = LocalDateTime.now()
    }

    fun changeDepartment(newDepartment: Department) {
        this.department?.employees?.remove(this)
        this.department = newDepartment
        newDepartment.employees.add(this)
        this.updatedAt = LocalDateTime.now()
    }

    fun changePosition(newPosition: Position) {
        this.position = newPosition
        this.updatedAt = LocalDateTime.now()
    }

    fun updateSalary(newSalary: BigDecimal) {
        this.salary = newSalary
        this.updatedAt = LocalDateTime.now()
    }

    fun takeLeave() {
        if (status == EmployeeStatus.ACTIVE) {
            status = EmployeeStatus.ON_LEAVE
            updatedAt = LocalDateTime.now()
        }
    }

    fun returnFromLeave() {
        if (status == EmployeeStatus.ON_LEAVE) {
            status = EmployeeStatus.ACTIVE
            updatedAt = LocalDateTime.now()
        }
    }

    fun resign(resignDate: LocalDate = LocalDate.now()) {
        this.status = EmployeeStatus.RESIGNED
        this.resignDate = resignDate
        this.updatedAt = LocalDateTime.now()
    }

    fun getYearsOfService(): Int {
        val endDate = resignDate ?: LocalDate.now()
        return endDate.year - hireDate.year
    }
}
