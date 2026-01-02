package com.monkeys.product.domain.model

import java.time.LocalDateTime

data class Category(
    val id: Long = 0,
    val name: String,
    val code: String,
    val description: String? = null,
    val parent: Category? = null,
    val productCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun isTopLevel(): Boolean = parent == null

    fun update(name: String, description: String?) = copy(
        name = name,
        description = description,
        updatedAt = LocalDateTime.now()
    )
}
