package com.monkeys.product.application.port.`in`

import com.monkeys.product.domain.model.Product
import com.monkeys.product.domain.model.ProductStatus
import java.math.BigDecimal

interface ProductUseCase {
    fun findAllProducts(): List<Product>
    fun findProductById(id: Long): Product?
    fun findProductBySku(sku: String): Product?
    fun searchProducts(name: String): List<Product>
    fun findProductsByStatus(status: ProductStatus): List<Product>
    fun findProductsByCategory(categoryId: Long): List<Product>
    fun findProductsByBrand(brand: String): List<Product>
    fun findProductsByPriceRange(minPrice: BigDecimal, maxPrice: BigDecimal): List<Product>
    fun findActiveProducts(): List<Product>
    fun findLowStockProducts(): List<Product>
    fun findOutOfStockProducts(): List<Product>
    fun activateProduct(id: Long): Product?
    fun deactivateProduct(id: Long): Product?
    fun discontinueProduct(id: Long): Product?
}
