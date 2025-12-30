package com.monkeys.product.service

import com.monkeys.product.entity.*
import com.monkeys.shared.util.ValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Product MCP Tool Provider
 * 상품 관리 시스템의 MCP 도구들을 제공합니다.
 */
@Service
@Transactional(readOnly = true)
class ProductMcpService(
    private val productService: ProductService
) {
    private val logger = LoggerFactory.getLogger(ProductMcpService::class.java)

    // ===== 상품 조회 =====

    @Tool(
        name = "searchProducts",
        description = "상품명으로 상품을 검색합니다."
    )
    fun searchProducts(
        @ToolParam(description = "검색할 상품명", required = true)
        name: String
    ): List<ProductInfo> {
        val validatedName = ValidationUtils.requireNotBlank(name, "상품명")
        logger.info("MCP Tool 호출: searchProducts - name=$validatedName")
        return productService.searchProducts(validatedName).map { it.toInfo() }
    }

    @Tool(
        name = "getProductBySku",
        description = "SKU로 상품을 조회합니다."
    )
    fun getProductBySku(
        @ToolParam(description = "상품 SKU", required = true)
        sku: String
    ): ProductInfo? {
        val validatedSku = ValidationUtils.requireNotBlank(sku, "SKU")
        logger.info("MCP Tool 호출: getProductBySku - sku=$validatedSku")
        return productService.findProductBySku(validatedSku)?.toInfo()
    }

    @Tool(
        name = "getProductsByCategory",
        description = "카테고리별 상품 목록을 조회합니다."
    )
    fun getProductsByCategory(
        @ToolParam(description = "카테고리 ID", required = true)
        categoryId: Long
    ): List<ProductInfo> {
        logger.info("MCP Tool 호출: getProductsByCategory - categoryId=$categoryId")
        return productService.findProductsByCategory(categoryId).map { it.toInfo() }
    }

    @Tool(
        name = "getProductsByBrand",
        description = "브랜드별 상품 목록을 조회합니다."
    )
    fun getProductsByBrand(
        @ToolParam(description = "브랜드명", required = true)
        brand: String
    ): List<ProductInfo> {
        val validatedBrand = ValidationUtils.requireNotBlank(brand, "브랜드명")
        logger.info("MCP Tool 호출: getProductsByBrand - brand=$validatedBrand")
        return productService.findProductsByBrand(validatedBrand).map { it.toInfo() }
    }

    @Tool(
        name = "getProductsByPriceRange",
        description = "가격 범위로 상품을 검색합니다."
    )
    fun getProductsByPriceRange(
        @ToolParam(description = "최소 가격", required = true)
        minPrice: Double,
        @ToolParam(description = "최대 가격", required = true)
        maxPrice: Double
    ): List<ProductInfo> {
        ValidationUtils.requireNonNegative(minPrice.toInt(), "최소 가격")
        ValidationUtils.requirePositive(maxPrice, "최대 가격")
        ValidationUtils.validateRange(minPrice, maxPrice, "최소 가격", "최대 가격")
        logger.info("MCP Tool 호출: getProductsByPriceRange - minPrice=$minPrice, maxPrice=$maxPrice")
        return productService.findProductsByPriceRange(
            BigDecimal.valueOf(minPrice),
            BigDecimal.valueOf(maxPrice)
        ).map { it.toInfo() }
    }

    @Tool(
        name = "getActiveProducts",
        description = "판매 중인 상품 목록을 조회합니다."
    )
    fun getActiveProducts(): List<ProductInfo> {
        logger.info("MCP Tool 호출: getActiveProducts")
        return productService.findActiveProducts().map { it.toInfo() }
    }

    @Tool(
        name = "getLowStockProducts",
        description = "재고 부족 상품 목록을 조회합니다."
    )
    fun getLowStockProducts(): List<ProductInfo> {
        logger.info("MCP Tool 호출: getLowStockProducts")
        return productService.findLowStockProducts().map { it.toInfo() }
    }

    @Tool(
        name = "getOutOfStockProducts",
        description = "품절 상품 목록을 조회합니다."
    )
    fun getOutOfStockProducts(): List<ProductInfo> {
        logger.info("MCP Tool 호출: getOutOfStockProducts")
        return productService.findOutOfStockProducts().map { it.toInfo() }
    }

    // ===== 재고 관리 =====

    @Tool(
        name = "addStock",
        description = "상품의 재고를 추가합니다."
    )
    @Transactional
    fun addStock(
        @ToolParam(description = "상품 ID", required = true)
        productId: Long,
        @ToolParam(description = "추가할 수량", required = true)
        amount: Int
    ): InventoryResult {
        ValidationUtils.requirePositive(productId, "상품 ID")
        ValidationUtils.requirePositive(amount, "추가할 수량")
        logger.info("MCP Tool 호출: addStock - productId=$productId, amount=$amount")

        val inventory = productService.addStock(productId, amount)
        return if (inventory != null) {
            InventoryResult(
                success = true,
                message = "${amount}개의 재고가 추가되었습니다. 현재 재고: ${inventory.quantity}",
                inventory = inventory.toInfo()
            )
        } else {
            InventoryResult(
                success = false,
                message = "상품을 찾을 수 없습니다.",
                inventory = null
            )
        }
    }

    @Tool(
        name = "removeStock",
        description = "상품의 재고를 차감합니다."
    )
    @Transactional
    fun removeStock(
        @ToolParam(description = "상품 ID", required = true)
        productId: Long,
        @ToolParam(description = "차감할 수량", required = true)
        amount: Int
    ): InventoryResult {
        ValidationUtils.requirePositive(productId, "상품 ID")
        ValidationUtils.requirePositive(amount, "차감할 수량")
        logger.info("MCP Tool 호출: removeStock - productId=$productId, amount=$amount")

        val inventory = productService.removeStock(productId, amount)
        return if (inventory != null) {
            InventoryResult(
                success = true,
                message = "${amount}개의 재고가 차감되었습니다. 현재 재고: ${inventory.quantity}",
                inventory = inventory.toInfo()
            )
        } else {
            InventoryResult(
                success = false,
                message = "상품을 찾을 수 없거나 재고가 부족합니다.",
                inventory = null
            )
        }
    }

    @Tool(
        name = "getInventory",
        description = "상품의 재고 정보를 조회합니다."
    )
    fun getInventory(
        @ToolParam(description = "상품 ID", required = true)
        productId: Long
    ): InventoryInfo? {
        logger.info("MCP Tool 호출: getInventory - productId=$productId")
        return productService.getInventory(productId)?.toInfo()
    }

    // ===== 상품 상태 관리 =====

    @Tool(
        name = "activateProduct",
        description = "상품을 판매 중 상태로 변경합니다."
    )
    @Transactional
    fun activateProduct(
        @ToolParam(description = "상품 ID", required = true)
        productId: Long
    ): ProductResult {
        logger.info("MCP Tool 호출: activateProduct - productId=$productId")

        val product = productService.activateProduct(productId)
        return if (product != null) {
            ProductResult(
                success = true,
                message = "상품이 활성화되었습니다.",
                product = product.toInfo()
            )
        } else {
            ProductResult(
                success = false,
                message = "상품을 찾을 수 없습니다.",
                product = null
            )
        }
    }

    @Tool(
        name = "deactivateProduct",
        description = "상품을 판매 중지 상태로 변경합니다."
    )
    @Transactional
    fun deactivateProduct(
        @ToolParam(description = "상품 ID", required = true)
        productId: Long
    ): ProductResult {
        logger.info("MCP Tool 호출: deactivateProduct - productId=$productId")

        val product = productService.deactivateProduct(productId)
        return if (product != null) {
            ProductResult(
                success = true,
                message = "상품이 비활성화되었습니다.",
                product = product.toInfo()
            )
        } else {
            ProductResult(
                success = false,
                message = "상품을 찾을 수 없습니다.",
                product = null
            )
        }
    }

    @Tool(
        name = "discontinueProduct",
        description = "상품을 단종 처리합니다."
    )
    @Transactional
    fun discontinueProduct(
        @ToolParam(description = "상품 ID", required = true)
        productId: Long
    ): ProductResult {
        logger.info("MCP Tool 호출: discontinueProduct - productId=$productId")

        val product = productService.discontinueProduct(productId)
        return if (product != null) {
            ProductResult(
                success = true,
                message = "상품이 단종 처리되었습니다.",
                product = product.toInfo()
            )
        } else {
            ProductResult(
                success = false,
                message = "상품을 찾을 수 없습니다.",
                product = null
            )
        }
    }

    // ===== 카테고리 조회 =====

    @Tool(
        name = "getAllCategories",
        description = "모든 카테고리 목록을 조회합니다."
    )
    fun getAllCategories(): List<CategoryInfo> {
        logger.info("MCP Tool 호출: getAllCategories")
        return productService.findAllCategories().map { it.toInfo() }
    }

    @Tool(
        name = "getTopLevelCategories",
        description = "최상위 카테고리 목록을 조회합니다."
    )
    fun getTopLevelCategories(): List<CategoryInfo> {
        logger.info("MCP Tool 호출: getTopLevelCategories")
        return productService.findTopLevelCategories().map { it.toInfo() }
    }

    @Tool(
        name = "getSubCategories",
        description = "하위 카테고리 목록을 조회합니다."
    )
    fun getSubCategories(
        @ToolParam(description = "상위 카테고리 ID", required = true)
        parentId: Long
    ): List<CategoryInfo> {
        logger.info("MCP Tool 호출: getSubCategories - parentId=$parentId")
        return productService.findSubCategories(parentId).map { it.toInfo() }
    }

    // ===== 통계 =====

    @Tool(
        name = "getProductStats",
        description = "상품 통계를 조회합니다."
    )
    fun getProductStats(): ProductStats {
        logger.info("MCP Tool 호출: getProductStats")
        return productService.getProductStats()
    }
}

