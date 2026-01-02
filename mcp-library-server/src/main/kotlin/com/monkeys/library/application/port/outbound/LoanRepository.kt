package com.monkeys.library.application.port.outbound

import com.monkeys.library.domain.model.Loan
import com.monkeys.library.domain.model.LoanStatus
import java.time.LocalDate

interface LoanRepository {
    fun findById(id: Long): Loan?
    fun findByBorrowerEmail(email: String): List<Loan>
    fun findByStatus(status: LoanStatus): List<Loan>
    fun findByDueDateBeforeAndStatus(date: LocalDate, status: LoanStatus): List<Loan>
    fun save(loan: Loan): Loan
    fun countByStatus(status: LoanStatus): Long
    fun countByDueDateBeforeAndStatus(date: LocalDate, status: LoanStatus): Long
}
