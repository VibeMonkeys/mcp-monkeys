package com.monkeys.product.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.product.entity.Product
import com.monkeys.product.entity.ProductStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
interface ProductRepository : JpaRepository<Product, Long>, KotlinJdslJpqlExecutor {
    fun findBySku(sku: String): Optional<Product>
    fun findByNameContainingIgnoreCase(name: String): List<Product>
    fun findByStatus(status: ProductStatus): List<Product>
    fun findByCategoryId(categoryId: Long): List<Product>
    fun findByBrandIgnoreCase(brand: String): List<Product>
    fun findByPriceBetween(minPrice: BigDecimal, maxPrice: BigDecimal): List<Product>

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    fun findActiveProducts(): List<Product>

    @Query("SELECT p FROM Product p JOIN p.inventory i WHERE i.quantity <= i.reorderLevel AND p.status != 'DISCONTINUED'")
    fun findLowStockProducts(): List<Product>

    @Query("SELECT p FROM Product p JOIN p.inventory i WHERE i.quantity = 0 AND p.status != 'DISCONTINUED'")
    fun findOutOfStockProducts(): List<Product>
}
