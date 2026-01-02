package com.monkeys.todo.adapter.outbound.persistence.repository

import com.monkeys.todo.adapter.outbound.persistence.entity.TodoEntity
import com.monkeys.todo.domain.model.TodoStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TodoJpaRepository : JpaRepository<TodoEntity, Long> {
    fun findByTodoListId(listId: Long): List<TodoEntity>

    @Query("SELECT t FROM TodoEntity t WHERE t.todoList.ownerEmail = :ownerEmail")
    fun findByOwnerEmail(@Param("ownerEmail") ownerEmail: String): List<TodoEntity>

    @Query("SELECT t FROM TodoEntity t WHERE t.todoList.ownerEmail = :ownerEmail AND t.status = :status")
    fun findByOwnerEmailAndStatus(
        @Param("ownerEmail") ownerEmail: String,
        @Param("status") status: TodoStatus
    ): List<TodoEntity>

    fun findByTitleContaining(keyword: String): List<TodoEntity>

    @Query("SELECT t FROM TodoEntity t WHERE t.dueDate < :today AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    fun findOverdue(@Param("today") today: LocalDate): List<TodoEntity>

    @Query("SELECT t FROM TodoEntity t JOIN t.tags tag WHERE tag.name = :tagName")
    fun findByTagName(@Param("tagName") tagName: String): List<TodoEntity>

    @Query("SELECT COUNT(t) FROM TodoEntity t WHERE t.todoList.ownerEmail = :ownerEmail")
    fun countByOwnerEmail(@Param("ownerEmail") ownerEmail: String): Int

    @Query("SELECT COUNT(t) FROM TodoEntity t WHERE t.todoList.ownerEmail = :ownerEmail AND t.status = :status")
    fun countByOwnerEmailAndStatus(
        @Param("ownerEmail") ownerEmail: String,
        @Param("status") status: TodoStatus
    ): Int

    @Query("SELECT COUNT(t) FROM TodoEntity t WHERE t.todoList.ownerEmail = :ownerEmail AND t.dueDate < :today AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    fun countOverdueByOwnerEmail(
        @Param("ownerEmail") ownerEmail: String,
        @Param("today") today: LocalDate
    ): Int
}
