package com.monkeys.library.adapter.`in`.mcp.dto

import com.monkeys.library.domain.model.Book

data class BookInfo(
    val id: Long,
    val title: String,
    val isbn: String,
    val authorName: String,
    val publisher: String?,
    val category: String?,
    val description: String?,
    val availableCopies: Int,
    val totalCopies: Int,
    val status: String
) {
    companion object {
        fun from(book: Book) = BookInfo(
            id = book.id,
            title = book.title,
            isbn = book.isbn,
            authorName = book.author.name,
            publisher = book.publisher,
            category = book.category,
            description = book.description,
            availableCopies = book.availableCopies,
            totalCopies = book.totalCopies,
            status = book.status.name
        )
    }
}
