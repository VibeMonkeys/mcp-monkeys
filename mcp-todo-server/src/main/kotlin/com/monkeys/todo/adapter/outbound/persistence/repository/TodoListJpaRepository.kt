package com.monkeys.todo.adapter.outbound.persistence.repository

import com.monkeys.todo.adapter.outbound.persistence.entity.TodoListEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TodoListJpaRepository : JpaRepository<TodoListEntity, Long> {
    fun findByOwnerEmail(ownerEmail: String): List<TodoListEntity>
}
