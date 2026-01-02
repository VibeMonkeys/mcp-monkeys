package com.monkeys.todo.application.port.outbound

import com.monkeys.todo.domain.model.Todo
import com.monkeys.todo.domain.model.TodoStatus

interface TodoRepository {
    fun findById(id: Long): Todo?
    fun findByTodoListId(listId: Long): List<Todo>
    fun findByOwnerEmail(ownerEmail: String): List<Todo>
    fun findByOwnerEmailAndStatus(ownerEmail: String, status: TodoStatus): List<Todo>
    fun findByTitleContaining(keyword: String): List<Todo>
    fun findOverdue(): List<Todo>
    fun findByTagName(tagName: String): List<Todo>
    fun save(todo: Todo): Todo
    fun delete(id: Long)
    fun countByOwnerEmail(ownerEmail: String): Int
    fun countByOwnerEmailAndStatus(ownerEmail: String, status: TodoStatus): Int
    fun countOverdueByOwnerEmail(ownerEmail: String): Int
}
