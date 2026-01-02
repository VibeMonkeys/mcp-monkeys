package com.monkeys.employee.application.port.outbound

import com.monkeys.employee.domain.model.Position

interface PositionRepository {
    fun findById(id: Long): Position?
    fun findAll(): List<Position>
    fun count(): Long
}
