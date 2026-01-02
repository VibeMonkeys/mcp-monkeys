package com.monkeys.library.domain.model

import java.time.LocalDate

data class Loan(
    val id: Long = 0,
    val book: Book,
    val borrowerName: String,
    val borrowerEmail: String,
    val loanDate: LocalDate = LocalDate.now(),
    val dueDate: LocalDate = LocalDate.now().plusDays(14),
    val returnDate: LocalDate? = null,
    val status: LoanStatus = LoanStatus.ACTIVE
) {
    fun isOverdue(): Boolean =
        status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate)

    fun returnBook(): Loan = copy(
        returnDate = LocalDate.now(),
        status = LoanStatus.RETURNED
    )

    fun extend(days: Int = 7): Loan = copy(
        dueDate = dueDate.plusDays(days.toLong())
    )
}

enum class LoanStatus {
    ACTIVE,
    RETURNED,
    OVERDUE
}
