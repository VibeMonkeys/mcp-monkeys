package com.monkeys.product.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.product.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<Category, Long>, KotlinJdslJpqlExecutor {
    fun findByCode(code: String): Optional<Category>
    fun findByNameContainingIgnoreCase(name: String): List<Category>
    fun findByParentIsNull(): List<Category>
    fun findByParentId(parentId: Long): List<Category>
}
