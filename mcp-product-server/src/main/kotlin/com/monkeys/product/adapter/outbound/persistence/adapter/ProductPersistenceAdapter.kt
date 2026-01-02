package com.monkeys.product.adapter.outbound.persistence.adapter

import com.monkeys.product.adapter.outbound.persistence.repository.ProductJpaRepository
import com.monkeys.product.application.port.outbound.ProductRepository
import com.monkeys.product.domain.model.Product
import com.monkeys.product.domain.model.ProductStatus
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class ProductPersistenceAdapter(
    private val productJpaRepository: ProductJpaRepository
) : ProductRepository {

    override fun findById(id: Long): Product? =
        productJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findBySku(sku: String): Product? =
        productJpaRepository.findBySku(sku).orElse(null)?.toDomain()

    override fun findByNameContaining(name: String): List<Product> =
        productJpaRepository.findByNameContainingIgnoreCase(name).map { it.toDomain() }

    override fun findByStatus(status: ProductStatus): List<Product> =
        productJpaRepository.findByStatus(status).map { it.toDomain() }

    override fun findByCategoryId(categoryId: Long): List<Product> =
        productJpaRepository.findByCategoryId(categoryId).map { it.toDomain() }

    override fun findByBrand(brand: String): List<Product> =
        productJpaRepository.findByBrandIgnoreCase(brand).map { it.toDomain() }

    override fun findByPriceBetween(minPrice: BigDecimal, maxPrice: BigDecimal): List<Product> =
        productJpaRepository.findByPriceBetween(minPrice, maxPrice).map { it.toDomain() }

    override fun findActiveProducts(): List<Product> =
        productJpaRepository.findActiveProducts().map { it.toDomain() }

    override fun findLowStockProducts(): List<Product> =
        productJpaRepository.findLowStockProducts().map { it.toDomain() }

    override fun findOutOfStockProducts(): List<Product> =
        productJpaRepository.findOutOfStockProducts().map { it.toDomain() }

    override fun findAll(): List<Product> =
        productJpaRepository.findAll().map { it.toDomain() }

    override fun save(product: Product): Product {
        val entity = productJpaRepository.findById(product.id).orElseThrow {
            IllegalArgumentException("Product not found: ${product.id}")
        }
        entity.updateFrom(product)
        return productJpaRepository.save(entity).toDomain()
    }

    override fun count(): Long =
        productJpaRepository.count()

    override fun countByStatus(status: ProductStatus): Long =
        productJpaRepository.countByStatus(status)

    override fun countLowStockProducts(): Long =
        productJpaRepository.countLowStockProducts()

    override fun calculateTotalInventoryValue(): BigDecimal? =
        productJpaRepository.calculateTotalInventoryValue()
}
