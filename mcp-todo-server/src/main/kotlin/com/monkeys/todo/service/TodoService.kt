package com.monkeys.todo.service

import com.monkeys.todo.entity.*
import com.monkeys.todo.repository.TagRepository
import com.monkeys.todo.repository.TodoListRepository
import com.monkeys.todo.repository.TodoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class TodoService(
    private val todoListRepository: TodoListRepository,
    private val todoRepository: TodoRepository,
    private val tagRepository: TagRepository
) {

    // ===== TodoList Operations =====

    fun findAllLists(): List<TodoList> =
        todoListRepository.findAll()

    fun findListById(id: Long): TodoList? =
        todoListRepository.findById(id).orElse(null)

    fun findListsByOwner(email: String): List<TodoList> =
        todoListRepository.findByOwnerEmail(email)

    @Transactional
    fun createList(name: String, description: String?, ownerEmail: String): TodoList {
        val todoList = TodoList(
            name = name,
            description = description,
            ownerEmail = ownerEmail
        )
        return todoListRepository.save(todoList)
    }

    @Transactional
    fun updateList(id: Long, name: String, description: String?): TodoList? {
        val todoList = todoListRepository.findById(id).orElse(null) ?: return null
        todoList.update(name, description)
        return todoListRepository.save(todoList)
    }

    @Transactional
    fun deleteList(id: Long): Boolean {
        if (!todoListRepository.existsById(id)) return false
        todoListRepository.deleteById(id)
        return true
    }

    // ===== Todo Operations =====

    fun findTodoById(id: Long): Todo? =
        todoRepository.findById(id).orElse(null)

    fun findTodosByList(listId: Long): List<Todo> =
        todoRepository.findByTodoListId(listId)

    fun findTodosByOwner(email: String): List<Todo> =
        todoRepository.findByOwnerEmail(email)

    fun findTodosByStatus(status: TodoStatus): List<Todo> =
        todoRepository.findByStatus(status)

    fun findTodosByOwnerAndStatus(email: String, status: TodoStatus): List<Todo> =
        todoRepository.findByOwnerEmailAndStatus(email, status)

    fun searchTodos(keyword: String): List<Todo> =
        todoRepository.findByTitleContainingIgnoreCase(keyword)

    fun findOverdueTodos(): List<Todo> =
        todoRepository.findOverdueTodos(LocalDate.now())

    fun findTodosByTag(tagName: String): List<Todo> =
        todoRepository.findByTagName(tagName)

    @Transactional
    fun createTodo(
        listId: Long,
        title: String,
        description: String?,
        priority: Priority,
        dueDate: LocalDate?
    ): Todo? {
        val todoList = todoListRepository.findById(listId).orElse(null) ?: return null

        val todo = Todo(
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate
        )
        todoList.addTodo(todo)
        return todoRepository.save(todo)
    }

    @Transactional
    fun updateTodo(
        id: Long,
        title: String,
        description: String?,
        priority: Priority,
        dueDate: LocalDate?
    ): Todo? {
        val todo = todoRepository.findById(id).orElse(null) ?: return null
        todo.update(title, description, priority, dueDate)
        return todoRepository.save(todo)
    }

    @Transactional
    fun startTodo(id: Long): Todo? {
        val todo = todoRepository.findById(id).orElse(null) ?: return null
        todo.start()
        return todoRepository.save(todo)
    }

    @Transactional
    fun completeTodo(id: Long): Todo? {
        val todo = todoRepository.findById(id).orElse(null) ?: return null
        todo.complete()
        return todoRepository.save(todo)
    }

    @Transactional
    fun cancelTodo(id: Long): Todo? {
        val todo = todoRepository.findById(id).orElse(null) ?: return null
        todo.cancel()
        return todoRepository.save(todo)
    }

    @Transactional
    fun reopenTodo(id: Long): Todo? {
        val todo = todoRepository.findById(id).orElse(null) ?: return null
        todo.reopen()
        return todoRepository.save(todo)
    }

    @Transactional
    fun deleteTodo(id: Long): Boolean {
        if (!todoRepository.existsById(id)) return false
        todoRepository.deleteById(id)
        return true
    }

    // ===== Tag Operations =====

    fun findAllTags(): List<Tag> =
        tagRepository.findAll()

    fun findTagById(id: Long): Tag? =
        tagRepository.findById(id).orElse(null)

    fun findTagByName(name: String): Tag? =
        tagRepository.findByName(name).orElse(null)

    @Transactional
    fun createTag(name: String, color: String = "#808080"): Tag {
        val tag = Tag(name = name, color = color)
        return tagRepository.save(tag)
    }

    @Transactional
    fun getOrCreateTag(name: String, color: String = "#808080"): Tag {
        return tagRepository.findByName(name).orElseGet {
            tagRepository.save(Tag(name = name, color = color))
        }
    }

    @Transactional
    fun addTagToTodo(todoId: Long, tagName: String): Todo? {
        val todo = todoRepository.findById(todoId).orElse(null) ?: return null
        val tag = getOrCreateTag(tagName)
        todo.addTag(tag)
        return todoRepository.save(todo)
    }

    @Transactional
    fun removeTagFromTodo(todoId: Long, tagName: String): Todo? {
        val todo = todoRepository.findById(todoId).orElse(null) ?: return null
        val tag = tagRepository.findByName(tagName).orElse(null) ?: return todo
        todo.removeTag(tag)
        return todoRepository.save(todo)
    }

    // ===== Statistics =====

    fun getStats(email: String): TodoStats {
        val todos = todoRepository.findByOwnerEmail(email)
        val total = todos.size
        val pending = todos.count { it.status == TodoStatus.PENDING }
        val inProgress = todos.count { it.status == TodoStatus.IN_PROGRESS }
        val completed = todos.count { it.status == TodoStatus.COMPLETED }
        val cancelled = todos.count { it.status == TodoStatus.CANCELLED }
        val overdue = todos.count { it.isOverdue() }

        return TodoStats(
            total = total,
            pending = pending,
            inProgress = inProgress,
            completed = completed,
            cancelled = cancelled,
            overdue = overdue
        )
    }
}

data class TodoStats(
    val total: Int,
    val pending: Int,
    val inProgress: Int,
    val completed: Int,
    val cancelled: Int,
    val overdue: Int
)
