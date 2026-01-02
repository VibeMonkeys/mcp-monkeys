package com.monkeys.product.application.port.outbound

import com.monkeys.product.domain.model.Category

interface CategoryRepository {
    fun findById(id: Long): Category?
    fun findByCode(code: String): Category?
    fun findByParentIsNull(): List<Category>
    fun findByParentId(parentId: Long): List<Category>
    fun findAll(): List<Category>
    fun count(): Long
}
