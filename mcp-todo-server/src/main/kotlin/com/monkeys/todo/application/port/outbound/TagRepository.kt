package com.monkeys.todo.application.port.outbound

import com.monkeys.todo.domain.model.Tag

interface TagRepository {
    fun findById(id: Long): Tag?
    fun findByName(name: String): Tag?
    fun findAll(): List<Tag>
    fun save(tag: Tag): Tag
    fun delete(id: Long)
}
