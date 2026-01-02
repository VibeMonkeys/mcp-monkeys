package com.monkeys.employee.adapter.`in`.mcp.dto

import com.monkeys.employee.domain.model.Position
import java.math.BigDecimal

data class PositionDto(
    val id: Long,
    val name: String,
    val level: Int,
    val minSalary: BigDecimal,
    val maxSalary: BigDecimal
) {
    companion object {
        fun fromDomain(position: Position): PositionDto = PositionDto(
            id = position.id,
            name = position.name,
            level = position.level,
            minSalary = position.minSalary,
            maxSalary = position.maxSalary
        )
    }
}
