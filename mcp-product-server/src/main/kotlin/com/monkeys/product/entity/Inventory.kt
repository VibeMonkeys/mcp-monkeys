package com.monkeys.product.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "inventories")
class Inventory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    var product: Product,

    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false)
    var reorderLevel: Int = 10,  // 재주문 알림 수준

    var location: String? = null,  // 창고 위치

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun addStock(amount: Int) {
        require(amount > 0) { "추가 수량은 양수여야 합니다." }
        quantity += amount
        updatedAt = LocalDateTime.now()
        product.updateInventoryStatus()
    }

    fun removeStock(amount: Int): Boolean {
        require(amount > 0) { "제거 수량은 양수여야 합니다." }
        if (quantity < amount) return false
        quantity -= amount
        updatedAt = LocalDateTime.now()
        product.updateInventoryStatus()
        return true
    }

    fun isLowStock(): Boolean = quantity <= reorderLevel

    fun needsReorder(): Boolean = quantity > 0 && quantity <= reorderLevel

    fun isOutOfStock(): Boolean = quantity == 0

    fun updateReorderLevel(newLevel: Int) {
        require(newLevel >= 0) { "재주문 수준은 0 이상이어야 합니다." }
        reorderLevel = newLevel
        updatedAt = LocalDateTime.now()
    }
}
