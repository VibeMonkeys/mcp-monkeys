package com.monkeys.library.application.port.`in`

import com.monkeys.library.domain.model.Loan

interface LoanUseCase {
    fun borrowBook(bookId: Long, borrowerName: String, borrowerEmail: String): Loan?
    fun returnBook(loanId: Long): Loan?
    fun extendLoan(loanId: Long, days: Int = 7): Loan?
    fun findLoansByBorrower(email: String): List<Loan>
    fun findActiveLoans(): List<Loan>
    fun findOverdueLoans(): List<Loan>
    fun findLoanById(id: Long): Loan?
}
