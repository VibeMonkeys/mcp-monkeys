package com.monkeys.todo.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.todo.entity.TodoList
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TodoListRepository : JpaRepository<TodoList, Long>, KotlinJdslJpqlExecutor {
    fun findByOwnerEmail(ownerEmail: String): List<TodoList>
    fun findByNameContainingIgnoreCase(name: String): List<TodoList>
}
