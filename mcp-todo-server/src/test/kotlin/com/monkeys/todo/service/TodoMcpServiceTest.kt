package com.monkeys.todo.service

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class TodoMcpServiceTest : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)
    override fun isolationMode() = IsolationMode.InstancePerTest

    @Autowired
    private lateinit var todoMcpService: TodoMcpService

    private val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    init {
        describe("MCP Tool - createTodoList") {
            it("새로운 할일 목록을 생성한다") {
                // when
                val result = todoMcpService.createTodoList(
                    name = "MCP 테스트 목록",
                    description = "MCP를 통해 생성된 목록",
                    ownerEmail = "mcp-test@example.com"
                )

                // then
                result.name shouldBe "MCP 테스트 목록"
                result.ownerEmail shouldBe "mcp-test@example.com"
            }
        }

        describe("MCP Tool - getTodoLists") {
            it("사용자의 할일 목록들을 조회한다") {
                // when
                val result = todoMcpService.getTodoLists("kim@example.com")

                // then
                result.shouldNotBeEmpty()
                result.forEach { it.ownerEmail shouldBe "kim@example.com" }
            }
        }

        describe("MCP Tool - createTodo") {
            it("새로운 할일을 생성한다") {
                // given
                val lists = todoMcpService.getTodoLists("kim@example.com")
                val listId = lists.first().id

                // when
                val result = todoMcpService.createTodo(
                    listId = listId,
                    title = "MCP 테스트 할일",
                    description = "설명",
                    priority = "HIGH",
                    dueDate = null
                )

                // then
                result.success shouldBe true
                result.todo.shouldNotBeNull()
                result.todo!!.title shouldBe "MCP 테스트 할일"
                result.todo!!.priority shouldBe "HIGH"
            }

            it("존재하지 않는 목록에는 생성 실패한다") {
                // when
                val result = todoMcpService.createTodo(
                    listId = 99999L,
                    title = "테스트",
                    description = null,
                    priority = "MEDIUM",
                    dueDate = null
                )

                // then
                result.success shouldBe false
                result.todo.shouldBeNull()
            }
        }

        describe("MCP Tool - getMyTodos") {
            it("사용자의 할일들을 조회한다") {
                // when
                val result = todoMcpService.getMyTodos("kim@example.com", null)

                // then
                result.shouldNotBeEmpty()
            }

            it("상태로 필터링하여 조회한다") {
                // when
                val result = todoMcpService.getMyTodos("kim@example.com", "PENDING")

                // then
                result.forEach { it.status shouldBe "PENDING" }
            }
        }

        describe("MCP Tool - searchTodos") {
            it("제목으로 할일을 검색한다") {
                // when
                val result = todoMcpService.searchTodos("이메일")

                // then
                result.shouldNotBeEmpty()
                result.forEach {
                    it.title.lowercase().contains("이메일") shouldBe true
                }
            }
        }

        describe("MCP Tool - completeTodo") {
            it("할일을 완료 처리한다") {
                // given
                val lists = todoMcpService.getTodoLists("kim@example.com")
                val todos = todoMcpService.getTodosByList(lists.first().id)
                val pendingTodo = todos.firstOrNull { it.status == "PENDING" || it.status == "IN_PROGRESS" }

                if (pendingTodo != null) {
                    // when
                    val result = todoMcpService.completeTodo(pendingTodo.id)

                    // then
                    result.success shouldBe true
                    result.todo.shouldNotBeNull()
                    result.todo!!.status shouldBe "COMPLETED"
                }
            }
        }

        describe("MCP Tool - addTagToTodo") {
            it("할일에 태그를 추가한다") {
                // given
                val lists = todoMcpService.getTodoLists("kim@example.com")
                val todos = todoMcpService.getTodosByList(lists.first().id)
                val todo = todos.first()

                // when
                val result = todoMcpService.addTagToTodo(todo.id, "MCP태그")

                // then
                result.success shouldBe true
                result.todo.shouldNotBeNull()
                result.todo!!.tags.contains("MCP태그") shouldBe true
            }
        }

        describe("MCP Tool - getTodosByTag") {
            it("태그로 할일을 조회한다") {
                // when
                val result = todoMcpService.getTodosByTag("업무")

                // then
                result.shouldNotBeEmpty()
            }
        }

        describe("MCP Tool - getAllTags") {
            it("모든 태그를 조회한다") {
                // when
                val result = todoMcpService.getAllTags()

                // then
                result.shouldNotBeEmpty()
            }
        }

        describe("MCP Tool - getTodoStats") {
            it("사용자의 할일 통계를 조회한다") {
                // when
                val result = todoMcpService.getTodoStats("kim@example.com")

                // then
                result.total shouldBeGreaterThan 0
            }
        }

        describe("MCP Tool - getOverdueTodos") {
            it("연체된 할일을 조회한다") {
                // when
                val result = todoMcpService.getOverdueTodos()

                // then
                result.forEach { it.isOverdue shouldBe true }
            }
        }
    }
}
