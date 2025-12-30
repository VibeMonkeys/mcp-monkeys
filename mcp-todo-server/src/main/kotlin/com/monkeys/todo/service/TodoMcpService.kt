package com.monkeys.todo.service

import com.monkeys.todo.entity.*
import com.monkeys.shared.util.ValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * Todo MCP Tool Provider
 * 할일 관리 시스템의 MCP 도구들을 제공합니다.
 */
@Service
@Transactional(readOnly = true)
class TodoMcpService(
    private val todoService: TodoService
) {
    private val logger = LoggerFactory.getLogger(TodoMcpService::class.java)

    // ===== 할일 목록(List) 관리 =====

    @Tool(
        name = "createTodoList",
        description = "새로운 할일 목록을 생성합니다."
    )
    @Transactional
    fun createTodoList(
        @ToolParam(description = "목록 이름", required = true)
        name: String,
        @ToolParam(description = "목록 설명")
        description: String? = null,
        @ToolParam(description = "소유자 이메일", required = true)
        ownerEmail: String
    ): TodoListInfo {
        val validatedName = ValidationUtils.requireNotBlank(name, "목록 이름")
        val validatedEmail = ValidationUtils.validateEmail(ownerEmail, "소유자 이메일")
        logger.info("MCP Tool 호출: createTodoList - name=$validatedName, owner=$validatedEmail")
        val todoList = todoService.createList(validatedName, description, validatedEmail)
        return todoList.toInfo()
    }

    @Tool(
        name = "getTodoLists",
        description = "특정 사용자의 할일 목록들을 조회합니다."
    )
    fun getTodoLists(
        @ToolParam(description = "소유자 이메일", required = true)
        ownerEmail: String
    ): List<TodoListInfo> {
        val validatedEmail = ValidationUtils.validateEmail(ownerEmail, "소유자 이메일")
        logger.info("MCP Tool 호출: getTodoLists - owner=$validatedEmail")
        return todoService.findListsByOwner(validatedEmail).map { it.toInfo() }
    }

    @Tool(
        name = "getTodoListDetail",
        description = "할일 목록의 상세 정보와 포함된 할일들을 조회합니다."
    )
    fun getTodoListDetail(
        @ToolParam(description = "목록 ID", required = true)
        listId: Long
    ): TodoListDetailInfo? {
        logger.info("MCP Tool 호출: getTodoListDetail - listId=$listId")
        val todoList = todoService.findListById(listId) ?: return null
        return todoList.toDetailInfo()
    }

    // ===== 할일(Todo) 관리 =====

    @Tool(
        name = "createTodo",
        description = "새로운 할일을 생성합니다."
    )
    @Transactional
    fun createTodo(
        @ToolParam(description = "할일 목록 ID", required = true)
        listId: Long,
        @ToolParam(description = "할일 제목", required = true)
        title: String,
        @ToolParam(description = "할일 상세 설명")
        description: String? = null,
        @ToolParam(description = "우선순위 (LOW, MEDIUM, HIGH, URGENT). 기본값: MEDIUM")
        priority: String = "MEDIUM",
        @ToolParam(description = "마감일 (YYYY-MM-DD 형식)")
        dueDate: String? = null
    ): TodoResult {
        ValidationUtils.requirePositive(listId, "목록 ID")
        val validatedTitle = ValidationUtils.requireNotBlank(title, "할일 제목")
        logger.info("MCP Tool 호출: createTodo - listId=$listId, title=$validatedTitle")

        val priorityEnum = try {
            Priority.valueOf(priority.uppercase())
        } catch (e: Exception) {
            Priority.MEDIUM
        }

        val dueDateParsed = dueDate?.let {
            try { LocalDate.parse(it) } catch (e: Exception) { null }
        }

        val todo = todoService.createTodo(listId, validatedTitle, description, priorityEnum, dueDateParsed)
        return if (todo != null) {
            TodoResult(
                success = true,
                message = "할일이 생성되었습니다.",
                todo = todo.toInfo()
            )
        } else {
            TodoResult(
                success = false,
                message = "할일 목록을 찾을 수 없습니다.",
                todo = null
            )
        }
    }

    @Tool(
        name = "getTodosByList",
        description = "특정 목록의 할일들을 조회합니다."
    )
    fun getTodosByList(
        @ToolParam(description = "목록 ID", required = true)
        listId: Long
    ): List<TodoInfo> {
        logger.info("MCP Tool 호출: getTodosByList - listId=$listId")
        return todoService.findTodosByList(listId).map { it.toInfo() }
    }

    @Tool(
        name = "getMyTodos",
        description = "사용자의 모든 할일을 조회합니다."
    )
    fun getMyTodos(
        @ToolParam(description = "소유자 이메일", required = true)
        ownerEmail: String,
        @ToolParam(description = "상태 필터 (PENDING, IN_PROGRESS, COMPLETED, CANCELLED). 비워두면 전체 조회")
        status: String? = null
    ): List<TodoInfo> {
        logger.info("MCP Tool 호출: getMyTodos - owner=$ownerEmail, status=$status")

        val todos = if (status != null) {
            try {
                val statusEnum = TodoStatus.valueOf(status.uppercase())
                todoService.findTodosByOwnerAndStatus(ownerEmail, statusEnum)
            } catch (e: Exception) {
                todoService.findTodosByOwner(ownerEmail)
            }
        } else {
            todoService.findTodosByOwner(ownerEmail)
        }

        return todos.map { it.toInfo() }
    }

    @Tool(
        name = "searchTodos",
        description = "제목으로 할일을 검색합니다."
    )
    fun searchTodos(
        @ToolParam(description = "검색 키워드", required = true)
        keyword: String
    ): List<TodoInfo> {
        logger.info("MCP Tool 호출: searchTodos - keyword=$keyword")
        return todoService.searchTodos(keyword).map { it.toInfo() }
    }

    @Tool(
        name = "startTodo",
        description = "할일을 진행 중 상태로 변경합니다."
    )
    @Transactional
    fun startTodo(
        @ToolParam(description = "할일 ID", required = true)
        todoId: Long
    ): TodoResult {
        logger.info("MCP Tool 호출: startTodo - todoId=$todoId")

        val todo = todoService.startTodo(todoId)
        return if (todo != null) {
            TodoResult(
                success = true,
                message = "할일을 시작했습니다.",
                todo = todo.toInfo()
            )
        } else {
            TodoResult(
                success = false,
                message = "할일을 찾을 수 없습니다.",
                todo = null
            )
        }
    }

    @Tool(
        name = "completeTodo",
        description = "할일을 완료 처리합니다."
    )
    @Transactional
    fun completeTodo(
        @ToolParam(description = "할일 ID", required = true)
        todoId: Long
    ): TodoResult {
        logger.info("MCP Tool 호출: completeTodo - todoId=$todoId")

        val todo = todoService.completeTodo(todoId)
        return if (todo != null) {
            TodoResult(
                success = true,
                message = "할일을 완료했습니다.",
                todo = todo.toInfo()
            )
        } else {
            TodoResult(
                success = false,
                message = "할일을 찾을 수 없습니다.",
                todo = null
            )
        }
    }

    @Tool(
        name = "cancelTodo",
        description = "할일을 취소합니다."
    )
    @Transactional
    fun cancelTodo(
        @ToolParam(description = "할일 ID", required = true)
        todoId: Long
    ): TodoResult {
        logger.info("MCP Tool 호출: cancelTodo - todoId=$todoId")

        val todo = todoService.cancelTodo(todoId)
        return if (todo != null) {
            TodoResult(
                success = true,
                message = "할일을 취소했습니다.",
                todo = todo.toInfo()
            )
        } else {
            TodoResult(
                success = false,
                message = "할일을 찾을 수 없습니다.",
                todo = null
            )
        }
    }

    @Tool(
        name = "reopenTodo",
        description = "완료되거나 취소된 할일을 다시 엽니다."
    )
    @Transactional
    fun reopenTodo(
        @ToolParam(description = "할일 ID", required = true)
        todoId: Long
    ): TodoResult {
        logger.info("MCP Tool 호출: reopenTodo - todoId=$todoId")

        val todo = todoService.reopenTodo(todoId)
        return if (todo != null) {
            TodoResult(
                success = true,
                message = "할일을 다시 열었습니다.",
                todo = todo.toInfo()
            )
        } else {
            TodoResult(
                success = false,
                message = "할일을 찾을 수 없습니다.",
                todo = null
            )
        }
    }

    @Tool(
        name = "getOverdueTodos",
        description = "연체된 할일 목록을 조회합니다."
    )
    fun getOverdueTodos(): List<TodoInfo> {
        logger.info("MCP Tool 호출: getOverdueTodos")
        return todoService.findOverdueTodos().map { it.toInfo() }
    }

    // ===== 태그 관리 =====

    @Tool(
        name = "addTagToTodo",
        description = "할일에 태그를 추가합니다."
    )
    @Transactional
    fun addTagToTodo(
        @ToolParam(description = "할일 ID", required = true)
        todoId: Long,
        @ToolParam(description = "태그 이름", required = true)
        tagName: String
    ): TodoResult {
        logger.info("MCP Tool 호출: addTagToTodo - todoId=$todoId, tagName=$tagName")

        val todo = todoService.addTagToTodo(todoId, tagName)
        return if (todo != null) {
            TodoResult(
                success = true,
                message = "태그를 추가했습니다.",
                todo = todo.toInfo()
            )
        } else {
            TodoResult(
                success = false,
                message = "할일을 찾을 수 없습니다.",
                todo = null
            )
        }
    }

    @Tool(
        name = "getTodosByTag",
        description = "특정 태그가 붙은 할일들을 조회합니다."
    )
    fun getTodosByTag(
        @ToolParam(description = "태그 이름", required = true)
        tagName: String
    ): List<TodoInfo> {
        logger.info("MCP Tool 호출: getTodosByTag - tagName=$tagName")
        return todoService.findTodosByTag(tagName).map { it.toInfo() }
    }

    @Tool(
        name = "getAllTags",
        description = "모든 태그 목록을 조회합니다."
    )
    fun getAllTags(): List<TagInfo> {
        logger.info("MCP Tool 호출: getAllTags")
        return todoService.findAllTags().map { it.toInfo() }
    }

    // ===== 통계 =====

    @Tool(
        name = "getTodoStats",
        description = "사용자의 할일 통계를 조회합니다."
    )
    fun getTodoStats(
        @ToolParam(description = "소유자 이메일", required = true)
        ownerEmail: String
    ): TodoStats {
        logger.info("MCP Tool 호출: getTodoStats - owner=$ownerEmail")
        return todoService.getStats(ownerEmail)
    }
}