// ===== DTO =====

data class ProductInfo(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val categoryName: String?,
    val price: String,
    val costPrice: String,
    val brand: String?,
    val status: String,
    val stockQuantity: Int,
    val isLowStock: Boolean,
    val margin: String
)

data class InventoryInfo(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val reorderLevel: Int,
    val location: String?,
    val isLowStock: Boolean,
    val isOutOfStock: Boolean
)

data class CategoryInfo(
    val id: Long,
    val name: String,
    val code: String,
    val description: String?,
    val parentName: String?,
    val productCount: Int,
    val isTopLevel: Boolean
)

data class ProductResult(
    val success: Boolean,
    val message: String,
    val product: ProductInfo?
)

data class InventoryResult(
    val success: Boolean,
    val message: String,
    val inventory: InventoryInfo?
)

// ===== Extension Functions =====

private fun Product.toInfo() = ProductInfo(
    id = id,
    sku = sku,
    name = name,
    description = description,
    categoryName = category?.name,
    price = price.toString(),
    costPrice = costPrice.toString(),
    brand = brand,
    status = status.name,
    stockQuantity = inventory?.quantity ?: 0,
    isLowStock = inventory?.isLowStock() ?: false,
    margin = getMargin().toString() + "%"
)

private fun Inventory.toInfo() = InventoryInfo(
    productId = product.id,
    productName = product.name,
    quantity = quantity,
    reorderLevel = reorderLevel,
    location = location,
    isLowStock = isLowStock(),
    isOutOfStock = isOutOfStock()
)

private fun Category.toInfo() = CategoryInfo(
    id = id,
    name = name,
    code = code,
    description = description,
    parentName = parent?.name,
    productCount = getProductCount(),
    isTopLevel = isTopLevel()
)
