package com.monkeys.library.adapter.`in`.mcp.dto

import com.monkeys.library.domain.model.Loan

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
) {
    companion object {
        fun from(loan: Loan) = LoanInfo(
            id = loan.id,
            bookTitle = loan.book.title,
            bookIsbn = loan.book.isbn,
            borrowerName = loan.borrowerName,
            borrowerEmail = loan.borrowerEmail,
            loanDate = loan.loanDate.toString(),
            dueDate = loan.dueDate.toString(),
            returnDate = loan.returnDate?.toString(),
            status = loan.status.name,
            isOverdue = loan.isOverdue()
        )
    }
}

data class LoanResult(
    val success: Boolean,
    val message: String,
    val loanInfo: LoanInfo?
)
