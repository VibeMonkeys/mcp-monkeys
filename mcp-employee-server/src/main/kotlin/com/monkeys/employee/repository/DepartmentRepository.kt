package com.monkeys.employee.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.employee.entity.Department
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DepartmentRepository : JpaRepository<Department, Long>, KotlinJdslJpqlExecutor {
    fun findByCode(code: String): Optional<Department>
    fun findByNameContainingIgnoreCase(name: String): List<Department>
}
