package com.monkeys.todo.adapter.`in`.mcp

import com.monkeys.todo.adapter.`in`.mcp.dto.*
import com.monkeys.todo.application.port.`in`.TagUseCase
import com.monkeys.todo.application.port.`in`.TodoListUseCase
import com.monkeys.todo.application.port.`in`.TodoStatsUseCase
import com.monkeys.todo.application.port.`in`.TodoUseCase
import com.monkeys.todo.domain.model.Priority
import com.monkeys.todo.domain.model.TodoStatus
import com.monkeys.shared.util.ValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class TodoMcpAdapter(
    private val todoListUseCase: TodoListUseCase,
    private val todoUseCase: TodoUseCase,
    private val tagUseCase: TagUseCase,
    private val todoStatsUseCase: TodoStatsUseCase
) {
    private val logger = LoggerFactory.getLogger(TodoMcpAdapter::class.java)

    // ===== TodoList Operations =====

    @Tool(name = "createTodoList", description = "새로운 할일 목록을 생성합니다.")
    fun createTodoList(
        @ToolParam(description = "목록 이름", required = true) name: String,
        @ToolParam(description = "목록 설명") description: String? = null,
        @ToolParam(description = "소유자 이메일", required = true) ownerEmail: String
    ): TodoListDto {
        val validatedName = ValidationUtils.requireNotBlank(name, "목록 이름")
        val validatedEmail = ValidationUtils.validateEmail(ownerEmail, "소유자 이메일")
        logger.info("MCP Tool: createTodoList - name=$validatedName, owner=$validatedEmail")
        val todoList = todoListUseCase.createList(validatedName, description, validatedEmail)
        return TodoListDto.fromDomain(todoList)
    }

    @Tool(name = "getTodoLists", description = "특정 사용자의 할일 목록들을 조회합니다.")
    fun getTodoLists(
        @ToolParam(description = "소유자 이메일", required = true) ownerEmail: String
    ): List<TodoListDto> {
        val validatedEmail = ValidationUtils.validateEmail(ownerEmail, "소유자 이메일")
        logger.info("MCP Tool: getTodoLists - owner=$validatedEmail")
        return todoListUseCase.findListsByOwner(validatedEmail).map { TodoListDto.fromDomain(it) }
    }

    @Tool(name = "getTodoListDetail", description = "할일 목록의 상세 정보와 포함된 할일들을 조회합니다.")
    fun getTodoListDetail(
        @ToolParam(description = "목록 ID", required = true) listId: Long
    ): TodoListDetailDto? {
        logger.info("MCP Tool: getTodoListDetail - listId=$listId")
        val todoList = todoListUseCase.findListById(listId) ?: return null
        return TodoListDetailDto.fromDomain(todoList)
    }

    // ===== Todo Operations =====

    @Tool(name = "createTodo", description = "새로운 할일을 생성합니다.")
    fun createTodo(
        @ToolParam(description = "할일 목록 ID", required = true) listId: Long,
        @ToolParam(description = "할일 제목", required = true) title: String,
        @ToolParam(description = "할일 상세 설명") description: String? = null,
        @ToolParam(description = "우선순위 (LOW, MEDIUM, HIGH, URGENT). 기본값: MEDIUM") priority: String = "MEDIUM",
        @ToolParam(description = "마감일 (YYYY-MM-DD 형식)") dueDate: String? = null
    ): TodoResultDto {
        ValidationUtils.requirePositive(listId, "목록 ID")
        val validatedTitle = ValidationUtils.requireNotBlank(title, "할일 제목")
        logger.info("MCP Tool: createTodo - listId=$listId, title=$validatedTitle")

        val priorityEnum = try {
            Priority.valueOf(priority.uppercase())
        } catch (e: Exception) {
            Priority.MEDIUM
        }

        val dueDateParsed = dueDate?.let {
            try { LocalDate.parse(it) } catch (e: Exception) { null }
        }

        val todo = todoUseCase.createTodo(listId, validatedTitle, description, priorityEnum, dueDateParsed)
        return if (todo != null) {
            TodoResultDto(success = true, message = "할일이 생성되었습니다.", todo = TodoDto.fromDomain(todo))
        } else {
            TodoResultDto(success = false, message = "할일 목록을 찾을 수 없습니다.", todo = null)
        }
    }

    @Tool(name = "getTodosByList", description = "특정 목록의 할일들을 조회합니다.")
    fun getTodosByList(
        @ToolParam(description = "목록 ID", required = true) listId: Long
    ): List<TodoDto> {
        logger.info("MCP Tool: getTodosByList - listId=$listId")
        return todoUseCase.findTodosByList(listId).map { TodoDto.fromDomain(it) }
    }

    @Tool(name = "getMyTodos", description = "사용자의 모든 할일을 조회합니다.")
    fun getMyTodos(
        @ToolParam(description = "소유자 이메일", required = true) ownerEmail: String,
        @ToolParam(description = "상태 필터 (PENDING, IN_PROGRESS, COMPLETED, CANCELLED). 비워두면 전체 조회") status: String? = null
    ): List<TodoDto> {
        logger.info("MCP Tool: getMyTodos - owner=$ownerEmail, status=$status")

        val todos = if (status != null) {
            try {
                val statusEnum = TodoStatus.valueOf(status.uppercase())
                todoUseCase.findTodosByOwnerAndStatus(ownerEmail, statusEnum)
            } catch (e: Exception) {
                todoUseCase.findTodosByOwner(ownerEmail)
            }
        } else {
            todoUseCase.findTodosByOwner(ownerEmail)
        }

        return todos.map { TodoDto.fromDomain(it) }
    }

    @Tool(name = "searchTodos", description = "제목으로 할일을 검색합니다.")
    fun searchTodos(
        @ToolParam(description = "검색 키워드", required = true) keyword: String
    ): List<TodoDto> {
        logger.info("MCP Tool: searchTodos - keyword=$keyword")
        return todoUseCase.searchTodos(keyword).map { TodoDto.fromDomain(it) }
    }

    @Tool(name = "startTodo", description = "할일을 진행 중 상태로 변경합니다.")
    fun startTodo(
        @ToolParam(description = "할일 ID", required = true) todoId: Long
    ): TodoResultDto {
        logger.info("MCP Tool: startTodo - todoId=$todoId")
        val todo = todoUseCase.startTodo(todoId)
        return if (todo != null) {
            TodoResultDto(success = true, message = "할일을 시작했습니다.", todo = TodoDto.fromDomain(todo))
        } else {
            TodoResultDto(success = false, message = "할일을 찾을 수 없습니다.", todo = null)
        }
    }

    @Tool(name = "completeTodo", description = "할일을 완료 처리합니다.")
    fun completeTodo(
        @ToolParam(description = "할일 ID", required = true) todoId: Long
    ): TodoResultDto {
        logger.info("MCP Tool: completeTodo - todoId=$todoId")
        val todo = todoUseCase.completeTodo(todoId)
        return if (todo != null) {
            TodoResultDto(success = true, message = "할일을 완료했습니다.", todo = TodoDto.fromDomain(todo))
        } else {
            TodoResultDto(success = false, message = "할일을 찾을 수 없습니다.", todo = null)
        }
    }

    @Tool(name = "cancelTodo", description = "할일을 취소합니다.")
    fun cancelTodo(
        @ToolParam(description = "할일 ID", required = true) todoId: Long
    ): TodoResultDto {
        logger.info("MCP Tool: cancelTodo - todoId=$todoId")
        val todo = todoUseCase.cancelTodo(todoId)
        return if (todo != null) {
            TodoResultDto(success = true, message = "할일을 취소했습니다.", todo = TodoDto.fromDomain(todo))
        } else {
            TodoResultDto(success = false, message = "할일을 찾을 수 없습니다.", todo = null)
        }
    }

    @Tool(name = "reopenTodo", description = "완료되거나 취소된 할일을 다시 엽니다.")
    fun reopenTodo(
        @ToolParam(description = "할일 ID", required = true) todoId: Long
    ): TodoResultDto {
        logger.info("MCP Tool: reopenTodo - todoId=$todoId")
        val todo = todoUseCase.reopenTodo(todoId)
        return if (todo != null) {
            TodoResultDto(success = true, message = "할일을 다시 열었습니다.", todo = TodoDto.fromDomain(todo))
        } else {
            TodoResultDto(success = false, message = "할일을 찾을 수 없습니다.", todo = null)
        }
    }

    @Tool(name = "getOverdueTodos", description = "연체된 할일 목록을 조회합니다.")
    fun getOverdueTodos(): List<TodoDto> {
        logger.info("MCP Tool: getOverdueTodos")
        return todoUseCase.findOverdueTodos().map { TodoDto.fromDomain(it) }
    }

    // ===== Tag Operations =====

    @Tool(name = "addTagToTodo", description = "할일에 태그를 추가합니다.")
    fun addTagToTodo(
        @ToolParam(description = "할일 ID", required = true) todoId: Long,
        @ToolParam(description = "태그 이름", required = true) tagName: String
    ): TodoResultDto {
        logger.info("MCP Tool: addTagToTodo - todoId=$todoId, tagName=$tagName")
        val todo = tagUseCase.addTagToTodo(todoId, tagName)
        return if (todo != null) {
            TodoResultDto(success = true, message = "태그를 추가했습니다.", todo = TodoDto.fromDomain(todo))
        } else {
            TodoResultDto(success = false, message = "할일을 찾을 수 없습니다.", todo = null)
        }
    }

    @Tool(name = "getTodosByTag", description = "특정 태그가 붙은 할일들을 조회합니다.")
    fun getTodosByTag(
        @ToolParam(description = "태그 이름", required = true) tagName: String
    ): List<TodoDto> {
        logger.info("MCP Tool: getTodosByTag - tagName=$tagName")
        return tagUseCase.findTodosByTag(tagName).map { TodoDto.fromDomain(it) }
    }

    @Tool(name = "getAllTags", description = "모든 태그 목록을 조회합니다.")
    fun getAllTags(): List<TagDto> {
        logger.info("MCP Tool: getAllTags")
        return tagUseCase.findAllTags().map { TagDto.fromDomain(it) }
    }

    // ===== Stats =====

    @Tool(name = "getTodoStats", description = "사용자의 할일 통계를 조회합니다.")
    fun getTodoStats(
        @ToolParam(description = "소유자 이메일", required = true) ownerEmail: String
    ): TodoStatsDto {
        logger.info("MCP Tool: getTodoStats - owner=$ownerEmail")
        return TodoStatsDto.fromDomain(todoStatsUseCase.getStats(ownerEmail))
    }
}
