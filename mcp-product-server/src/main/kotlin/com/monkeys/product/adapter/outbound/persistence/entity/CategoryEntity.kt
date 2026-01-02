package com.monkeys.product.adapter.outbound.persistence.entity

import com.monkeys.product.domain.model.Category
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "categories")
class CategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = false, unique = true)
    var code: String,

    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: CategoryEntity? = null,

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    val children: MutableList<CategoryEntity> = mutableListOf(),

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    val products: MutableList<ProductEntity> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain() = Category(
        id = id,
        name = name,
        code = code,
        description = description,
        parent = parent?.toDomainShallow(),
        productCount = products.size,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun toDomainShallow() = Category(
        id = id,
        name = name,
        code = code,
        description = description,
        parent = null,
        productCount = 0,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
