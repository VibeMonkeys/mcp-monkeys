package com.monkeys.todo.adapter.outbound.persistence.entity

import com.monkeys.todo.domain.model.Priority
import com.monkeys.todo.domain.model.Todo
import com.monkeys.todo.domain.model.TodoStatus
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "todos")
class TodoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String,

    @Column(length = 2000)
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    var todoList: TodoListEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TodoStatus = TodoStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var priority: Priority = Priority.MEDIUM,

    var dueDate: LocalDate? = null,

    var completedAt: LocalDateTime? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "todo_tags",
        joinColumns = [JoinColumn(name = "todo_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    val tags: MutableSet<TagEntity> = mutableSetOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Todo = Todo(
        id = id,
        title = title,
        description = description,
        todoListId = todoList?.id ?: 0,
        status = status,
        priority = priority,
        dueDate = dueDate,
        completedAt = completedAt,
        tags = tags.map { it.toDomain() }.toSet(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun updateFromDomain(todo: Todo) {
        title = todo.title
        description = todo.description
        status = todo.status
        priority = todo.priority
        dueDate = todo.dueDate
        completedAt = todo.completedAt
        updatedAt = todo.updatedAt
    }

    companion object {
        fun fromDomain(todo: Todo, todoListEntity: TodoListEntity?): TodoEntity = TodoEntity(
            id = todo.id,
            title = todo.title,
            description = todo.description,
            todoList = todoListEntity,
            status = todo.status,
            priority = todo.priority,
            dueDate = todo.dueDate,
            completedAt = todo.completedAt,
            createdAt = todo.createdAt,
            updatedAt = todo.updatedAt
        )
    }
}
