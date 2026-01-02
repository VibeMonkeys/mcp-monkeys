package com.monkeys.employee.adapter.outbound.persistence.entity

import com.monkeys.employee.domain.model.Position
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "positions")
class PositionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column
    val level: Int = 1,

    @Column(name = "min_salary")
    val minSalary: BigDecimal = BigDecimal.ZERO,

    @Column(name = "max_salary")
    val maxSalary: BigDecimal = BigDecimal.ZERO
) {
    fun toDomain(): Position = Position(
        id = id,
        name = name,
        level = level,
        minSalary = minSalary,
        maxSalary = maxSalary
    )

    companion object {
        fun fromDomain(position: Position): PositionEntity = PositionEntity(
            id = position.id,
            name = position.name,
            level = position.level,
            minSalary = position.minSalary,
            maxSalary = position.maxSalary
        )
    }
}
