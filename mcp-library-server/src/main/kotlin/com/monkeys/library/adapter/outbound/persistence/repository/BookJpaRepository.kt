package com.monkeys.library.adapter.outbound.persistence.repository

import com.monkeys.library.adapter.outbound.persistence.entity.BookEntity
import com.monkeys.library.domain.model.BookStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookJpaRepository : JpaRepository<BookEntity, Long> {
    fun findByIsbn(isbn: String): BookEntity?
    fun findByTitleContainingIgnoreCase(keyword: String): List<BookEntity>
    fun findByCategory(category: String): List<BookEntity>

    @Query("SELECT b FROM BookEntity b WHERE LOWER(b.author.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    fun findByAuthorNameContainingIgnoreCase(authorName: String): List<BookEntity>

    fun findByStatus(status: BookStatus): List<BookEntity>
    fun countByStatus(status: BookStatus): Long
}
