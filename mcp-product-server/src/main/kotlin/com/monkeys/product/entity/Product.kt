package com.monkeys.product.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

enum class ProductStatus {
    ACTIVE,       // 판매 중
    INACTIVE,     // 판매 중지
    OUT_OF_STOCK, // 품절
    DISCONTINUED  // 단종
}

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val sku: String,

    @Column(nullable = false)
    var name: String,

    @Column(length = 2000)
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null,

    @Column(nullable = false, precision = 15, scale = 2)
    var price: BigDecimal,

    @Column(precision = 15, scale = 2)
    var costPrice: BigDecimal = BigDecimal.ZERO,

    var brand: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProductStatus = ProductStatus.ACTIVE,

    @OneToOne(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var inventory: Inventory? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, description: String?, price: BigDecimal, brand: String?) {
        this.name = name
        this.description = description
        this.price = price
        this.brand = brand
        this.updatedAt = LocalDateTime.now()
    }

    fun activate() {
        if (status != ProductStatus.DISCONTINUED) {
            status = if (inventory?.quantity ?: 0 > 0) ProductStatus.ACTIVE else ProductStatus.OUT_OF_STOCK
            updatedAt = LocalDateTime.now()
        }
    }

    fun deactivate() {
        status = ProductStatus.INACTIVE
        updatedAt = LocalDateTime.now()
    }

    fun discontinue() {
        status = ProductStatus.DISCONTINUED
        updatedAt = LocalDateTime.now()
    }

    fun updateInventoryStatus() {
        if (status != ProductStatus.DISCONTINUED && status != ProductStatus.INACTIVE) {
            status = if (inventory?.quantity ?: 0 > 0) ProductStatus.ACTIVE else ProductStatus.OUT_OF_STOCK
            updatedAt = LocalDateTime.now()
        }
    }

    fun getMargin(): BigDecimal {
        return if (costPrice > BigDecimal.ZERO) {
            ((price - costPrice) / price * BigDecimal(100)).setScale(2, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }
}
