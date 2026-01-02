package com.monkeys.product.adapter.`in`.mcp

import com.monkeys.product.adapter.`in`.mcp.dto.*
import com.monkeys.product.application.port.`in`.*
import com.monkeys.product.domain.model.ProductStats
import com.monkeys.shared.util.ValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ProductMcpAdapter(
    private val productUseCase: ProductUseCase,
    private val inventoryUseCase: InventoryUseCase,
    private val categoryUseCase: CategoryUseCase,
    private val productStatsUseCase: ProductStatsUseCase
) {
    private val logger = LoggerFactory.getLogger(ProductMcpAdapter::class.java)

    // ===== Product Search =====

    @Tool(name = "searchProducts", description = "상품명으로 상품을 검색합니다.")
    fun searchProducts(
        @ToolParam(description = "검색할 상품명", required = true) name: String
    ): List<ProductInfo> {
        val validatedName = ValidationUtils.requireNotBlank(name, "상품명")
        logger.info("MCP Tool 호출: searchProducts - name=$validatedName")
        return productUseCase.searchProducts(validatedName).map { ProductInfo.from(it) }
    }

    @Tool(name = "getProductBySku", description = "SKU로 상품을 조회합니다.")
    fun getProductBySku(
        @ToolParam(description = "상품 SKU", required = true) sku: String
    ): ProductInfo? {
        val validatedSku = ValidationUtils.requireNotBlank(sku, "SKU")
        logger.info("MCP Tool 호출: getProductBySku - sku=$validatedSku")
        return productUseCase.findProductBySku(validatedSku)?.let { ProductInfo.from(it) }
    }

    @Tool(name = "getProductsByCategory", description = "카테고리별 상품 목록을 조회합니다.")
    fun getProductsByCategory(
        @ToolParam(description = "카테고리 ID", required = true) categoryId: Long
    ): List<ProductInfo> {
        logger.info("MCP Tool 호출: getProductsByCategory - categoryId=$categoryId")
        return productUseCase.findProductsByCategory(categoryId).map { ProductInfo.from(it) }
    }

    @Tool(name = "getProductsByBrand", description = "브랜드별 상품 목록을 조회합니다.")
    fun getProductsByBrand(
        @ToolParam(description = "브랜드명", required = true) brand: String
    ): List<ProductInfo> {
        val validatedBrand = ValidationUtils.requireNotBlank(brand, "브랜드명")
        logger.info("MCP Tool 호출: getProductsByBrand - brand=$validatedBrand")
        return productUseCase.findProductsByBrand(validatedBrand).map { ProductInfo.from(it) }
    }

    @Tool(name = "getProductsByPriceRange", description = "가격 범위로 상품을 검색합니다.")
    fun getProductsByPriceRange(
        @ToolParam(description = "최소 가격", required = true) minPrice: Double,
        @ToolParam(description = "최대 가격", required = true) maxPrice: Double
    ): List<ProductInfo> {
        ValidationUtils.requireNonNegative(minPrice.toInt(), "최소 가격")
        ValidationUtils.requirePositive(maxPrice, "최대 가격")
        ValidationUtils.validateRange(minPrice, maxPrice, "최소 가격", "최대 가격")
        logger.info("MCP Tool 호출: getProductsByPriceRange - minPrice=$minPrice, maxPrice=$maxPrice")
        return productUseCase.findProductsByPriceRange(
            BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice)
        ).map { ProductInfo.from(it) }
    }

    @Tool(name = "getActiveProducts", description = "판매 중인 상품 목록을 조회합니다.")
    fun getActiveProducts(): List<ProductInfo> {
        logger.info("MCP Tool 호출: getActiveProducts")
        return productUseCase.findActiveProducts().map { ProductInfo.from(it) }
    }

    @Tool(name = "getLowStockProducts", description = "재고 부족 상품 목록을 조회합니다.")
    fun getLowStockProducts(): List<ProductInfo> {
        logger.info("MCP Tool 호출: getLowStockProducts")
        return productUseCase.findLowStockProducts().map { ProductInfo.from(it) }
    }

    @Tool(name = "getOutOfStockProducts", description = "품절 상품 목록을 조회합니다.")
    fun getOutOfStockProducts(): List<ProductInfo> {
        logger.info("MCP Tool 호출: getOutOfStockProducts")
        return productUseCase.findOutOfStockProducts().map { ProductInfo.from(it) }
    }

    // ===== Inventory =====

    @Tool(name = "addStock", description = "상품의 재고를 추가합니다.")
    fun addStock(
        @ToolParam(description = "상품 ID", required = true) productId: Long,
        @ToolParam(description = "추가할 수량", required = true) amount: Int
    ): InventoryResult {
        ValidationUtils.requirePositive(productId, "상품 ID")
        ValidationUtils.requirePositive(amount, "추가할 수량")
        logger.info("MCP Tool 호출: addStock - productId=$productId, amount=$amount")

        val inventory = inventoryUseCase.addStock(productId, amount)
        return if (inventory != null) {
            InventoryResult(true, "${amount}개의 재고가 추가되었습니다. 현재 재고: ${inventory.quantity}", InventoryInfo.from(inventory))
        } else {
            InventoryResult(false, "상품을 찾을 수 없습니다.", null)
        }
    }

    @Tool(name = "removeStock", description = "상품의 재고를 차감합니다.")
    fun removeStock(
        @ToolParam(description = "상품 ID", required = true) productId: Long,
        @ToolParam(description = "차감할 수량", required = true) amount: Int
    ): InventoryResult {
        ValidationUtils.requirePositive(productId, "상품 ID")
        ValidationUtils.requirePositive(amount, "차감할 수량")
        logger.info("MCP Tool 호출: removeStock - productId=$productId, amount=$amount")

        val inventory = inventoryUseCase.removeStock(productId, amount)
        return if (inventory != null) {
            InventoryResult(true, "${amount}개의 재고가 차감되었습니다. 현재 재고: ${inventory.quantity}", InventoryInfo.from(inventory))
        } else {
            InventoryResult(false, "상품을 찾을 수 없거나 재고가 부족합니다.", null)
        }
    }

    @Tool(name = "getInventory", description = "상품의 재고 정보를 조회합니다.")
    fun getInventory(
        @ToolParam(description = "상품 ID", required = true) productId: Long
    ): InventoryInfo? {
        logger.info("MCP Tool 호출: getInventory - productId=$productId")
        return inventoryUseCase.getInventory(productId)?.let { InventoryInfo.from(it) }
    }

    // ===== Product Status =====

    @Tool(name = "activateProduct", description = "상품을 판매 중 상태로 변경합니다.")
    fun activateProduct(
        @ToolParam(description = "상품 ID", required = true) productId: Long
    ): ProductResult {
        logger.info("MCP Tool 호출: activateProduct - productId=$productId")
        val product = productUseCase.activateProduct(productId)
        return if (product != null) {
            ProductResult(true, "상품이 활성화되었습니다.", ProductInfo.from(product))
        } else {
            ProductResult(false, "상품을 찾을 수 없습니다.", null)
        }
    }

    @Tool(name = "deactivateProduct", description = "상품을 판매 중지 상태로 변경합니다.")
    fun deactivateProduct(
        @ToolParam(description = "상품 ID", required = true) productId: Long
    ): ProductResult {
        logger.info("MCP Tool 호출: deactivateProduct - productId=$productId")
        val product = productUseCase.deactivateProduct(productId)
        return if (product != null) {
            ProductResult(true, "상품이 비활성화되었습니다.", ProductInfo.from(product))
        } else {
            ProductResult(false, "상품을 찾을 수 없습니다.", null)
        }
    }

    @Tool(name = "discontinueProduct", description = "상품을 단종 처리합니다.")
    fun discontinueProduct(
        @ToolParam(description = "상품 ID", required = true) productId: Long
    ): ProductResult {
        logger.info("MCP Tool 호출: discontinueProduct - productId=$productId")
        val product = productUseCase.discontinueProduct(productId)
        return if (product != null) {
            ProductResult(true, "상품이 단종 처리되었습니다.", ProductInfo.from(product))
        } else {
            ProductResult(false, "상품을 찾을 수 없습니다.", null)
        }
    }

    // ===== Category =====

    @Tool(name = "getAllCategories", description = "모든 카테고리 목록을 조회합니다.")
    fun getAllCategories(): List<CategoryInfo> {
        logger.info("MCP Tool 호출: getAllCategories")
        return categoryUseCase.findAllCategories().map { CategoryInfo.from(it) }
    }

    @Tool(name = "getTopLevelCategories", description = "최상위 카테고리 목록을 조회합니다.")
    fun getTopLevelCategories(): List<CategoryInfo> {
        logger.info("MCP Tool 호출: getTopLevelCategories")
        return categoryUseCase.findTopLevelCategories().map { CategoryInfo.from(it) }
    }

    @Tool(name = "getSubCategories", description = "하위 카테고리 목록을 조회합니다.")
    fun getSubCategories(
        @ToolParam(description = "상위 카테고리 ID", required = true) parentId: Long
    ): List<CategoryInfo> {
        logger.info("MCP Tool 호출: getSubCategories - parentId=$parentId")
        return categoryUseCase.findSubCategories(parentId).map { CategoryInfo.from(it) }
    }

    // ===== Stats =====

    @Tool(name = "getProductStats", description = "상품 통계를 조회합니다.")
    fun getProductStats(): ProductStats {
        logger.info("MCP Tool 호출: getProductStats")
        return productStatsUseCase.getProductStats()
    }
}
