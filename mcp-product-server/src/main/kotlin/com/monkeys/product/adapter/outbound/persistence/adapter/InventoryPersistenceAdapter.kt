package com.monkeys.product.adapter.outbound.persistence.adapter

import com.monkeys.product.adapter.outbound.persistence.repository.InventoryJpaRepository
import com.monkeys.product.application.port.outbound.InventoryRepository
import com.monkeys.product.domain.model.Inventory
import org.springframework.stereotype.Repository

@Repository
class InventoryPersistenceAdapter(
    private val inventoryJpaRepository: InventoryJpaRepository
) : InventoryRepository {

    override fun findByProductId(productId: Long): Inventory? =
        inventoryJpaRepository.findByProductId(productId).orElse(null)?.toDomain()

    override fun findLowStockInventories(): List<Inventory> =
        inventoryJpaRepository.findLowStockInventories().map { it.toDomain() }

    override fun save(inventory: Inventory): Inventory {
        val entity = inventoryJpaRepository.findByProductId(inventory.productId).orElseThrow {
            IllegalArgumentException("Inventory not found for product: ${inventory.productId}")
        }
        entity.updateFrom(inventory)
        return inventoryJpaRepository.save(entity).toDomain()
    }
}
