package com.monkeys.todo.domain.model

import java.time.LocalDateTime

data class TodoList(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val ownerEmail: String,
    val todos: List<Todo> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, description: String?) = copy(
        name = name,
        description = description,
        updatedAt = LocalDateTime.now()
    )

    fun getCompletedCount(): Int = todos.count { it.status == TodoStatus.COMPLETED }

    fun getPendingCount(): Int = todos.count { it.status != TodoStatus.COMPLETED }

    fun getTotalCount(): Int = todos.size
}
