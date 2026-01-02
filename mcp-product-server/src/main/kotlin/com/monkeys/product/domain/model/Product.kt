package com.monkeys.product.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

data class Product(
    val id: Long = 0,
    val sku: String,
    val name: String,
    val description: String? = null,
    val category: Category? = null,
    val price: BigDecimal,
    val costPrice: BigDecimal = BigDecimal.ZERO,
    val brand: String? = null,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val inventory: Inventory? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, description: String?, price: BigDecimal, brand: String?) = copy(
        name = name,
        description = description,
        price = price,
        brand = brand,
        updatedAt = LocalDateTime.now()
    )

    fun activate() = copy(
        status = if (inventory?.quantity ?: 0 > 0) ProductStatus.ACTIVE else ProductStatus.OUT_OF_STOCK,
        updatedAt = LocalDateTime.now()
    )

    fun deactivate() = copy(
        status = ProductStatus.INACTIVE,
        updatedAt = LocalDateTime.now()
    )

    fun discontinue() = copy(
        status = ProductStatus.DISCONTINUED,
        updatedAt = LocalDateTime.now()
    )

    fun updateInventoryStatus(): Product {
        if (status == ProductStatus.DISCONTINUED || status == ProductStatus.INACTIVE) return this
        return copy(
            status = if (inventory?.quantity ?: 0 > 0) ProductStatus.ACTIVE else ProductStatus.OUT_OF_STOCK,
            updatedAt = LocalDateTime.now()
        )
    }

    fun getMargin(): BigDecimal {
        return if (costPrice > BigDecimal.ZERO) {
            ((price - costPrice) / price * BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }
}

enum class ProductStatus {
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK,
    DISCONTINUED
}
