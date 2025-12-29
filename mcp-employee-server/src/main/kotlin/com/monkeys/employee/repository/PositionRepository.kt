package com.monkeys.employee.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.employee.entity.Position
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PositionRepository : JpaRepository<Position, Long>, KotlinJdslJpqlExecutor {
    fun findByName(name: String): Optional<Position>
    fun findByLevel(level: Int): List<Position>
    fun findByLevelGreaterThanEqual(level: Int): List<Position>
}
