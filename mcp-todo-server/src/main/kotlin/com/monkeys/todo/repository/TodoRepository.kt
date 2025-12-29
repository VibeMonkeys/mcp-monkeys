package com.monkeys.todo.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.todo.entity.Priority
import com.monkeys.todo.entity.Todo
import com.monkeys.todo.entity.TodoStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TodoRepository : JpaRepository<Todo, Long>, KotlinJdslJpqlExecutor {
    fun findByTodoListId(todoListId: Long): List<Todo>
    fun findByStatus(status: TodoStatus): List<Todo>
    fun findByPriority(priority: Priority): List<Todo>
    fun findByDueDateBefore(date: LocalDate): List<Todo>

    @Query("SELECT t FROM Todo t WHERE t.dueDate < :date AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    fun findOverdueTodos(date: LocalDate): List<Todo>

    @Query("SELECT t FROM Todo t WHERE t.todoList.ownerEmail = :email")
    fun findByOwnerEmail(email: String): List<Todo>

    @Query("SELECT t FROM Todo t WHERE t.todoList.ownerEmail = :email AND t.status = :status")
    fun findByOwnerEmailAndStatus(email: String, status: TodoStatus): List<Todo>

    fun findByTitleContainingIgnoreCase(keyword: String): List<Todo>

    @Query("SELECT t FROM Todo t JOIN t.tags tag WHERE tag.name = :tagName")
    fun findByTagName(tagName: String): List<Todo>
}
