package com.monkeys.todo.domain.model

data class TodoStats(
    val totalTodos: Int,
    val pendingTodos: Int,
    val inProgressTodos: Int,
    val completedTodos: Int,
    val cancelledTodos: Int,
    val overdueTodos: Int,
    val completionRate: Double
)
