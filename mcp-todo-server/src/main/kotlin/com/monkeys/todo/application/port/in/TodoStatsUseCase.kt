package com.monkeys.todo.application.port.`in`

import com.monkeys.todo.domain.model.TodoStats

interface TodoStatsUseCase {
    fun getStats(ownerEmail: String): TodoStats
}
