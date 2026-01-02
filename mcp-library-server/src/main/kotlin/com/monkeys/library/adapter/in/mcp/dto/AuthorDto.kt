package com.monkeys.library.adapter.`in`.mcp.dto

import com.monkeys.library.domain.model.Author

data class AuthorInfo(
    val id: Long,
    val name: String,
    val nationality: String?,
    val biography: String?,
    val bookCount: Int
) {
    companion object {
        fun from(author: Author) = AuthorInfo(
            id = author.id,
            name = author.name,
            nationality = author.nationality,
            biography = author.biography,
            bookCount = author.bookCount
        )
    }
}
