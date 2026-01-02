package com.monkeys.todo.application.service

import com.monkeys.todo.application.port.`in`.TagUseCase
import com.monkeys.todo.application.port.`in`.TodoListUseCase
import com.monkeys.todo.application.port.`in`.TodoStatsUseCase
import com.monkeys.todo.application.port.`in`.TodoUseCase
import com.monkeys.todo.application.port.outbound.TagRepository
import com.monkeys.todo.application.port.outbound.TodoListRepository
import com.monkeys.todo.application.port.outbound.TodoRepository
import com.monkeys.todo.domain.model.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class TodoApplicationService(
    private val todoListRepository: TodoListRepository,
    private val todoRepository: TodoRepository,
    private val tagRepository: TagRepository
) : TodoListUseCase, TodoUseCase, TagUseCase, TodoStatsUseCase {

    // TodoListUseCase implementation
    override fun createList(name: String, description: String?, ownerEmail: String): TodoList {
        val todoList = TodoList(
            name = name,
            description = description,
            ownerEmail = ownerEmail
        )
        return todoListRepository.save(todoList)
    }

    @Transactional(readOnly = true)
    override fun findListById(id: Long): TodoList? =
        todoListRepository.findById(id)

    @Transactional(readOnly = true)
    override fun findListsByOwner(ownerEmail: String): List<TodoList> =
        todoListRepository.findByOwnerEmail(ownerEmail)

    override fun updateList(id: Long, name: String, description: String?): TodoList? {
        val todoList = todoListRepository.findById(id) ?: return null
        val updatedList = todoList.update(name, description)
        return todoListRepository.save(updatedList)
    }

    override fun deleteList(id: Long): Boolean {
        if (!todoListRepository.existsById(id)) return false
        todoListRepository.delete(id)
        return true
    }

    // TodoUseCase implementation
    override fun createTodo(listId: Long, title: String, description: String?, priority: Priority, dueDate: LocalDate?): Todo? {
        if (todoListRepository.findById(listId) == null) return null
        val todo = Todo(
            title = title,
            description = description,
            todoListId = listId,
            priority = priority,
            dueDate = dueDate
        )
        return todoRepository.save(todo)
    }

    @Transactional(readOnly = true)
    override fun findTodoById(id: Long): Todo? =
        todoRepository.findById(id)

    @Transactional(readOnly = true)
    override fun findTodosByList(listId: Long): List<Todo> =
        todoRepository.findByTodoListId(listId)

    @Transactional(readOnly = true)
    override fun findTodosByOwner(ownerEmail: String): List<Todo> =
        todoRepository.findByOwnerEmail(ownerEmail)

    @Transactional(readOnly = true)
    override fun findTodosByOwnerAndStatus(ownerEmail: String, status: TodoStatus): List<Todo> =
        todoRepository.findByOwnerEmailAndStatus(ownerEmail, status)

    @Transactional(readOnly = true)
    override fun searchTodos(keyword: String): List<Todo> =
        todoRepository.findByTitleContaining(keyword)

    @Transactional(readOnly = true)
    override fun findOverdueTodos(): List<Todo> =
        todoRepository.findOverdue()

    override fun startTodo(id: Long): Todo? {
        val todo = todoRepository.findById(id) ?: return null
        val startedTodo = todo.start()
        return todoRepository.save(startedTodo)
    }

    override fun completeTodo(id: Long): Todo? {
        val todo = todoRepository.findById(id) ?: return null
        val completedTodo = todo.complete()
        return todoRepository.save(completedTodo)
    }

    override fun cancelTodo(id: Long): Todo? {
        val todo = todoRepository.findById(id) ?: return null
        val cancelledTodo = todo.cancel()
        return todoRepository.save(cancelledTodo)
    }

    override fun reopenTodo(id: Long): Todo? {
        val todo = todoRepository.findById(id) ?: return null
        val reopenedTodo = todo.reopen()
        return todoRepository.save(reopenedTodo)
    }

    override fun updateTodo(id: Long, title: String, description: String?, priority: Priority, dueDate: LocalDate?): Todo? {
        val todo = todoRepository.findById(id) ?: return null
        val updatedTodo = todo.update(title, description, priority, dueDate)
        return todoRepository.save(updatedTodo)
    }

    // TagUseCase implementation
    @Transactional(readOnly = true)
    override fun findAllTags(): List<Tag> =
        tagRepository.findAll()

    override fun findOrCreateTag(name: String, color: String?): Tag {
        val existingTag = tagRepository.findByName(name)
        if (existingTag != null) return existingTag
        val newTag = Tag(name = name, color = color ?: "#808080")
        return tagRepository.save(newTag)
    }

    override fun addTagToTodo(todoId: Long, tagName: String): Todo? {
        val todo = todoRepository.findById(todoId) ?: return null
        val tag = findOrCreateTag(tagName)
        val updatedTodo = todo.addTag(tag)
        return todoRepository.save(updatedTodo)
    }

    override fun removeTagFromTodo(todoId: Long, tagName: String): Todo? {
        val todo = todoRepository.findById(todoId) ?: return null
        val tag = tagRepository.findByName(tagName) ?: return todo
        val updatedTodo = todo.removeTag(tag)
        return todoRepository.save(updatedTodo)
    }

    @Transactional(readOnly = true)
    override fun findTodosByTag(tagName: String): List<Todo> =
        todoRepository.findByTagName(tagName)

    // TodoStatsUseCase implementation
    @Transactional(readOnly = true)
    override fun getStats(ownerEmail: String): TodoStats {
        val total = todoRepository.countByOwnerEmail(ownerEmail)
        val pending = todoRepository.countByOwnerEmailAndStatus(ownerEmail, TodoStatus.PENDING)
        val inProgress = todoRepository.countByOwnerEmailAndStatus(ownerEmail, TodoStatus.IN_PROGRESS)
        val completed = todoRepository.countByOwnerEmailAndStatus(ownerEmail, TodoStatus.COMPLETED)
        val cancelled = todoRepository.countByOwnerEmailAndStatus(ownerEmail, TodoStatus.CANCELLED)
        val overdue = todoRepository.countOverdueByOwnerEmail(ownerEmail)
        val completionRate = if (total > 0) (completed.toDouble() / total * 100) else 0.0

        return TodoStats(
            totalTodos = total,
            pendingTodos = pending,
            inProgressTodos = inProgress,
            completedTodos = completed,
            cancelledTodos = cancelled,
            overdueTodos = overdue,
            completionRate = completionRate
        )
    }
}
