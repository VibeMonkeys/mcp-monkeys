package com.monkeys.todo.adapter.outbound.persistence.entity

import com.monkeys.todo.domain.model.TodoList
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "todo_lists")
class TodoListEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    var description: String? = null,

    @Column(nullable = false)
    var ownerEmail: String,

    @OneToMany(mappedBy = "todoList", cascade = [CascadeType.ALL], orphanRemoval = true)
    val todos: MutableList<TodoEntity> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): TodoList = TodoList(
        id = id,
        name = name,
        description = description,
        ownerEmail = ownerEmail,
        todos = todos.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun toDomainWithoutTodos(): TodoList = TodoList(
        id = id,
        name = name,
        description = description,
        ownerEmail = ownerEmail,
        todos = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(todoList: TodoList): TodoListEntity = TodoListEntity(
            id = todoList.id,
            name = todoList.name,
            description = todoList.description,
            ownerEmail = todoList.ownerEmail,
            createdAt = todoList.createdAt,
            updatedAt = todoList.updatedAt
        )
    }
}
