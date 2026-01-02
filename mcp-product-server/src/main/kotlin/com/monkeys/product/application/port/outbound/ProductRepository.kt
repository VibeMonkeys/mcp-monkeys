package com.monkeys.product.application.port.outbound

import com.monkeys.product.domain.model.Product
import com.monkeys.product.domain.model.ProductStatus
import java.math.BigDecimal

interface ProductRepository {
    fun findById(id: Long): Product?
    fun findBySku(sku: String): Product?
    fun findByNameContaining(name: String): List<Product>
    fun findByStatus(status: ProductStatus): List<Product>
    fun findByCategoryId(categoryId: Long): List<Product>
    fun findByBrand(brand: String): List<Product>
    fun findByPriceBetween(minPrice: BigDecimal, maxPrice: BigDecimal): List<Product>
    fun findActiveProducts(): List<Product>
    fun findLowStockProducts(): List<Product>
    fun findOutOfStockProducts(): List<Product>
    fun findAll(): List<Product>
    fun save(product: Product): Product
    fun count(): Long
    fun countByStatus(status: ProductStatus): Long
    fun countLowStockProducts(): Long
    fun calculateTotalInventoryValue(): BigDecimal?
}
