package com.monkeys.todo.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.todo.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TagRepository : JpaRepository<Tag, Long>, KotlinJdslJpqlExecutor {
    fun findByName(name: String): Optional<Tag>
    fun findByNameContainingIgnoreCase(name: String): List<Tag>
}
