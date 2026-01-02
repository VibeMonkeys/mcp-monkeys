package com.monkeys.product.adapter.outbound.persistence.repository

import com.monkeys.product.adapter.outbound.persistence.entity.ProductEntity
import com.monkeys.product.domain.model.ProductStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.util.*

interface ProductJpaRepository : JpaRepository<ProductEntity, Long> {
    fun findBySku(sku: String): Optional<ProductEntity>
    fun findByNameContainingIgnoreCase(name: String): List<ProductEntity>
    fun findByStatus(status: ProductStatus): List<ProductEntity>
    fun findByCategoryId(categoryId: Long): List<ProductEntity>
    fun findByBrandIgnoreCase(brand: String): List<ProductEntity>
    fun findByPriceBetween(minPrice: BigDecimal, maxPrice: BigDecimal): List<ProductEntity>

    @Query("SELECT p FROM ProductEntity p WHERE p.status = 'ACTIVE'")
    fun findActiveProducts(): List<ProductEntity>

    @Query("SELECT p FROM ProductEntity p JOIN p.inventory i WHERE i.quantity <= i.reorderLevel AND i.quantity > 0")
    fun findLowStockProducts(): List<ProductEntity>

    @Query("SELECT p FROM ProductEntity p WHERE p.status = 'OUT_OF_STOCK'")
    fun findOutOfStockProducts(): List<ProductEntity>

    fun countByStatus(status: ProductStatus): Long

    @Query("SELECT COUNT(p) FROM ProductEntity p JOIN p.inventory i WHERE i.quantity <= i.reorderLevel AND i.quantity > 0")
    fun countLowStockProducts(): Long

    @Query("SELECT SUM(p.price * i.quantity) FROM ProductEntity p JOIN p.inventory i WHERE p.status = 'ACTIVE'")
    fun calculateTotalInventoryValue(): BigDecimal?
}
