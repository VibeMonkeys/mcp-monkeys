package com.monkeys.product.adapter.outbound.persistence.entity

import com.monkeys.product.domain.model.Inventory
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "inventories")
class InventoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    var product: ProductEntity,

    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false)
    var reorderLevel: Int = 10,

    var location: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain() = Inventory(
        id = id,
        productId = product.id,
        productName = product.name,
        quantity = quantity,
        reorderLevel = reorderLevel,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun updateFrom(inventory: Inventory) {
        this.quantity = inventory.quantity
        this.reorderLevel = inventory.reorderLevel
        this.location = inventory.location
        this.updatedAt = inventory.updatedAt
    }
}
