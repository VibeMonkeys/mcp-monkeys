package com.monkeys.library.domain.model

import java.time.LocalDate

data class Book(
    val id: Long = 0,
    val title: String,
    val isbn: String,
    val author: Author,
    val publisher: String? = null,
    val publishedDate: LocalDate? = null,
    val category: String? = null,
    val description: String? = null,
    val totalCopies: Int = 1,
    val availableCopies: Int = 1,
    val status: BookStatus = BookStatus.AVAILABLE
) {
    fun isAvailable(): Boolean = availableCopies > 0 && status == BookStatus.AVAILABLE

    fun borrow(): Book {
        if (!isAvailable()) return this
        val newAvailable = availableCopies - 1
        return copy(
            availableCopies = newAvailable,
            status = if (newAvailable == 0) BookStatus.ALL_BORROWED else status
        )
    }

    fun returnBook(): Book {
        val newAvailable = availableCopies + 1
        return copy(
            availableCopies = newAvailable,
            status = if (newAvailable > 0) BookStatus.AVAILABLE else status
        )
    }
}

enum class BookStatus {
    AVAILABLE,
    ALL_BORROWED,
    UNAVAILABLE
}
