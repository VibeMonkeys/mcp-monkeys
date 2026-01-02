package com.monkeys.library.domain.model

data class Author(
    val id: Long = 0,
    val name: String,
    val nationality: String? = null,
    val biography: String? = null,
    val bookCount: Int = 0
)
