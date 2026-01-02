package com.monkeys.todo.adapter.outbound.persistence.repository

import com.monkeys.todo.adapter.outbound.persistence.entity.TagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TagJpaRepository : JpaRepository<TagEntity, Long> {
    fun findByName(name: String): TagEntity?
}
