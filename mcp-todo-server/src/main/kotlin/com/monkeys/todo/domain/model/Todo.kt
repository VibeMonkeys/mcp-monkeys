package com.monkeys.todo.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class TodoStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

data class Todo(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val todoListId: Long,
    val status: TodoStatus = TodoStatus.PENDING,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val completedAt: LocalDateTime? = null,
    val tags: Set<Tag> = emptySet(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(title: String, description: String?, priority: Priority, dueDate: LocalDate?) = copy(
        title = title,
        description = description,
        priority = priority,
        dueDate = dueDate,
        updatedAt = LocalDateTime.now()
    )

    fun start(): Todo {
        if (status != TodoStatus.PENDING) return this
        return copy(status = TodoStatus.IN_PROGRESS, updatedAt = LocalDateTime.now())
    }

    fun complete() = copy(
        status = TodoStatus.COMPLETED,
        completedAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    fun cancel() = copy(
        status = TodoStatus.CANCELLED,
        updatedAt = LocalDateTime.now()
    )

    fun reopen(): Todo {
        if (status != TodoStatus.COMPLETED && status != TodoStatus.CANCELLED) return this
        return copy(
            status = TodoStatus.PENDING,
            completedAt = null,
            updatedAt = LocalDateTime.now()
        )
    }

    fun addTag(tag: Tag) = copy(
        tags = tags + tag,
        updatedAt = LocalDateTime.now()
    )

    fun removeTag(tag: Tag) = copy(
        tags = tags - tag,
        updatedAt = LocalDateTime.now()
    )

    fun isOverdue(): Boolean =
        dueDate != null &&
        LocalDate.now().isAfter(dueDate) &&
        status != TodoStatus.COMPLETED &&
        status != TodoStatus.CANCELLED
}
