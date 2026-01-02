package com.monkeys.employee.adapter.outbound.persistence.repository

import com.monkeys.employee.adapter.outbound.persistence.entity.PositionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PositionJpaRepository : JpaRepository<PositionEntity, Long>
