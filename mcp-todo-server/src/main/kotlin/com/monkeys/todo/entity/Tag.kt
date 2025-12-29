package com.monkeys.todo.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tags")
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String,

    var color: String = "#808080",

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    val todos: MutableSet<Todo> = mutableSetOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
