package com.monkeys.employee.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "positions")
class Position(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = false)
    var level: Int,  // 1: 사원, 2: 대리, 3: 과장, 4: 차장, 5: 부장, 6: 이사, 7: 상무, 8: 전무, 9: 사장

    var description: String? = null,

    @Column(precision = 15, scale = 2)
    var minSalary: BigDecimal = BigDecimal.ZERO,

    @Column(precision = 15, scale = 2)
    var maxSalary: BigDecimal = BigDecimal.ZERO,

    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    val employees: MutableList<Employee> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, level: Int, description: String?, minSalary: BigDecimal, maxSalary: BigDecimal) {
        this.name = name
        this.level = level
        this.description = description
        this.minSalary = minSalary
        this.maxSalary = maxSalary
        this.updatedAt = LocalDateTime.now()
    }

    fun getEmployeeCount(): Int = employees.size
}
