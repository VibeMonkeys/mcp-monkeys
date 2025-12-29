package com.monkeys.library.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.library.entity.Loan
import com.monkeys.library.entity.LoanStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface LoanRepository : JpaRepository<Loan, Long>, KotlinJdslJpqlExecutor {

    fun findByBorrowerEmail(email: String): List<Loan>

    fun findByBorrowerNameContainingIgnoreCase(name: String): List<Loan>

    fun findByStatus(status: LoanStatus): List<Loan>

    fun findByBookId(bookId: Long): List<Loan>

    fun findByBookIdAndStatus(bookId: Long, status: LoanStatus): List<Loan>

    fun findByDueDateBeforeAndStatus(date: LocalDate, status: LoanStatus): List<Loan>
}
