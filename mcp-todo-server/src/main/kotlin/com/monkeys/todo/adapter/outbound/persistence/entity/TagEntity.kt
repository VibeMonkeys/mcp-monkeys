package com.monkeys.todo.adapter.outbound.persistence.entity

import com.monkeys.todo.domain.model.Tag
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tags")
class TagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String,

    var color: String = "#808080",

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    val todos: MutableSet<TodoEntity> = mutableSetOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Tag = Tag(
        id = id,
        name = name,
        color = color,
        todoCount = todos.size,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(tag: Tag): TagEntity = TagEntity(
            id = tag.id,
            name = tag.name,
            color = tag.color,
            createdAt = tag.createdAt
        )
    }
}
