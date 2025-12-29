package com.monkeys.employee.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "departments")
class Department(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = false, unique = true)
    var code: String,

    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    var manager: Employee? = null,

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    val employees: MutableList<Employee> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, description: String?) {
        this.name = name
        this.description = description
        this.updatedAt = LocalDateTime.now()
    }

    fun assignManager(employee: Employee) {
        this.manager = employee
        this.updatedAt = LocalDateTime.now()
    }

    fun getEmployeeCount(): Int = employees.size
}
