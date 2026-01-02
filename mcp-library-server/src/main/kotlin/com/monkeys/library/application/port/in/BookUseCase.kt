package com.monkeys.library.application.port.`in`

import com.monkeys.library.domain.model.Book

interface BookUseCase {
    fun searchBooks(keyword: String): List<Book>
    fun findBookByIsbn(isbn: String): Book?
    fun findBookById(id: Long): Book?
    fun findBooksByCategory(category: String): List<Book>
    fun findBooksByAuthor(authorName: String): List<Book>
    fun findAvailableBooks(): List<Book>
    fun getAllBooks(): List<Book>
}
