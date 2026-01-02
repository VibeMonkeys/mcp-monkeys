package com.monkeys.product.application.port.outbound

import com.monkeys.product.domain.model.Inventory

interface InventoryRepository {
    fun findByProductId(productId: Long): Inventory?
    fun findLowStockInventories(): List<Inventory>
    fun save(inventory: Inventory): Inventory
}
