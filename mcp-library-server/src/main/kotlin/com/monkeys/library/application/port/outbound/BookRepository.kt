package com.monkeys.library.application.port.outbound

import com.monkeys.library.domain.model.Book
import com.monkeys.library.domain.model.BookStatus

interface BookRepository {
    fun findById(id: Long): Book?
    fun findByIsbn(isbn: String): Book?
    fun findByTitleContaining(keyword: String): List<Book>
    fun findByCategory(category: String): List<Book>
    fun findByAuthorNameContaining(authorName: String): List<Book>
    fun findByStatus(status: BookStatus): List<Book>
    fun findAll(): List<Book>
    fun save(book: Book): Book
    fun count(): Long
    fun countByStatus(status: BookStatus): Long
}
