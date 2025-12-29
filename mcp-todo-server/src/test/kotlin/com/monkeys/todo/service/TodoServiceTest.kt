package com.monkeys.todo.service

import com.monkeys.todo.entity.*
import com.monkeys.todo.repository.TagRepository
import com.monkeys.todo.repository.TodoListRepository
import com.monkeys.todo.repository.TodoRepository
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class TodoServiceTest : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)
    override fun isolationMode() = IsolationMode.InstancePerTest

    @Autowired
    private lateinit var todoService: TodoService

    @Autowired
    private lateinit var todoListRepository: TodoListRepository

    @Autowired
    private lateinit var todoRepository: TodoRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    private val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    init {
        describe("할일 목록 조회") {
            context("소유자 이메일로 조회할 때") {
                it("해당 사용자의 목록들을 반환한다") {
                    // when
                    val result = todoService.findListsByOwner("kim@example.com")

                    // then
                    result.shouldNotBeEmpty()
                    result.forEach { it.ownerEmail shouldBe "kim@example.com" }
                }

                it("해당 사용자의 목록이 없으면 빈 리스트를 반환한다") {
                    // when
                    val result = todoService.findListsByOwner("nobody@example.com")

                    // then
                    result shouldHaveSize 0
                }
            }
        }

        describe("할일 목록 생성") {
            it("새로운 할일 목록을 생성한다") {
                // when
                val result = todoService.createList(
                    name = "테스트 목록",
                    description = "테스트 설명",
                    ownerEmail = "test@example.com"
                )

                // then
                result.shouldNotBeNull()
                result.name shouldBe "테스트 목록"
                result.ownerEmail shouldBe "test@example.com"
            }
        }

        describe("할일 생성") {
            context("유효한 목록 ID로 생성할 때") {
                it("할일을 생성한다") {
                    // given
                    val list = todoService.findListsByOwner("kim@example.com").first()

                    // when
                    val result = todoService.createTodo(
                        listId = list.id,
                        title = "새 할일",
                        description = "설명",
                        priority = Priority.HIGH,
                        dueDate = null
                    )

                    // then
                    result.shouldNotBeNull()
                    result.title shouldBe "새 할일"
                    result.priority shouldBe Priority.HIGH
                    result.status shouldBe TodoStatus.PENDING
                }
            }

            context("존재하지 않는 목록 ID로 생성할 때") {
                it("null을 반환한다") {
                    // when
                    val result = todoService.createTodo(
                        listId = 99999L,
                        title = "새 할일",
                        description = null,
                        priority = Priority.MEDIUM,
                        dueDate = null
                    )

                    // then
                    result.shouldBeNull()
                }
            }
        }

        describe("할일 상태 변경") {
            context("할일을 시작할 때") {
                it("상태가 IN_PROGRESS로 변경된다") {
                    // given
                    val pendingTodo = todoRepository.findByStatus(TodoStatus.PENDING).first()

                    // when
                    val result = todoService.startTodo(pendingTodo.id)

                    // then
                    result.shouldNotBeNull()
                    result.status shouldBe TodoStatus.IN_PROGRESS
                }
            }

            context("할일을 완료할 때") {
                it("상태가 COMPLETED로 변경된다") {
                    // given
                    val inProgressTodo = todoRepository.findByStatus(TodoStatus.IN_PROGRESS).first()

                    // when
                    val result = todoService.completeTodo(inProgressTodo.id)

                    // then
                    result.shouldNotBeNull()
                    result.status shouldBe TodoStatus.COMPLETED
                    result.completedAt.shouldNotBeNull()
                }
            }

            context("할일을 취소할 때") {
                it("상태가 CANCELLED로 변경된다") {
                    // given
                    val pendingTodo = todoRepository.findByStatus(TodoStatus.PENDING).first()

                    // when
                    val result = todoService.cancelTodo(pendingTodo.id)

                    // then
                    result.shouldNotBeNull()
                    result.status shouldBe TodoStatus.CANCELLED
                }
            }

            context("완료된 할일을 다시 열 때") {
                it("상태가 PENDING으로 변경된다") {
                    // given
                    val completedTodo = todoRepository.findByStatus(TodoStatus.COMPLETED).first()

                    // when
                    val result = todoService.reopenTodo(completedTodo.id)

                    // then
                    result.shouldNotBeNull()
                    result.status shouldBe TodoStatus.PENDING
                    result.completedAt.shouldBeNull()
                }
            }
        }

        describe("연체된 할일 조회") {
            it("마감일이 지난 미완료 할일들을 반환한다") {
                // when
                val result = todoService.findOverdueTodos()

                // then
                result.forEach { todo ->
                    todo.isOverdue() shouldBe true
                }
            }
        }

        describe("태그 관리") {
            context("할일에 태그를 추가할 때") {
                it("태그가 추가된다") {
                    // given
                    val todo = todoRepository.findByStatus(TodoStatus.PENDING).first()

                    // when
                    val result = todoService.addTagToTodo(todo.id, "새태그")

                    // then
                    result.shouldNotBeNull()
                    // 태그가 추가되었는지 확인 - 새로 조회해서 확인
                    val tag = todoService.findTagByName("새태그")
                    tag.shouldNotBeNull()
                }
            }
        }

        describe("통계 조회") {
            it("사용자의 할일 통계를 반환한다") {
                // when
                val result = todoService.getStats("kim@example.com")

                // then
                result.total shouldBe todoRepository.findByOwnerEmail("kim@example.com").size
            }
        }
    }
}
