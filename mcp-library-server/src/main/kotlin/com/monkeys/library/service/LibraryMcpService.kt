package com.monkeys.library.service

import com.monkeys.library.entity.*
import com.monkeys.shared.util.ValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Library MCP Tool Provider
 * 도서관 시스템의 MCP 도구들을 제공합니다.
 *
 * Spring AI 2.0의 @Tool, @ToolParam 어노테이션을 사용합니다.
 * MCP 프로토콜을 통해 AI가 도서 검색, 대출, 반납 등의 작업을 수행할 수 있습니다.
 */
@Service
@Transactional(readOnly = true)
class LibraryMcpService(
    private val libraryService: LibraryService
) {
    private val logger = LoggerFactory.getLogger(LibraryMcpService::class.java)

    // ===== 도서 검색 도구 =====

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
        return libraryService.searchBooks(validatedKeyword).map { it.toInfo() }
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
        return libraryService.findBookByIsbn(isbn)?.toInfo()
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
        return libraryService.findBooksByCategory(category).map { it.toInfo() }
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
        return libraryService.findBooksByAuthor(authorName).map { it.toInfo() }
    }

    @Tool(
        name = "getAvailableBooks",
        description = "현재 대출 가능한 도서 목록을 조회합니다."
    )
    fun getAvailableBooks(): List<BookInfo> {
        logger.info("MCP Tool 호출: getAvailableBooks")
        return libraryService.findAvailableBooks().map { it.toInfo() }
    }

    // ===== 저자 검색 도구 =====

    @Tool(
        name = "searchAuthors",
        description = "저자를 검색합니다."
    )
    fun searchAuthors(
        @ToolParam(description = "검색할 저자 이름", required = true)
        name: String
    ): List<AuthorInfo> {
        logger.info("MCP Tool 호출: searchAuthors - name=$name")
        return libraryService.searchAuthors(name).map { it.toInfo() }
    }

    // ===== 대출 관리 도구 =====

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

        val loan = libraryService.borrowBook(bookId, validatedName, validatedEmail)
        return if (loan != null) {
            LoanResult(
                success = true,
                message = "도서 대출이 완료되었습니다.",
                loanInfo = loan.toInfo()
            )
        } else {
            val book = libraryService.findBookById(bookId)
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

        val loan = libraryService.returnBook(loanId)
        return if (loan != null) {
            LoanResult(
                success = true,
                message = "도서 반납이 완료되었습니다.",
                loanInfo = loan.toInfo()
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

        val loan = libraryService.extendLoan(loanId, days)
        return if (loan != null) {
            LoanResult(
                success = true,
                message = "대출 기간이 ${days}일 연장되었습니다. 새로운 반납일: ${loan.dueDate}",
                loanInfo = loan.toInfo()
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
        return libraryService.findLoansByBorrower(validatedEmail).map { it.toInfo() }
    }

    @Tool(
        name = "getOverdueLoans",
        description = "연체된 대출 목록을 조회합니다."
    )
    fun getOverdueLoans(): List<LoanInfo> {
        logger.info("MCP Tool 호출: getOverdueLoans")
        return libraryService.findOverdueLoans().map { it.toInfo() }
    }

    // ===== 통계 도구 =====

    @Tool(
        name = "getLibraryStats",
        description = "도서관 통계 정보를 조회합니다 (총 도서 수, 대출 현황 등)."
    )
    fun getLibraryStats(): LibraryStats {
        logger.info("MCP Tool 호출: getLibraryStats")
        return libraryService.getLibraryStats()
    }
}

// ===== DTO =====

data class BookInfo(
    val id: Long,
    val title: String,
    val isbn: String,
    val authorName: String,
    val publisher: String?,
    val category: String?,
    val description: String?,
    val availableCopies: Int,
    val totalCopies: Int,
    val status: String
)

data class AuthorInfo(
    val id: Long,
    val name: String,
    val nationality: String?,
    val biography: String?,
    val bookCount: Int
)

data class LoanInfo(
    val id: Long,
    val bookTitle: String,
    val bookIsbn: String,
    val borrowerName: String,
    val borrowerEmail: String,
    val loanDate: String,
    val dueDate: String,
    val returnDate: String?,
    val status: String,
    val isOverdue: Boolean
)

data class LoanResult(
    val success: Boolean,
    val message: String,
    val loanInfo: LoanInfo?
)

// ===== Extension Functions =====

private fun Book.toInfo() = BookInfo(
    id = id,
    title = title,
    isbn = isbn,
    authorName = author.name,
    publisher = publisher,
    category = category,
    description = description,
    availableCopies = availableCopies,
    totalCopies = totalCopies,
    status = status.name
)

private fun Author.toInfo() = AuthorInfo(
    id = id,
    name = name,
    nationality = nationality,
    biography = biography,
    bookCount = books.size
)

private fun Loan.toInfo() = LoanInfo(
    id = id,
    bookTitle = book.title,
    bookIsbn = book.isbn,
    borrowerName = borrowerName,
    borrowerEmail = borrowerEmail,
    loanDate = loanDate.toString(),
    dueDate = dueDate.toString(),
    returnDate = returnDate?.toString(),
    status = status.name,
    isOverdue = isOverdue()
)
