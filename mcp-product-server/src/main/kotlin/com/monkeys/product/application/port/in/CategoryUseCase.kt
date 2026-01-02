package com.monkeys.product.application.port.`in`

import com.monkeys.product.domain.model.Category

interface CategoryUseCase {
    fun findAllCategories(): List<Category>
    fun findCategoryById(id: Long): Category?
    fun findCategoryByCode(code: String): Category?
    fun findTopLevelCategories(): List<Category>
    fun findSubCategories(parentId: Long): List<Category>
}
