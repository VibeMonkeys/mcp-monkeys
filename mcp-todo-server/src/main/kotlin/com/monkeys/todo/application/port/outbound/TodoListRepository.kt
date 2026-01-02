package com.monkeys.todo.application.port.outbound

import com.monkeys.todo.domain.model.TodoList

interface TodoListRepository {
    fun findById(id: Long): TodoList?
    fun findByOwnerEmail(ownerEmail: String): List<TodoList>
    fun save(todoList: TodoList): TodoList
    fun delete(id: Long)
    fun existsById(id: Long): Boolean
}
