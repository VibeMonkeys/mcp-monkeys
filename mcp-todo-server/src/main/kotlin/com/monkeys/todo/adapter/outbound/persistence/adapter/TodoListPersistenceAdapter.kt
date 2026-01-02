package com.monkeys.todo.adapter.outbound.persistence.adapter

import com.monkeys.todo.adapter.outbound.persistence.entity.TodoListEntity
import com.monkeys.todo.adapter.outbound.persistence.repository.TodoListJpaRepository
import com.monkeys.todo.application.port.outbound.TodoListRepository
import com.monkeys.todo.domain.model.TodoList
import org.springframework.stereotype.Component

@Component
class TodoListPersistenceAdapter(
    private val todoListJpaRepository: TodoListJpaRepository
) : TodoListRepository {

    override fun findById(id: Long): TodoList? =
        todoListJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)

    override fun findByOwnerEmail(ownerEmail: String): List<TodoList> =
        todoListJpaRepository.findByOwnerEmail(ownerEmail).map { it.toDomain() }

    override fun save(todoList: TodoList): TodoList {
        val entity = if (todoList.id != 0L) {
            todoListJpaRepository.findById(todoList.id).orElse(null)?.apply {
                name = todoList.name
                description = todoList.description
                updatedAt = todoList.updatedAt
            } ?: TodoListEntity.fromDomain(todoList)
        } else {
            TodoListEntity.fromDomain(todoList)
        }
        return todoListJpaRepository.save(entity).toDomain()
    }

    override fun delete(id: Long) {
        todoListJpaRepository.deleteById(id)
    }

    override fun existsById(id: Long): Boolean =
        todoListJpaRepository.existsById(id)
}
