package com.monkeys.todo.adapter.`in`.mcp.dto

import com.monkeys.todo.domain.model.TodoList

data class TodoListDto(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerEmail: String,
    val todoCount: Int,
    val completedCount: Int,
    val pendingCount: Int,
    val createdAt: String
) {
    companion object {
        fun fromDomain(todoList: TodoList): TodoListDto = TodoListDto(
            id = todoList.id,
            name = todoList.name,
            description = todoList.description,
            ownerEmail = todoList.ownerEmail,
            todoCount = todoList.getTotalCount(),
            completedCount = todoList.getCompletedCount(),
            pendingCount = todoList.getPendingCount(),
            createdAt = todoList.createdAt.toString()
        )
    }
}

data class TodoListDetailDto(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerEmail: String,
    val todos: List<TodoDto>,
    val createdAt: String
) {
    companion object {
        fun fromDomain(todoList: TodoList): TodoListDetailDto = TodoListDetailDto(
            id = todoList.id,
            name = todoList.name,
            description = todoList.description,
            ownerEmail = todoList.ownerEmail,
            todos = todoList.todos.map { TodoDto.fromDomain(it) },
            createdAt = todoList.createdAt.toString()
        )
    }
}
