package com.monkeys.product.adapter.`in`.mcp.dto

import com.monkeys.product.domain.model.Product

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
) {
    companion object {
        fun from(product: Product) = ProductInfo(
            id = product.id,
            sku = product.sku,
            name = product.name,
            description = product.description,
            categoryName = product.category?.name,
            price = product.price.toString(),
            costPrice = product.costPrice.toString(),
            brand = product.brand,
            status = product.status.name,
            stockQuantity = product.inventory?.quantity ?: 0,
            isLowStock = product.inventory?.isLowStock() ?: false,
            margin = product.getMargin().toString() + "%"
        )
    }
}

data class ProductResult(
    val success: Boolean,
    val message: String,
    val product: ProductInfo?
)
