package com.monkeys.library.adapter.`in`.mcp

import com.monkeys.library.adapter.`in`.mcp.dto.*
import com.monkeys.library.application.port.`in`.*
import com.monkeys.library.domain.model.LibraryStats
import com.monkeys.shared.util.ValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class LibraryMcpAdapter(
    private val bookUseCase: BookUseCase,
    private val authorUseCase: AuthorUseCase,
    private val loanUseCase: LoanUseCase,
    private val libraryStatsUseCase: LibraryStatsUseCase
) {
    private val logger = LoggerFactory.getLogger(LibraryMcpAdapter::class.java)

    // ===== Book Tools =====

    @Tool(
        name = "searchBooks",
        description = "도서를 검색합니다. 제목에 키워드가 포함된 도서 목록을 반환합니다."
    )
    fun searchBooks(
        @ToolParam(description = "검색할 키워드 (도서 제목)", required = true)
        keyword: String
    ): List<BookInfo> {
        val validatedKeyword = ValidationUtils.requireNotBlank(keyword, "검색 키워드")
        logger.info("MCP Tool 호출: searchBooks - keyword=$validatedKeyword")
        return bookUseCase.searchBooks(validatedKeyword).map { BookInfo.from(it) }
    }

    @Tool(
        name = "findBookByIsbn",
        description = "ISBN으로 도서를 조회합니다."
    )
    fun findBookByIsbn(
        @ToolParam(description = "도서의 ISBN 번호", required = true)
        isbn: String
    ): BookInfo? {
        logger.info("MCP Tool 호출: findBookByIsbn - isbn=$isbn")
        return bookUseCase.findBookByIsbn(isbn)?.let { BookInfo.from(it) }
    }

    @Tool(
        name = "findBooksByCategory",
        description = "카테고리별로 도서를 조회합니다."
    )
    fun findBooksByCategory(
        @ToolParam(description = "도서 카테고리 (예: 소설, 과학, 역사, 프로그래밍 등)", required = true)
        category: String
    ): List<BookInfo> {
        logger.info("MCP Tool 호출: findBooksByCategory - category=$category")
        return bookUseCase.findBooksByCategory(category).map { BookInfo.from(it) }
    }

    @Tool(
        name = "findBooksByAuthor",
        description = "저자별로 도서를 조회합니다."
    )
    fun findBooksByAuthor(
        @ToolParam(description = "저자 이름", required = true)
        authorName: String
    ): List<BookInfo> {
        logger.info("MCP Tool 호출: findBooksByAuthor - authorName=$authorName")
        return bookUseCase.findBooksByAuthor(authorName).map { BookInfo.from(it) }
    }

    @Tool(
        name = "getAvailableBooks",
        description = "현재 대출 가능한 도서 목록을 조회합니다."
    )
    fun getAvailableBooks(): List<BookInfo> {
        logger.info("MCP Tool 호출: getAvailableBooks")
        return bookUseCase.findAvailableBooks().map { BookInfo.from(it) }
    }

    // ===== Author Tools =====

    @Tool(
        name = "searchAuthors",
        description = "저자를 검색합니다."
    )
    fun searchAuthors(
        @ToolParam(description = "검색할 저자 이름", required = true)
        name: String
    ): List<AuthorInfo> {
        logger.info("MCP Tool 호출: searchAuthors - name=$name")
        return authorUseCase.searchAuthors(name).map { AuthorInfo.from(it) }
    }

    // ===== Loan Tools =====

    @Tool(
        name = "borrowBook",
        description = "도서를 대출합니다. 대출 가능한 경우에만 성공합니다."
    )
    fun borrowBook(
        @ToolParam(description = "대출할 도서 ID", required = true)
        bookId: Long,
        @ToolParam(description = "대출자 이름", required = true)
        borrowerName: String,
        @ToolParam(description = "대출자 이메일", required = true)
        borrowerEmail: String
    ): LoanResult {
        ValidationUtils.requirePositive(bookId, "도서 ID")
        val validatedName = ValidationUtils.requireNotBlank(borrowerName, "대출자 이름")
        val validatedEmail = ValidationUtils.validateEmail(borrowerEmail, "대출자 이메일")
        logger.info("MCP Tool 호출: borrowBook - bookId=$bookId, borrower=$validatedName")

        val loan = loanUseCase.borrowBook(bookId, validatedName, validatedEmail)
        return if (loan != null) {
            LoanResult(
                success = true,
                message = "도서 대출이 완료되었습니다.",
                loanInfo = LoanInfo.from(loan)
            )
        } else {
            val book = bookUseCase.findBookById(bookId)
            LoanResult(
                success = false,
                message = if (book == null) "도서를 찾을 수 없습니다." else "현재 대출 가능한 도서가 없습니다.",
                loanInfo = null
            )
        }
    }

    @Tool(
        name = "returnBook",
        description = "대출한 도서를 반납합니다."
    )
    fun returnBook(
        @ToolParam(description = "반납할 대출 ID", required = true)
        loanId: Long
    ): LoanResult {
        logger.info("MCP Tool 호출: returnBook - loanId=$loanId")

        val loan = loanUseCase.returnBook(loanId)
        return if (loan != null) {
            LoanResult(
                success = true,
                message = "도서 반납이 완료되었습니다.",
                loanInfo = LoanInfo.from(loan)
            )
        } else {
            LoanResult(
                success = false,
                message = "반납할 수 없는 대출입니다. 대출 ID를 확인해주세요.",
                loanInfo = null
            )
        }
    }

    @Tool(
        name = "extendLoan",
        description = "대출 기간을 연장합니다."
    )
    fun extendLoan(
        @ToolParam(description = "연장할 대출 ID", required = true)
        loanId: Long,
        @ToolParam(description = "연장할 일수 (기본값: 7일)")
        days: Int = 7
    ): LoanResult {
        logger.info("MCP Tool 호출: extendLoan - loanId=$loanId, days=$days")

        val loan = loanUseCase.extendLoan(loanId, days)
        return if (loan != null) {
            LoanResult(
                success = true,
                message = "대출 기간이 ${days}일 연장되었습니다. 새로운 반납일: ${loan.dueDate}",
                loanInfo = LoanInfo.from(loan)
            )
        } else {
            LoanResult(
                success = false,
                message = "연장할 수 없는 대출입니다.",
                loanInfo = null
            )
        }
    }

    @Tool(
        name = "getMyLoans",
        description = "특정 이메일로 대출한 도서 목록을 조회합니다."
    )
    fun getMyLoans(
        @ToolParam(description = "대출자 이메일", required = true)
        email: String
    ): List<LoanInfo> {
        val validatedEmail = ValidationUtils.validateEmail(email, "대출자 이메일")
        logger.info("MCP Tool 호출: getMyLoans - email=$validatedEmail")
        return loanUseCase.findLoansByBorrower(validatedEmail).map { LoanInfo.from(it) }
    }

    @Tool(
        name = "getOverdueLoans",
        description = "연체된 대출 목록을 조회합니다."
    )
    fun getOverdueLoans(): List<LoanInfo> {
        logger.info("MCP Tool 호출: getOverdueLoans")
        return loanUseCase.findOverdueLoans().map { LoanInfo.from(it) }
    }

    // ===== Stats Tools =====

    @Tool(
        name = "getLibraryStats",
        description = "도서관 통계 정보를 조회합니다 (총 도서 수, 대출 현황 등)."
    )
    fun getLibraryStats(): LibraryStats {
        logger.info("MCP Tool 호출: getLibraryStats")
        return libraryStatsUseCase.getLibraryStats()
    }
}
