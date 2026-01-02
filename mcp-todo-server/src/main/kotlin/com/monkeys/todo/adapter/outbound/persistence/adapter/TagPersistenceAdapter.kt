package com.monkeys.todo.adapter.outbound.persistence.adapter

import com.monkeys.todo.adapter.outbound.persistence.entity.TagEntity
import com.monkeys.todo.adapter.outbound.persistence.repository.TagJpaRepository
import com.monkeys.todo.application.port.outbound.TagRepository
import com.monkeys.todo.domain.model.Tag
import org.springframework.stereotype.Component

@Component
class TagPersistenceAdapter(
    private val tagJpaRepository: TagJpaRepository
) : TagRepository {

    override fun findById(id: Long): Tag? =
        tagJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)

    override fun findByName(name: String): Tag? =
        tagJpaRepository.findByName(name)?.toDomain()

    override fun findAll(): List<Tag> =
        tagJpaRepository.findAll().map { it.toDomain() }

    override fun save(tag: Tag): Tag {
        val entity = if (tag.id != 0L) {
            tagJpaRepository.findById(tag.id).orElse(null)?.apply {
                name = tag.name
                color = tag.color
            } ?: TagEntity.fromDomain(tag)
        } else {
            TagEntity.fromDomain(tag)
        }
        return tagJpaRepository.save(entity).toDomain()
    }

    override fun delete(id: Long) {
        tagJpaRepository.deleteById(id)
    }
}
