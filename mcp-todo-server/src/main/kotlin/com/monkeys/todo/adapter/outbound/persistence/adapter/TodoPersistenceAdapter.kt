package com.monkeys.todo.adapter.outbound.persistence.adapter

import com.monkeys.todo.adapter.outbound.persistence.entity.TagEntity
import com.monkeys.todo.adapter.outbound.persistence.entity.TodoEntity
import com.monkeys.todo.adapter.outbound.persistence.repository.TagJpaRepository
import com.monkeys.todo.adapter.outbound.persistence.repository.TodoJpaRepository
import com.monkeys.todo.adapter.outbound.persistence.repository.TodoListJpaRepository
import com.monkeys.todo.application.port.outbound.TodoRepository
import com.monkeys.todo.domain.model.Todo
import com.monkeys.todo.domain.model.TodoStatus
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class TodoPersistenceAdapter(
    private val todoJpaRepository: TodoJpaRepository,
    private val todoListJpaRepository: TodoListJpaRepository,
    private val tagJpaRepository: TagJpaRepository
) : TodoRepository {

    override fun findById(id: Long): Todo? =
        todoJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)

    override fun findByTodoListId(listId: Long): List<Todo> =
        todoJpaRepository.findByTodoListId(listId).map { it.toDomain() }

    override fun findByOwnerEmail(ownerEmail: String): List<Todo> =
        todoJpaRepository.findByOwnerEmail(ownerEmail).map { it.toDomain() }

    override fun findByOwnerEmailAndStatus(ownerEmail: String, status: TodoStatus): List<Todo> =
        todoJpaRepository.findByOwnerEmailAndStatus(ownerEmail, status).map { it.toDomain() }

    override fun findByTitleContaining(keyword: String): List<Todo> =
        todoJpaRepository.findByTitleContaining(keyword).map { it.toDomain() }

    override fun findOverdue(): List<Todo> =
        todoJpaRepository.findOverdue(LocalDate.now()).map { it.toDomain() }

    override fun findByTagName(tagName: String): List<Todo> =
        todoJpaRepository.findByTagName(tagName).map { it.toDomain() }

    override fun save(todo: Todo): Todo {
        val entity: TodoEntity = if (todo.id != 0L) {
            todoJpaRepository.findById(todo.id).orElse(null)?.apply {
                updateFromDomain(todo)
                updateTags(todo)
            } ?: createNewTodoEntity(todo)
        } else {
            createNewTodoEntity(todo)
        }
        return todoJpaRepository.save(entity).toDomain()
    }

    private fun TodoEntity.updateTags(todo: Todo) {
        val currentTagNames = tags.map { it.name }.toSet()
        val newTagNames = todo.tags.map { it.name }.toSet()

        val tagsToRemove = tags.filter { it.name !in newTagNames }
        val tagNamesToAdd = newTagNames - currentTagNames

        tagsToRemove.forEach { tags.remove(it) }
        tagNamesToAdd.forEach { tagName ->
            val tagEntity = tagJpaRepository.findByName(tagName)
                ?: tagJpaRepository.save(TagEntity(name = tagName))
            tags.add(tagEntity)
        }
    }

    private fun createNewTodoEntity(todo: Todo): TodoEntity {
        val todoListEntity = todoListJpaRepository.findById(todo.todoListId).orElse(null)
        val entity = TodoEntity.fromDomain(todo, todoListEntity)
        todo.tags.forEach { tag ->
            val tagEntity = tagJpaRepository.findByName(tag.name)
                ?: tagJpaRepository.save(TagEntity.fromDomain(tag))
            entity.tags.add(tagEntity)
        }
        return entity
    }

    override fun delete(id: Long) {
        todoJpaRepository.deleteById(id)
    }

    override fun countByOwnerEmail(ownerEmail: String): Int =
        todoJpaRepository.countByOwnerEmail(ownerEmail)

    override fun countByOwnerEmailAndStatus(ownerEmail: String, status: TodoStatus): Int =
        todoJpaRepository.countByOwnerEmailAndStatus(ownerEmail, status)

    override fun countOverdueByOwnerEmail(ownerEmail: String): Int =
        todoJpaRepository.countOverdueByOwnerEmail(ownerEmail, LocalDate.now())
}
