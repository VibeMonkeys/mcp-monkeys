package com.monkeys.todo.adapter.`in`.mcp.dto

data class TodoResultDto(
    val success: Boolean,
    val message: String,
    val todo: TodoDto?
)
