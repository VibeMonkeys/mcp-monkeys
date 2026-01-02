package com.monkeys.todo.adapter.`in`.mcp.dto

import com.monkeys.todo.domain.model.TodoStats

data class TodoStatsDto(
    val totalTodos: Int,
    val pendingTodos: Int,
    val inProgressTodos: Int,
    val completedTodos: Int,
    val cancelledTodos: Int,
    val overdueTodos: Int,
    val completionRate: Double
) {
    companion object {
        fun fromDomain(stats: TodoStats): TodoStatsDto = TodoStatsDto(
            totalTodos = stats.totalTodos,
            pendingTodos = stats.pendingTodos,
            inProgressTodos = stats.inProgressTodos,
            completedTodos = stats.completedTodos,
            cancelledTodos = stats.cancelledTodos,
            overdueTodos = stats.overdueTodos,
            completionRate = stats.completionRate
        )
    }
}
