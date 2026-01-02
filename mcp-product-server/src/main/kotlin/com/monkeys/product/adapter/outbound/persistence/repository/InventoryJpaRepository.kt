package com.monkeys.product.adapter.outbound.persistence.repository

import com.monkeys.product.adapter.outbound.persistence.entity.InventoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface InventoryJpaRepository : JpaRepository<InventoryEntity, Long> {
    fun findByProductId(productId: Long): Optional<InventoryEntity>

    @Query("SELECT i FROM InventoryEntity i WHERE i.quantity <= i.reorderLevel AND i.quantity > 0")
    fun findLowStockInventories(): List<InventoryEntity>
}
