package com.monkeys.todo.adapter.`in`.mcp.dto

import com.monkeys.todo.domain.model.Todo

data class TodoDto(
    val id: Long,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val dueDate: String?,
    val completedAt: String?,
    val tags: List<String>,
    val isOverdue: Boolean,
    val createdAt: String
) {
    companion object {
        fun fromDomain(todo: Todo): TodoDto = TodoDto(
            id = todo.id,
            title = todo.title,
            description = todo.description,
            status = todo.status.name,
            priority = todo.priority.name,
            dueDate = todo.dueDate?.toString(),
            completedAt = todo.completedAt?.toString(),
            tags = todo.tags.map { it.name },
            isOverdue = todo.isOverdue(),
            createdAt = todo.createdAt.toString()
        )
    }
}
