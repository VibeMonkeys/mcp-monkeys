package com.monkeys.product.application.service

import com.monkeys.product.application.port.`in`.*
import com.monkeys.product.application.port.outbound.CategoryRepository
import com.monkeys.product.application.port.outbound.InventoryRepository
import com.monkeys.product.application.port.outbound.ProductRepository
import com.monkeys.product.domain.model.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val categoryRepository: CategoryRepository
) : ProductUseCase, InventoryUseCase, CategoryUseCase, ProductStatsUseCase {

    // ===== ProductUseCase =====

    override fun findAllProducts(): List<Product> =
        productRepository.findAll()

    override fun findProductById(id: Long): Product? =
        productRepository.findById(id)

    override fun findProductBySku(sku: String): Product? =
        productRepository.findBySku(sku)

    override fun searchProducts(name: String): List<Product> =
        productRepository.findByNameContaining(name)

    override fun findProductsByStatus(status: ProductStatus): List<Product> =
        productRepository.findByStatus(status)

    override fun findProductsByCategory(categoryId: Long): List<Product> =
        productRepository.findByCategoryId(categoryId)

    override fun findProductsByBrand(brand: String): List<Product> =
        productRepository.findByBrand(brand)

    override fun findProductsByPriceRange(minPrice: BigDecimal, maxPrice: BigDecimal): List<Product> =
        productRepository.findByPriceBetween(minPrice, maxPrice)

    override fun findActiveProducts(): List<Product> =
        productRepository.findActiveProducts()

    override fun findLowStockProducts(): List<Product> =
        productRepository.findLowStockProducts()

    override fun findOutOfStockProducts(): List<Product> =
        productRepository.findOutOfStockProducts()

    @Transactional
    override fun activateProduct(id: Long): Product? {
        val product = productRepository.findById(id) ?: return null
        val activated = product.activate()
        return productRepository.save(activated)
    }

    @Transactional
    override fun deactivateProduct(id: Long): Product? {
        val product = productRepository.findById(id) ?: return null
        val deactivated = product.deactivate()
        return productRepository.save(deactivated)
    }

    @Transactional
    override fun discontinueProduct(id: Long): Product? {
        val product = productRepository.findById(id) ?: return null
        val discontinued = product.discontinue()
        return productRepository.save(discontinued)
    }

    // ===== InventoryUseCase =====

    @Transactional
    override fun addStock(productId: Long, amount: Int): Inventory? {
        val inventory = inventoryRepository.findByProductId(productId) ?: return null
        val updated = inventory.addStock(amount)
        return inventoryRepository.save(updated)
    }

    @Transactional
    override fun removeStock(productId: Long, amount: Int): Inventory? {
        val inventory = inventoryRepository.findByProductId(productId) ?: return null
        val updated = inventory.removeStock(amount) ?: return null
        return inventoryRepository.save(updated)
    }

    override fun getInventory(productId: Long): Inventory? =
        inventoryRepository.findByProductId(productId)

    override fun findLowStockInventories(): List<Inventory> =
        inventoryRepository.findLowStockInventories()

    // ===== CategoryUseCase =====

    override fun findAllCategories(): List<Category> =
        categoryRepository.findAll()

    override fun findCategoryById(id: Long): Category? =
        categoryRepository.findById(id)

    override fun findCategoryByCode(code: String): Category? =
        categoryRepository.findByCode(code)

    override fun findTopLevelCategories(): List<Category> =
        categoryRepository.findByParentIsNull()

    override fun findSubCategories(parentId: Long): List<Category> =
        categoryRepository.findByParentId(parentId)

    // ===== ProductStatsUseCase =====

    override fun getProductStats(): ProductStats {
        val totalProducts = productRepository.count()
        val active = productRepository.countByStatus(ProductStatus.ACTIVE)
        val inactive = productRepository.countByStatus(ProductStatus.INACTIVE)
        val outOfStock = productRepository.countByStatus(ProductStatus.OUT_OF_STOCK)
        val discontinued = productRepository.countByStatus(ProductStatus.DISCONTINUED)
        val lowStock = productRepository.countLowStockProducts()
        val categories = categoryRepository.count()
        val totalValue = productRepository.calculateTotalInventoryValue() ?: BigDecimal.ZERO

        return ProductStats(
            totalProducts = totalProducts.toInt(),
            activeProducts = active.toInt(),
            inactiveProducts = inactive.toInt(),
            outOfStockProducts = outOfStock.toInt(),
            discontinuedProducts = discontinued.toInt(),
            lowStockProducts = lowStock.toInt(),
            totalCategories = categories,
            totalInventoryValue = totalValue
        )
    }
}
