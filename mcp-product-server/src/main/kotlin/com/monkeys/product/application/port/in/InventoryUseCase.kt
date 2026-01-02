package com.monkeys.product.application.port.`in`

import com.monkeys.product.domain.model.Inventory

interface InventoryUseCase {
    fun addStock(productId: Long, amount: Int): Inventory?
    fun removeStock(productId: Long, amount: Int): Inventory?
    fun getInventory(productId: Long): Inventory?
    fun findLowStockInventories(): List<Inventory>
}
