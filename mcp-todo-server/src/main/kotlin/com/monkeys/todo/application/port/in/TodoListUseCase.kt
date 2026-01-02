package com.monkeys.todo.application.port.`in`

import com.monkeys.todo.domain.model.TodoList

interface TodoListUseCase {
    fun createList(name: String, description: String?, ownerEmail: String): TodoList
    fun findListById(id: Long): TodoList?
    fun findListsByOwner(ownerEmail: String): List<TodoList>
    fun updateList(id: Long, name: String, description: String?): TodoList?
    fun deleteList(id: Long): Boolean
}
