package com.monkeys.library.application.service

import com.monkeys.library.application.port.`in`.*
import com.monkeys.library.application.port.outbound.AuthorRepository
import com.monkeys.library.application.port.outbound.BookRepository
import com.monkeys.library.application.port.outbound.LoanRepository
import com.monkeys.library.domain.model.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class LibraryService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val loanRepository: LoanRepository
) : BookUseCase, AuthorUseCase, LoanUseCase, LibraryStatsUseCase {

    // ===== BookUseCase =====

    override fun searchBooks(keyword: String): List<Book> =
        bookRepository.findByTitleContaining(keyword)

    override fun findBookByIsbn(isbn: String): Book? =
        bookRepository.findByIsbn(isbn)

    override fun findBookById(id: Long): Book? =
        bookRepository.findById(id)

    override fun findBooksByCategory(category: String): List<Book> =
        bookRepository.findByCategory(category)

    override fun findBooksByAuthor(authorName: String): List<Book> =
        bookRepository.findByAuthorNameContaining(authorName)

    override fun findAvailableBooks(): List<Book> =
        bookRepository.findByStatus(BookStatus.AVAILABLE)

    override fun getAllBooks(): List<Book> =
        bookRepository.findAll()

    // ===== AuthorUseCase =====

    override fun searchAuthors(name: String): List<Author> =
        authorRepository.findByNameContaining(name)

    override fun findAuthorById(id: Long): Author? =
        authorRepository.findById(id)

    override fun getAllAuthors(): List<Author> =
        authorRepository.findAll()

    // ===== LoanUseCase =====

    @Transactional
    override fun borrowBook(bookId: Long, borrowerName: String, borrowerEmail: String): Loan? {
        val book = bookRepository.findById(bookId) ?: return null
        if (!book.isAvailable()) return null

        val updatedBook = book.borrow()
        bookRepository.save(updatedBook)

        val loan = Loan(
            book = updatedBook,
            borrowerName = borrowerName,
            borrowerEmail = borrowerEmail,
            loanDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(14)
        )
        return loanRepository.save(loan)
    }

    @Transactional
    override fun returnBook(loanId: Long): Loan? {
        val loan = loanRepository.findById(loanId) ?: return null
        if (loan.status != LoanStatus.ACTIVE) return null

        val returnedLoan = loan.returnBook()
        val updatedBook = loan.book.returnBook()
        bookRepository.save(updatedBook)

        return loanRepository.save(returnedLoan)
    }

    @Transactional
    override fun extendLoan(loanId: Long, days: Int): Loan? {
        val loan = loanRepository.findById(loanId) ?: return null
        if (loan.status != LoanStatus.ACTIVE) return null

        val extendedLoan = loan.extend(days)
        return loanRepository.save(extendedLoan)
    }

    override fun findLoansByBorrower(email: String): List<Loan> =
        loanRepository.findByBorrowerEmail(email)

    override fun findActiveLoans(): List<Loan> =
        loanRepository.findByStatus(LoanStatus.ACTIVE)

    override fun findOverdueLoans(): List<Loan> =
        loanRepository.findByDueDateBeforeAndStatus(LocalDate.now(), LoanStatus.ACTIVE)

    override fun findLoanById(id: Long): Loan? =
        loanRepository.findById(id)

    // ===== LibraryStatsUseCase =====

    override fun getLibraryStats(): LibraryStats {
        val totalBooks = bookRepository.count()
        val availableBooks = bookRepository.countByStatus(BookStatus.AVAILABLE)
        val activeLoans = loanRepository.countByStatus(LoanStatus.ACTIVE)
        val overdueLoans = loanRepository.countByDueDateBeforeAndStatus(
            LocalDate.now(),
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
