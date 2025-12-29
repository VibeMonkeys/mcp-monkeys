package com.monkeys.library.service

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
class LibraryMcpServiceTest : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)
    override fun isolationMode() = IsolationMode.InstancePerTest

    @Autowired
    private lateinit var libraryMcpService: LibraryMcpService

    @Autowired
    private lateinit var libraryService: LibraryService

    private val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    init {
        describe("MCP Tool - searchBooks") {
            it("키워드로 도서를 검색하여 BookInfo 목록을 반환한다") {
                // when
                val result = libraryMcpService.searchBooks("클린")

                // then
                result.shouldNotBeEmpty()
                result.forEach { bookInfo ->
                    bookInfo.title.lowercase().contains("클린") shouldBe true
                }
            }
        }

        describe("MCP Tool - findBookByIsbn") {
            it("ISBN으로 도서를 조회하여 BookInfo를 반환한다") {
                // when
                val result = libraryMcpService.findBookByIsbn("978-89-6626-083-2")

                // then
                result.shouldNotBeNull()
                result.title shouldBe "클린 코드"
                result.authorName shouldBe "로버트 C. 마틴"
            }

            it("존재하지 않는 ISBN이면 null을 반환한다") {
                // when
                val result = libraryMcpService.findBookByIsbn("000-00-000-0000-0")

                // then
                result.shouldBeNull()
            }
        }

        describe("MCP Tool - findBooksByCategory") {
            it("카테고리별 도서를 조회하여 BookInfo 목록을 반환한다") {
                // when
                val result = libraryMcpService.findBooksByCategory("소설")

                // then
                result.shouldNotBeEmpty()
                result.forEach { bookInfo ->
                    bookInfo.category shouldBe "소설"
                }
            }
        }

        describe("MCP Tool - findBooksByAuthor") {
            it("저자명으로 도서를 조회하여 BookInfo 목록을 반환한다") {
                // when
                val result = libraryMcpService.findBooksByAuthor("하루키")

                // then
                result.shouldNotBeEmpty()
                result.forEach { bookInfo ->
                    bookInfo.authorName.contains("하루키") shouldBe true
                }
            }
        }

        describe("MCP Tool - getAvailableBooks") {
            it("대출 가능한 도서 목록을 반환한다") {
                // when
                val result = libraryMcpService.getAvailableBooks()

                // then
                result.shouldNotBeEmpty()
                result.forEach { bookInfo ->
                    bookInfo.availableCopies shouldBeGreaterThan 0
                    bookInfo.status shouldBe "AVAILABLE"
                }
            }
        }

        describe("MCP Tool - searchAuthors") {
            it("저자명으로 검색하여 AuthorInfo 목록을 반환한다") {
                // when
                val result = libraryMcpService.searchAuthors("김영하")

                // then
                result.shouldNotBeEmpty()
                result.first().name shouldBe "김영하"
            }
        }

        describe("MCP Tool - borrowBook") {
            it("도서 대출 성공 시 성공 결과를 반환한다") {
                // given
                val availableBook = libraryMcpService.getAvailableBooks().first()

                // when
                val result = libraryMcpService.borrowBook(
                    bookId = availableBook.id,
                    borrowerName = "MCP 테스트 사용자",
                    borrowerEmail = "mcp-test@example.com"
                )

                // then
                result.success shouldBe true
                result.message.contains("완료") shouldBe true
                result.loanInfo.shouldNotBeNull()
                result.loanInfo!!.borrowerName shouldBe "MCP 테스트 사용자"
            }

            it("존재하지 않는 도서 대출 시 실패 결과를 반환한다") {
                // when
                val result = libraryMcpService.borrowBook(
                    bookId = 99999L,
                    borrowerName = "테스트",
                    borrowerEmail = "test@example.com"
                )

                // then
                result.success shouldBe false
                result.loanInfo.shouldBeNull()
            }
        }

        describe("MCP Tool - returnBook") {
            it("도서 반납 성공 시 성공 결과를 반환한다") {
                // given
                val availableBook = libraryMcpService.getAvailableBooks().first()
                val borrowResult = libraryMcpService.borrowBook(
                    bookId = availableBook.id,
                    borrowerName = "반납 테스트 사용자",
                    borrowerEmail = "return-test@example.com"
                )
                val loanId = borrowResult.loanInfo!!.id

                // when
                val result = libraryMcpService.returnBook(loanId)

                // then
                result.success shouldBe true
                result.message.contains("완료") shouldBe true
                result.loanInfo.shouldNotBeNull()
                result.loanInfo!!.status shouldBe "RETURNED"
            }
        }

        describe("MCP Tool - extendLoan") {
            it("대출 연장 성공 시 새로운 반납일을 포함한 결과를 반환한다") {
                // given
                val availableBook = libraryMcpService.getAvailableBooks().first()
                val borrowResult = libraryMcpService.borrowBook(
                    bookId = availableBook.id,
                    borrowerName = "연장 테스트 사용자",
                    borrowerEmail = "extend-test@example.com"
                )
                val loanId = borrowResult.loanInfo!!.id

                // when
                val result = libraryMcpService.extendLoan(loanId, 7)

                // then
                result.success shouldBe true
                result.message.contains("7일 연장") shouldBe true
            }
        }

        describe("MCP Tool - getMyLoans") {
            it("이메일로 대출 내역을 조회하여 LoanInfo 목록을 반환한다") {
                // given - data.sql에서 hong@example.com으로 대출이 있음

                // when
                val result = libraryMcpService.getMyLoans("hong@example.com")

                // then
                result.shouldNotBeEmpty()
                result.forEach { loanInfo ->
                    loanInfo.borrowerEmail shouldBe "hong@example.com"
                }
            }
        }

        describe("MCP Tool - getOverdueLoans") {
            it("연체된 대출 목록을 반환한다") {
                // when
                val result = libraryMcpService.getOverdueLoans()

                // then
                result.forEach { loanInfo ->
                    loanInfo.isOverdue shouldBe true
                }
            }
        }

        describe("MCP Tool - getLibraryStats") {
            it("도서관 통계를 반환한다") {
                // when
                val result = libraryMcpService.getLibraryStats()

                // then
                result.totalBooks shouldBeGreaterThan 0
                result.totalAuthors shouldBeGreaterThan 0
            }
        }
    }
}
