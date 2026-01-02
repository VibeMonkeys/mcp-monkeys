package com.monkeys.product.application.port.`in`

import com.monkeys.product.domain.model.ProductStats

interface ProductStatsUseCase {
    fun getProductStats(): ProductStats
}
