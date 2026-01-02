package com.monkeys.library.application.port.outbound

import com.monkeys.library.domain.model.Author

interface AuthorRepository {
    fun findById(id: Long): Author?
    fun findByNameContaining(name: String): List<Author>
    fun findAll(): List<Author>
    fun count(): Long
}
