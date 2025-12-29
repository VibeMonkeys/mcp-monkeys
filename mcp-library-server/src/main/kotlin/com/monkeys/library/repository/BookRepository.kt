package com.monkeys.library.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.library.entity.Book
import com.monkeys.library.entity.BookStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookRepository : JpaRepository<Book, Long>, KotlinJdslJpqlExecutor {

    fun findByIsbn(isbn: String): Book?

    fun findByTitleContainingIgnoreCase(title: String): List<Book>

    fun findByCategory(category: String): List<Book>

    fun findByStatus(status: BookStatus): List<Book>

    fun findByAuthorId(authorId: Long): List<Book>

    fun findByAuthorNameContainingIgnoreCase(name: String): List<Book>
}
