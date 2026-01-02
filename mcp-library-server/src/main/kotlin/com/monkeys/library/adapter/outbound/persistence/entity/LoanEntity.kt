package com.monkeys.library.adapter.outbound.persistence.entity

import com.monkeys.library.domain.model.Loan
import com.monkeys.library.domain.model.LoanStatus
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "loans")
class LoanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    var book: BookEntity,

    @Column(name = "borrower_name", nullable = false, length = 100)
    var borrowerName: String,

    @Column(name = "borrower_email", nullable = false, length = 100)
    var borrowerEmail: String,

    @Column(name = "loan_date", nullable = false)
    var loanDate: LocalDate = LocalDate.now(),

    @Column(name = "due_date", nullable = false)
    var dueDate: LocalDate = LocalDate.now().plusDays(14),

    @Column(name = "return_date")
    var returnDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LoanStatus = LoanStatus.ACTIVE
) {
    fun toDomain() = Loan(
        id = id,
        book = book.toDomain(),
        borrowerName = borrowerName,
        borrowerEmail = borrowerEmail,
        loanDate = loanDate,
        dueDate = dueDate,
        returnDate = returnDate,
        status = status
    )

    fun updateFrom(loan: Loan) {
        this.borrowerName = loan.borrowerName
        this.borrowerEmail = loan.borrowerEmail
        this.loanDate = loan.loanDate
        this.dueDate = loan.dueDate
        this.returnDate = loan.returnDate
        this.status = loan.status
    }
}
