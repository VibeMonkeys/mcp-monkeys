package com.monkeys.todo.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

enum class TodoStatus {
    PENDING,      // 대기 중
    IN_PROGRESS,  // 진행 중
    COMPLETED,    // 완료
    CANCELLED     // 취소됨
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

@Entity
@Table(name = "todos")
class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String,

    @Column(length = 2000)
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    var todoList: TodoList? = null,

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
    val tags: MutableSet<Tag> = mutableSetOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun update(title: String, description: String?, priority: Priority, dueDate: LocalDate?) {
        this.title = title
        this.description = description
        this.priority = priority
        this.dueDate = dueDate
        this.updatedAt = LocalDateTime.now()
    }

    fun start() {
        if (status == TodoStatus.PENDING) {
            status = TodoStatus.IN_PROGRESS
            updatedAt = LocalDateTime.now()
        }
    }

    fun complete() {
        status = TodoStatus.COMPLETED
        completedAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    fun cancel() {
        status = TodoStatus.CANCELLED
        updatedAt = LocalDateTime.now()
    }

    fun reopen() {
        if (status == TodoStatus.COMPLETED || status == TodoStatus.CANCELLED) {
            status = TodoStatus.PENDING
            completedAt = null
            updatedAt = LocalDateTime.now()
        }
    }

    fun addTag(tag: Tag) {
        tags.add(tag)
    }

    fun removeTag(tag: Tag) {
        tags.remove(tag)
    }

    fun isOverdue(): Boolean {
        return dueDate != null &&
               LocalDate.now().isAfter(dueDate) &&
               status != TodoStatus.COMPLETED &&
               status != TodoStatus.CANCELLED
    }
}
