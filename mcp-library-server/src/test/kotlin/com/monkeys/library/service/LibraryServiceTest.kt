package com.monkeys.library.service

import com.monkeys.library.entity.*
import com.monkeys.library.repository.AuthorRepository
import com.monkeys.library.repository.BookRepository
import com.monkeys.library.repository.LoanRepository
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class LibraryServiceTest : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)
    override fun isolationMode() = IsolationMode.InstancePerTest

    @Autowired
    private lateinit var libraryService: LibraryService

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @Autowired
    private lateinit var loanRepository: LoanRepository

    private val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    init {
        describe("도서 검색") {
            context("키워드로 검색할 때") {
                it("제목에 키워드가 포함된 도서를 반환한다") {
                    // given - data.sql에서 이미 데이터가 있음

                    // when
                    val result = libraryService.searchBooks("코드")

                    // then
                    result.forEach { book ->
                        book.title.lowercase().contains("코드") shouldBe true
                    }
                }

                it("검색 결과가 없으면 빈 리스트를 반환한다") {
                    // when
                    val result = libraryService.searchBooks("존재하지않는도서제목xyz")

                    // then
                    result shouldHaveSize 0
                }
            }

            context("ISBN으로 검색할 때") {
                it("해당 ISBN의 도서를 반환한다") {
                    // when
                    val result = libraryService.findBookByIsbn("978-89-374-3340-1")

                    // then
                    result.shouldNotBeNull()
                    result.title shouldBe "살인자의 기억법"
                }

                it("존재하지 않는 ISBN이면 null을 반환한다") {
                    // when
                    val result = libraryService.findBookByIsbn("000-00-000-0000-0")

                    // then
                    result.shouldBeNull()
                }
            }

            context("카테고리로 검색할 때") {
                it("해당 카테고리의 도서들을 반환한다") {
                    // when
                    val result = libraryService.findBooksByCategory("프로그래밍")

                    // then
                    result.isNotEmpty() shouldBe true
                    result.forEach { book ->
                        book.category shouldBe "프로그래밍"
                    }
                }
            }
        }

        describe("도서 대출") {
            context("대출 가능한 도서를 대출할 때") {
                it("대출이 성공하고 대출 정보를 반환한다") {
                    // given
                    val availableBook = libraryService.findAvailableBooks().first()
                    val initialCopies = availableBook.availableCopies

                    // when
                    val loan = libraryService.borrowBook(
                        bookId = availableBook.id,
                        borrowerName = "테스트 사용자",
                        borrowerEmail = "test@example.com"
                    )

                    // then
                    loan.shouldNotBeNull()
                    loan.borrowerName shouldBe "테스트 사용자"
                    loan.borrowerEmail shouldBe "test@example.com"
                    loan.status shouldBe LoanStatus.ACTIVE

                    // 대출 가능 권수가 감소해야 함
                    val updatedBook = libraryService.findBookById(availableBook.id)
                    updatedBook.shouldNotBeNull()
                    updatedBook.availableCopies shouldBe initialCopies - 1
                }
            }

            context("대출 불가능한 도서를 대출할 때") {
                it("null을 반환한다") {
                    // given - 존재하지 않는 도서 ID
                    val nonExistentBookId = 99999L

                    // when
                    val loan = libraryService.borrowBook(
                        bookId = nonExistentBookId,
                        borrowerName = "테스트 사용자",
                        borrowerEmail = "test@example.com"
                    )

                    // then
                    loan.shouldBeNull()
                }
            }
        }

        describe("도서 반납") {
            context("대출 중인 도서를 반납할 때") {
                it("반납이 성공하고 상태가 RETURNED로 변경된다") {
                    // given
                    val activeLoan = libraryService.findActiveLoans().first()

                    // when
                    val returnedLoan = libraryService.returnBook(activeLoan.id)

                    // then
                    returnedLoan.shouldNotBeNull()
                    returnedLoan.status shouldBe LoanStatus.RETURNED
                    returnedLoan.returnDate.shouldNotBeNull()
                }
            }

            context("이미 반납된 대출을 다시 반납하려 할 때") {
                it("null을 반환한다") {
                    // given
                    val activeLoan = libraryService.findActiveLoans().first()
                    libraryService.returnBook(activeLoan.id) // 먼저 반납

                    // when
                    val result = libraryService.returnBook(activeLoan.id) // 다시 반납 시도

                    // then
                    result.shouldBeNull()
                }
            }
        }

        describe("대출 기간 연장") {
            context("대출 중인 도서의 기간을 연장할 때") {
                it("반납일이 연장된다") {
                    // given
                    val activeLoan = libraryService.findActiveLoans().first()
                    val originalDueDate = activeLoan.dueDate

                    // when
                    val extendedLoan = libraryService.extendLoan(activeLoan.id, 7)

                    // then
                    extendedLoan.shouldNotBeNull()
                    extendedLoan.dueDate shouldBe originalDueDate.plusDays(7)
                }
            }
        }

        describe("연체 대출 조회") {
            it("반납 기한이 지난 대출들을 반환한다") {
                // when
                val overdueLoans = libraryService.findOverdueLoans()

                // then
                overdueLoans.forEach { loan ->
                    loan.isOverdue() shouldBe true
                }
            }
        }

        describe("도서관 통계") {
            it("통계 정보를 반환한다") {
                // when
                val stats = libraryService.getLibraryStats()

                // then
                stats.totalBooks shouldBe bookRepository.count()
                stats.totalAuthors shouldBe authorRepository.count()
                stats.activeLoans shouldBe loanRepository.findByStatus(LoanStatus.ACTIVE).size
            }
        }
    }
}
