package com.monkeys.todo.adapter.`in`.mcp.dto

import com.monkeys.todo.domain.model.Tag

data class TagDto(
    val id: Long,
    val name: String,
    val color: String,
    val todoCount: Int
) {
    companion object {
        fun fromDomain(tag: Tag): TagDto = TagDto(
            id = tag.id,
            name = tag.name,
            color = tag.color,
            todoCount = tag.todoCount
        )
    }
}
