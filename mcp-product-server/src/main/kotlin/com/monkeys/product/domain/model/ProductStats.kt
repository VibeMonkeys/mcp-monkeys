package com.monkeys.product.domain.model

import java.math.BigDecimal

data class ProductStats(
    val totalProducts: Int,
    val activeProducts: Int,
    val inactiveProducts: Int,
    val outOfStockProducts: Int,
    val discontinuedProducts: Int,
    val lowStockProducts: Int,
    val totalCategories: Long,
    val totalInventoryValue: BigDecimal
)
