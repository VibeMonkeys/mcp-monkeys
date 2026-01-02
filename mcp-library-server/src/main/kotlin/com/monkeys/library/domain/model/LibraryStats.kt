package com.monkeys.library.domain.model

data class LibraryStats(
    val totalBooks: Long,
    val availableBooks: Int,
    val borrowedBooks: Long,
    val activeLoans: Int,
    val overdueLoans: Int,
    val totalAuthors: Long
)
