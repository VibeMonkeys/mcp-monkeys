package com.monkeys.library.application.port.`in`

import com.monkeys.library.domain.model.Author

interface AuthorUseCase {
    fun searchAuthors(name: String): List<Author>
    fun findAuthorById(id: Long): Author?
    fun getAllAuthors(): List<Author>
}
