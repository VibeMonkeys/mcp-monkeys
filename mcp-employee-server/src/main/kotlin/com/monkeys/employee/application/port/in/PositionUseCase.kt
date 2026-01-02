package com.monkeys.employee.application.port.`in`

import com.monkeys.employee.domain.model.Position

interface PositionUseCase {
    fun findAllPositions(): List<Position>
    fun findPositionById(id: Long): Position?
}
