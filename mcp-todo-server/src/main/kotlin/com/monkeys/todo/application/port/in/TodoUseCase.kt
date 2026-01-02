package com.monkeys.todo.application.port.`in`

import com.monkeys.todo.domain.model.Priority
import com.monkeys.todo.domain.model.Todo
import com.monkeys.todo.domain.model.TodoStatus
import java.time.LocalDate

interface TodoUseCase {
    fun createTodo(listId: Long, title: String, description: String?, priority: Priority, dueDate: LocalDate?): Todo?
    fun findTodoById(id: Long): Todo?
    fun findTodosByList(listId: Long): List<Todo>
    fun findTodosByOwner(ownerEmail: String): List<Todo>
    fun findTodosByOwnerAndStatus(ownerEmail: String, status: TodoStatus): List<Todo>
    fun searchTodos(keyword: String): List<Todo>
    fun findOverdueTodos(): List<Todo>
    fun startTodo(id: Long): Todo?
    fun completeTodo(id: Long): Todo?
    fun cancelTodo(id: Long): Todo?
    fun reopenTodo(id: Long): Todo?
    fun updateTodo(id: Long, title: String, description: String?, priority: Priority, dueDate: LocalDate?): Todo?
}