// ===== DTO =====

data class TodoListInfo(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerEmail: String,
    val todoCount: Int,
    val completedCount: Int,
    val pendingCount: Int,
    val createdAt: String
)

data class TodoListDetailInfo(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerEmail: String,
    val todos: List<TodoInfo>,
    val createdAt: String
)

data class TodoInfo(
    val id: Long,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val dueDate: String?,
    val completedAt: String?,
    val tags: List<String>,
    val isOverdue: Boolean,
    val createdAt: String
)

data class TagInfo(
    val id: Long,
    val name: String,
    val color: String,
    val todoCount: Int
)

data class TodoResult(
    val success: Boolean,
    val message: String,
    val todo: TodoInfo?
)

// ===== Extension Functions =====

private fun TodoList.toInfo() = TodoListInfo(
    id = id,
    name = name,
    description = description,
    ownerEmail = ownerEmail,
    todoCount = todos.size,
    completedCount = getCompletedCount(),
    pendingCount = getPendingCount(),
    createdAt = createdAt.toString()
)

private fun TodoList.toDetailInfo() = TodoListDetailInfo(
    id = id,
    name = name,
    description = description,
    ownerEmail = ownerEmail,
    todos = todos.map { it.toInfo() },
    createdAt = createdAt.toString()
)

private fun Todo.toInfo() = TodoInfo(
    id = id,
    title = title,
    description = description,
    status = status.name,
    priority = priority.name,
    dueDate = dueDate?.toString(),
    completedAt = completedAt?.toString(),
    tags = tags.map { it.name },
    isOverdue = isOverdue(),
    createdAt = createdAt.toString()
)

private fun Tag.toInfo() = TagInfo(
    id = id,
    name = name,
    color = color,
    todoCount = todos.size
)
