package com.monkeys.library.adapter.outbound.persistence.adapter

import com.monkeys.library.adapter.outbound.persistence.repository.BookJpaRepository
import com.monkeys.library.application.port.outbound.BookRepository
import com.monkeys.library.domain.model.Book
import com.monkeys.library.domain.model.BookStatus
import org.springframework.stereotype.Repository

@Repository
class BookPersistenceAdapter(
    private val bookJpaRepository: BookJpaRepository
) : BookRepository {

    override fun findById(id: Long): Book? =
        bookJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByIsbn(isbn: String): Book? =
        bookJpaRepository.findByIsbn(isbn)?.toDomain()

    override fun findByTitleContaining(keyword: String): List<Book> =
        bookJpaRepository.findByTitleContainingIgnoreCase(keyword).map { it.toDomain() }

    override fun findByCategory(category: String): List<Book> =
        bookJpaRepository.findByCategory(category).map { it.toDomain() }

    override fun findByAuthorNameContaining(authorName: String): List<Book> =
        bookJpaRepository.findByAuthorNameContainingIgnoreCase(authorName).map { it.toDomain() }

    override fun findByStatus(status: BookStatus): List<Book> =
        bookJpaRepository.findByStatus(status).map { it.toDomain() }

    override fun findAll(): List<Book> =
        bookJpaRepository.findAll().map { it.toDomain() }

    override fun save(book: Book): Book {
        val entity = bookJpaRepository.findById(book.id).orElse(null)
            ?: throw IllegalArgumentException("Book not found: ${book.id}")
        entity.updateFrom(book)
        return bookJpaRepository.save(entity).toDomain()
    }

    override fun count(): Long =
        bookJpaRepository.count()

    override fun countByStatus(status: BookStatus): Long =
        bookJpaRepository.countByStatus(status)
}
