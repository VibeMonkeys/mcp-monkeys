package com.monkeys.todo.domain.model

import java.time.LocalDateTime

data class Tag(
    val id: Long = 0,
    val name: String,
    val color: String = "#808080",
    val todoCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
