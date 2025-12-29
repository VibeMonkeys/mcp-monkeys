package com.monkeys.library.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "loans")
class Loan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    var book: Book,

    @Column(name = "borrower_name", nullable = false, length = 100)
    var borrowerName: String,

    @Column(name = "borrower_email", nullable = false, length = 200)
    var borrowerEmail: String,

    @Column(name = "loan_date", nullable = false)
    var loanDate: LocalDate = LocalDate.now(),

    @Column(name = "due_date", nullable = false)
    var dueDate: LocalDate = LocalDate.now().plusDays(14),

    @Column(name = "return_date")
    var returnDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: LoanStatus = LoanStatus.ACTIVE,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    fun isOverdue(): Boolean =
        status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate)

    fun returnBook() {
        returnDate = LocalDate.now()
        status = LoanStatus.RETURNED
        updatedAt = LocalDateTime.now()
        book.returnBook()
    }

    fun extend(days: Int = 7) {
        if (status != LoanStatus.ACTIVE) return
        dueDate = dueDate.plusDays(days.toLong())
        updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Loan) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

enum class LoanStatus {
    ACTIVE,     // 대출 중
    RETURNED,   // 반납 완료
    OVERDUE     // 연체
}
