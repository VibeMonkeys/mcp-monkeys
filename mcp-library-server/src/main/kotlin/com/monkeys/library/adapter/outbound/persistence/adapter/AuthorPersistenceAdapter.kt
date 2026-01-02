package com.monkeys.library.adapter.outbound.persistence.adapter

import com.monkeys.library.adapter.outbound.persistence.repository.AuthorJpaRepository
import com.monkeys.library.application.port.outbound.AuthorRepository
import com.monkeys.library.domain.model.Author
import org.springframework.stereotype.Repository

@Repository
class AuthorPersistenceAdapter(
    private val authorJpaRepository: AuthorJpaRepository
) : AuthorRepository {

    override fun findById(id: Long): Author? =
        authorJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByNameContaining(name: String): List<Author> =
        authorJpaRepository.findByNameContainingIgnoreCase(name).map { it.toDomain() }

    override fun findAll(): List<Author> =
        authorJpaRepository.findAll().map { it.toDomain() }

    override fun count(): Long =
        authorJpaRepository.count()
}
