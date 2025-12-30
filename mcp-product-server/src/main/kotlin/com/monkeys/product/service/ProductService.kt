package com.monkeys.product.service

import com.monkeys.product.entity.*
import com.monkeys.product.repository.CategoryRepository
import com.monkeys.product.repository.InventoryRepository
import com.monkeys.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val inventoryRepository: InventoryRepository
) {

    // ===== Product Operations =====

    fun findAllProducts(): List<Product> =
        productRepository.findAll()

    fun findProductById(id: Long): Product? =
        productRepository.findById(id).orElse(null)

    fun findProductBySku(sku: String): Product? =
        productRepository.findBySku(sku).orElse(null)

    fun searchProducts(name: String): List<Product> =
        productRepository.findByNameContainingIgnoreCase(name)

    fun findProductsByStatus(status: ProductStatus): List<Product> =
        productRepository.findByStatus(status)

    fun findProductsByCategory(categoryId: Long): List<Product> =
        productRepository.findByCategoryId(categoryId)

    fun findProductsByBrand(brand: String): List<Product> =
        productRepository.findByBrandIgnoreCase(brand)

    fun findProductsByPriceRange(minPrice: BigDecimal, maxPrice: BigDecimal): List<Product> =
        productRepository.findByPriceBetween(minPrice, maxPrice)

    fun findActiveProducts(): List<Product> =
        productRepository.findActiveProducts()

    fun findLowStockProducts(): List<Product> =
        productRepository.findLowStockProducts()

    fun findOutOfStockProducts(): List<Product> =
        productRepository.findOutOfStockProducts()

    @Transactional
    fun createProduct(
        sku: String,
        name: String,
        description: String?,
        categoryId: Long?,
        price: BigDecimal,
        costPrice: BigDecimal,
        brand: String?,
        initialStock: Int
    ): Product {
        val category = categoryId?.let { categoryRepository.findById(it).orElse(null) }

        val product = Product(
            sku = sku,
            name = name,
            description = description,
            category = category,
            price = price,
            costPrice = costPrice,
            brand = brand
        )

        val savedProduct = productRepository.save(product)

        val inventory = Inventory(
            product = savedProduct,
            quantity = initialStock
        )
        savedProduct.inventory = inventoryRepository.save(inventory)
        savedProduct.updateInventoryStatus()

        return productRepository.save(savedProduct)
    }

    @Transactional
    fun updateProduct(id: Long, name: String, description: String?, price: BigDecimal, brand: String?): Product? {
        val product = productRepository.findById(id).orElse(null) ?: return null
        product.update(name, description, price, brand)
        return productRepository.save(product)
    }

    @Transactional
    fun changeCategory(productId: Long, categoryId: Long): Product? {
        val product = productRepository.findById(productId).orElse(null) ?: return null
        val category = categoryRepository.findById(categoryId).orElse(null) ?: return null
        product.category = category
        product.updatedAt = java.time.LocalDateTime.now()
        return productRepository.save(product)
    }

    @Transactional
    fun activateProduct(id: Long): Product? {
        val product = productRepository.findById(id).orElse(null) ?: return null
        product.activate()
        return productRepository.save(product)
    }

    @Transactional
    fun deactivateProduct(id: Long): Product? {
        val product = productRepository.findById(id).orElse(null) ?: return null
        product.deactivate()
        return productRepository.save(product)
    }

    @Transactional
    fun discontinueProduct(id: Long): Product? {
        val product = productRepository.findById(id).orElse(null) ?: return null
        product.discontinue()
        return productRepository.save(product)
    }

    // ===== Inventory Operations =====

    @Transactional
    fun addStock(productId: Long, amount: Int): Inventory? {
        val inventory = inventoryRepository.findByProductId(productId).orElse(null) ?: return null
        inventory.addStock(amount)
        return inventoryRepository.save(inventory)
    }

    @Transactional
    fun removeStock(productId: Long, amount: Int): Inventory? {
        val inventory = inventoryRepository.findByProductId(productId).orElse(null) ?: return null
        if (!inventory.removeStock(amount)) return null
        return inventoryRepository.save(inventory)
    }

    fun getInventory(productId: Long): Inventory? =
        inventoryRepository.findByProductId(productId).orElse(null)

    fun findLowStockInventories(): List<Inventory> =
        inventoryRepository.findLowStockInventories()

    // ===== Category Operations =====

    fun findAllCategories(): List<Category> =
        categoryRepository.findAll()

    fun findCategoryById(id: Long): Category? =
        categoryRepository.findById(id).orElse(null)

    fun findCategoryByCode(code: String): Category? =
        categoryRepository.findByCode(code).orElse(null)

    fun findTopLevelCategories(): List<Category> =
        categoryRepository.findByParentIsNull()

    fun findSubCategories(parentId: Long): List<Category> =
        categoryRepository.findByParentId(parentId)

    @Transactional
    fun createCategory(name: String, code: String, description: String?, parentId: Long?): Category {
        val parent = parentId?.let { categoryRepository.findById(it).orElse(null) }
        val category = Category(
            name = name,
            code = code,
            description = description,
            parent = parent
        )
        return categoryRepository.save(category)
    }

    // ===== Statistics =====

    fun getProductStats(): ProductStats {
        val products = productRepository.findAll()
        val active = products.count { it.status == ProductStatus.ACTIVE }
        val inactive = products.count { it.status == ProductStatus.INACTIVE }
        val outOfStock = products.count { it.status == ProductStatus.OUT_OF_STOCK }
        val discontinued = products.count { it.status == ProductStatus.DISCONTINUED }
        val lowStock = findLowStockProducts().size
        val categories = categoryRepository.count()

        val totalValue = products.filter { it.status != ProductStatus.DISCONTINUED }
            .fold(BigDecimal.ZERO) { acc, product ->
                val quantity = BigDecimal(product.inventory?.quantity ?: 0)
                acc + (quantity * product.price)
            }

        return ProductStats(
            totalProducts = products.size,
            activeProducts = active,
            inactiveProducts = inactive,
            outOfStockProducts = outOfStock,
            discontinuedProducts = discontinued,
            lowStockProducts = lowStock,
            totalCategories = categories,
            totalInventoryValue = totalValue
        )
    }
}

data class ProductStats(
    val totalProducts: Int,
    val activeProducts: Int,
    val inactiveProducts: Int,
    val outOfStockProducts: Int,
    val discontinuedProducts: Int,
    val lowStockProducts: Int,
    val totalCategories: Long,
    val totalInventoryValue: BigDecimal
)
