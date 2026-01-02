package com.monkeys.product.adapter.`in`.mcp.dto

import com.monkeys.product.domain.model.Inventory

data class InventoryInfo(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val reorderLevel: Int,
    val location: String?,
    val isLowStock: Boolean,
    val isOutOfStock: Boolean
) {
    companion object {
        fun from(inventory: Inventory) = InventoryInfo(
            productId = inventory.productId,
            productName = inventory.productName,
            quantity = inventory.quantity,
            reorderLevel = inventory.reorderLevel,
            location = inventory.location,
            isLowStock = inventory.isLowStock(),
            isOutOfStock = inventory.isOutOfStock()
        )
    }
}

data class InventoryResult(
    val success: Boolean,
    val message: String,
    val inventory: InventoryInfo?
)
