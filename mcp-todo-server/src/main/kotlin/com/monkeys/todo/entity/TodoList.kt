package com.monkeys.todo.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "todo_lists")
class TodoList(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    var description: String? = null,

    @Column(nullable = false)
    var ownerEmail: String,

    @OneToMany(mappedBy = "todoList", cascade = [CascadeType.ALL], orphanRemoval = true)
    val todos: MutableList<Todo> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(name: String, description: String?) {
        this.name = name
        this.description = description
        this.updatedAt = LocalDateTime.now()
    }

    fun addTodo(todo: Todo) {
        todos.add(todo)
        todo.todoList = this
    }

    fun getCompletedCount(): Int = todos.count { it.status == TodoStatus.COMPLETED }

    fun getPendingCount(): Int = todos.count { it.status != TodoStatus.COMPLETED }
}
