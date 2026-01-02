package com.monkeys.employee.adapter.outbound.persistence.adapter

import com.monkeys.employee.adapter.outbound.persistence.repository.PositionJpaRepository
import com.monkeys.employee.application.port.outbound.PositionRepository
import com.monkeys.employee.domain.model.Position
import org.springframework.stereotype.Component

@Component
class PositionPersistenceAdapter(
    private val positionJpaRepository: PositionJpaRepository
) : PositionRepository {

    override fun findById(id: Long): Position? =
        positionJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)

    override fun findAll(): List<Position> =
        positionJpaRepository.findAll().map { it.toDomain() }

    override fun count(): Long =
        positionJpaRepository.count()
}
