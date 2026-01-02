package com.monkeys.library.adapter.outbound.persistence.repository

import com.monkeys.library.adapter.outbound.persistence.entity.AuthorEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AuthorJpaRepository : JpaRepository<AuthorEntity, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<AuthorEntity>
}
