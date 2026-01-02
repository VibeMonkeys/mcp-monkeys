package com.monkeys.product.adapter.`in`.mcp.dto

import com.monkeys.product.domain.model.Category

data class CategoryInfo(
    val id: Long,
    val name: String,
    val code: String,
    val description: String?,
    val parentName: String?,
    val productCount: Int,
    val isTopLevel: Boolean
) {
    companion object {
        fun from(category: Category) = CategoryInfo(
            id = category.id,
            name = category.name,
            code = category.code,
            description = category.description,
            parentName = category.parent?.name,
            productCount = category.productCount,
            isTopLevel = category.isTopLevel()
        )
    }
}
