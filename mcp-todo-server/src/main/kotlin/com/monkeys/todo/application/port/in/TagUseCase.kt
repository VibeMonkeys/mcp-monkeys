package com.monkeys.todo.application.port.`in`

import com.monkeys.todo.domain.model.Tag
import com.monkeys.todo.domain.model.Todo

interface TagUseCase {
    fun findAllTags(): List<Tag>
    fun findOrCreateTag(name: String, color: String? = null): Tag
    fun addTagToTodo(todoId: Long, tagName: String): Todo?
    fun removeTagFromTodo(todoId: Long, tagName: String): Todo?
    fun findTodosByTag(tagName: String): List<Todo>
}
