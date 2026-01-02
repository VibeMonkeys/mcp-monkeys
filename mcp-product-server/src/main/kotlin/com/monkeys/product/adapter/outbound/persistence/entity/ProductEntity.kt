package com.monkeys.product.adapter.outbound.persistence.entity

import com.monkeys.product.domain.model.Product
import com.monkeys.product.domain.model.ProductStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class ProductEntity(
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
    var category: CategoryEntity? = null,

    @Column(nullable = false, precision = 15, scale = 2)
    var price: BigDecimal,

    @Column(precision = 15, scale = 2)
    var costPrice: BigDecimal = BigDecimal.ZERO,

    var brand: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProductStatus = ProductStatus.ACTIVE,

    @OneToOne(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var inventory: InventoryEntity? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain() = Product(
        id = id,
        sku = sku,
        name = name,
        description = description,
        category = category?.toDomain(),
        price = price,
        costPrice = costPrice,
        brand = brand,
        status = status,
        inventory = inventory?.toDomain(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun updateFrom(product: Product) {
        this.name = product.name
        this.description = product.description
        this.price = product.price
        this.costPrice = product.costPrice
        this.brand = product.brand
        this.status = product.status
        this.updatedAt = product.updatedAt
    }
}
