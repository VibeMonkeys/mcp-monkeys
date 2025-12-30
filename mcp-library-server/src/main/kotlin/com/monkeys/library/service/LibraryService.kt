package com.monkeys.library.service

import com.monkeys.library.entity.*
import com.monkeys.library.repository.AuthorRepository
import com.monkeys.library.repository.BookRepository
import com.monkeys.library.repository.LoanRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class LibraryService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val loanRepository: LoanRepository
) {

    // ===== Book Operations =====

    fun searchBooks(keyword: String): List<Book> =
        bookRepository.findByTitleContainingIgnoreCase(keyword)

    fun findBookByIsbn(isbn: String): Book? =
        bookRepository.findByIsbn(isbn)

    fun findBookById(id: Long): Book? =
        bookRepository.findById(id).orElse(null)

    fun findBooksByCategory(category: String): List<Book> =
        bookRepository.findByCategory(category)

    fun findBooksByAuthor(authorName: String): List<Book> =
        bookRepository.findByAuthorNameContainingIgnoreCase(authorName)

    fun findAvailableBooks(): List<Book> =
        bookRepository.findByStatus(BookStatus.AVAILABLE)

    fun getAllBooks(): List<Book> =
        bookRepository.findAll()

    // ===== Author Operations =====

    fun searchAuthors(name: String): List<Author> =
        authorRepository.findByNameContainingIgnoreCase(name)

    fun findAuthorById(id: Long): Author? =
        authorRepository.findById(id).orElse(null)

    fun getAllAuthors(): List<Author> =
        authorRepository.findAll()

    // ===== Loan Operations =====

    @Transactional
    fun borrowBook(bookId: Long, borrowerName: String, borrowerEmail: String): Loan? {
        val book = bookRepository.findById(bookId).orElse(null) ?: return null

        if (!book.isAvailable()) return null

        book.borrow()
        bookRepository.save(book)

        val loan = Loan(
            book = book,
            borrowerName = borrowerName,
            borrowerEmail = borrowerEmail,
            loanDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(14)
        )
        return loanRepository.save(loan)
    }

    @Transactional
    fun returnBook(loanId: Long): Loan? {
        val loan = loanRepository.findById(loanId).orElse(null) ?: return null

        if (loan.status != LoanStatus.ACTIVE) return null

        loan.returnBook()
        return loanRepository.save(loan)
    }

    @Transactional
    fun extendLoan(loanId: Long, days: Int = 7): Loan? {
        val loan = loanRepository.findById(loanId).orElse(null) ?: return null

        if (loan.status != LoanStatus.ACTIVE) return null

        loan.extend(days)
        return loanRepository.save(loan)
    }

    fun findLoansByBorrower(email: String): List<Loan> =
        loanRepository.findByBorrowerEmail(email)

    fun findActiveLoans(): List<Loan> =
        loanRepository.findByStatus(LoanStatus.ACTIVE)

    fun findOverdueLoans(): List<Loan> =
        loanRepository.findByDueDateBeforeAndStatus(LocalDate.now(), LoanStatus.ACTIVE)

    fun findLoanById(id: Long): Loan? =
        loanRepository.findById(id).orElse(null)

    // ===== Statistics =====

    fun getLibraryStats(): LibraryStats {
        // 최적화: count 쿼리 사용 (전체 데이터 로드 대신)
        val totalBooks = bookRepository.count()
        val availableBooks = bookRepository.countByStatus(BookStatus.AVAILABLE)
        val activeLoans = loanRepository.countByStatus(LoanStatus.ACTIVE)
        val overdueLoans = loanRepository.countByDueDateBeforeAndStatus(
            java.time.LocalDate.now(),
            LoanStatus.ACTIVE
        )
        val totalAuthors = authorRepository.count()

        return LibraryStats(
            totalBooks = totalBooks,
            availableBooks = availableBooks.toInt(),
            borrowedBooks = totalBooks - availableBooks,
            activeLoans = activeLoans.toInt(),
            overdueLoans = overdueLoans.toInt(),
            totalAuthors = totalAuthors
        )
    }
}

data class LibraryStats(
    val totalBooks: Long,
    val availableBooks: Int,
    val borrowedBooks: Long,
    val activeLoans: Int,
    val overdueLoans: Int,
    val totalAuthors: Long
)
