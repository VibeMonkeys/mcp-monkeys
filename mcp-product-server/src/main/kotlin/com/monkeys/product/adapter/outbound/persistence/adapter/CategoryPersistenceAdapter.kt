package com.monkeys.product.adapter.outbound.persistence.adapter

import com.monkeys.product.adapter.outbound.persistence.repository.CategoryJpaRepository
import com.monkeys.product.application.port.outbound.CategoryRepository
import com.monkeys.product.domain.model.Category
import org.springframework.stereotype.Repository

@Repository
class CategoryPersistenceAdapter(
    private val categoryJpaRepository: CategoryJpaRepository
) : CategoryRepository {

    override fun findById(id: Long): Category? =
        categoryJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByCode(code: String): Category? =
        categoryJpaRepository.findByCode(code).orElse(null)?.toDomain()

    override fun findByParentIsNull(): List<Category> =
        categoryJpaRepository.findByParentIsNull().map { it.toDomain() }

    override fun findByParentId(parentId: Long): List<Category> =
        categoryJpaRepository.findByParentId(parentId).map { it.toDomain() }

    override fun findAll(): List<Category> =
        categoryJpaRepository.findAll().map { it.toDomain() }

    override fun count(): Long =
        categoryJpaRepository.count()
}
