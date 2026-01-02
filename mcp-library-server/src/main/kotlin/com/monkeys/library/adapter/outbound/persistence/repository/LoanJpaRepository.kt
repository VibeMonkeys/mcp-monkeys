package com.monkeys.library.adapter.outbound.persistence.repository

import com.monkeys.library.adapter.outbound.persistence.entity.LoanEntity
import com.monkeys.library.domain.model.LoanStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface LoanJpaRepository : JpaRepository<LoanEntity, Long> {
    fun findByBorrowerEmail(email: String): List<LoanEntity>
    fun findByStatus(status: LoanStatus): List<LoanEntity>
    fun findByDueDateBeforeAndStatus(date: LocalDate, status: LoanStatus): List<LoanEntity>
    fun countByStatus(status: LoanStatus): Long
    fun countByDueDateBeforeAndStatus(date: LocalDate, status: LoanStatus): Long
}
