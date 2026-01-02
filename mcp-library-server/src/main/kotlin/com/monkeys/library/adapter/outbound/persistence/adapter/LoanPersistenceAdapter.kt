package com.monkeys.library.adapter.outbound.persistence.adapter

import com.monkeys.library.adapter.outbound.persistence.entity.LoanEntity
import com.monkeys.library.adapter.outbound.persistence.repository.BookJpaRepository
import com.monkeys.library.adapter.outbound.persistence.repository.LoanJpaRepository
import com.monkeys.library.application.port.outbound.LoanRepository
import com.monkeys.library.domain.model.Loan
import com.monkeys.library.domain.model.LoanStatus
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class LoanPersistenceAdapter(
    private val loanJpaRepository: LoanJpaRepository,
    private val bookJpaRepository: BookJpaRepository
) : LoanRepository {

    override fun findById(id: Long): Loan? =
        loanJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByBorrowerEmail(email: String): List<Loan> =
        loanJpaRepository.findByBorrowerEmail(email).map { it.toDomain() }

    override fun findByStatus(status: LoanStatus): List<Loan> =
        loanJpaRepository.findByStatus(status).map { it.toDomain() }

    override fun findByDueDateBeforeAndStatus(date: LocalDate, status: LoanStatus): List<Loan> =
        loanJpaRepository.findByDueDateBeforeAndStatus(date, status).map { it.toDomain() }

    override fun save(loan: Loan): Loan {
        val entity = if (loan.id == 0L) {
            val bookEntity = bookJpaRepository.findById(loan.book.id).orElseThrow {
                IllegalArgumentException("Book not found: ${loan.book.id}")
            }
            LoanEntity(
                book = bookEntity,
                borrowerName = loan.borrowerName,
                borrowerEmail = loan.borrowerEmail,
                loanDate = loan.loanDate,
                dueDate = loan.dueDate,
                returnDate = loan.returnDate,
                status = loan.status
            )
        } else {
            loanJpaRepository.findById(loan.id).orElseThrow {
                IllegalArgumentException("Loan not found: ${loan.id}")
            }.apply { updateFrom(loan) }
        }
        return loanJpaRepository.save(entity).toDomain()
    }

    override fun countByStatus(status: LoanStatus): Long =
        loanJpaRepository.countByStatus(status)

    override fun countByDueDateBeforeAndStatus(date: LocalDate, status: LoanStatus): Long =
        loanJpaRepository.countByDueDateBeforeAndStatus(date, status)
}
