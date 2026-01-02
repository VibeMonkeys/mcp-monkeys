package com.monkeys.product.adapter.outbound.persistence.repository

import com.monkeys.product.adapter.outbound.persistence.entity.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryJpaRepository : JpaRepository<CategoryEntity, Long> {
    fun findByCode(code: String): Optional<CategoryEntity>
    fun findByParentIsNull(): List<CategoryEntity>
    fun findByParentId(parentId: Long): List<CategoryEntity>
}
