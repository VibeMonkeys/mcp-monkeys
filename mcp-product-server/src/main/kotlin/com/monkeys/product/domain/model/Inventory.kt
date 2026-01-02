package com.monkeys.product.domain.model

import java.time.LocalDateTime

data class Inventory(
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Int = 0,
    val reorderLevel: Int = 10,
    val location: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun addStock(amount: Int): Inventory {
        require(amount > 0) { "추가 수량은 양수여야 합니다." }
        return copy(
            quantity = quantity + amount,
            updatedAt = LocalDateTime.now()
        )
    }

    fun removeStock(amount: Int): Inventory? {
        require(amount > 0) { "제거 수량은 양수여야 합니다." }
        if (quantity < amount) return null
        return copy(
            quantity = quantity - amount,
            updatedAt = LocalDateTime.now()
        )
    }

    fun isLowStock(): Boolean = quantity <= reorderLevel
    fun needsReorder(): Boolean = quantity > 0 && quantity <= reorderLevel
    fun isOutOfStock(): Boolean = quantity == 0

    fun updateReorderLevel(newLevel: Int): Inventory {
        require(newLevel >= 0) { "재주문 수준은 0 이상이어야 합니다." }
        return copy(
            reorderLevel = newLevel,
            updatedAt = LocalDateTime.now()
        )
    }
}
