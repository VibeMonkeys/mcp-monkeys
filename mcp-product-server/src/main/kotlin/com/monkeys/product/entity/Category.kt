package com.monkeys.product.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "categories")
class Category(
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
    var parent: Category? = null,

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    val children: MutableList<Category> = mutableListOf(),

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    val products: MutableList<Product> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, description: String?) {
        this.name = name
        this.description = description
        this.updatedAt = LocalDateTime.now()
    }

    fun getProductCount(): Int = products.size

    fun isTopLevel(): Boolean = parent == null
}
