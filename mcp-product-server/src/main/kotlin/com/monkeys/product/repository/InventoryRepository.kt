package com.monkeys.product.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.product.entity.Inventory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InventoryRepository : JpaRepository<Inventory, Long>, KotlinJdslJpqlExecutor {
    fun findByProductId(productId: Long): Optional<Inventory>

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderLevel")
    fun findLowStockInventories(): List<Inventory>

    @Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
    fun findOutOfStockInventories(): List<Inventory>

    fun findByLocation(location: String): List<Inventory>
}
