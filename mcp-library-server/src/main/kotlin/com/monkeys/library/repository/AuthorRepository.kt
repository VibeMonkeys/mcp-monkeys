package com.monkeys.library.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.monkeys.library.entity.Author
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorRepository : JpaRepository<Author, Long>, KotlinJdslJpqlExecutor {

    fun findByNameContainingIgnoreCase(name: String): List<Author>

    fun findByNationality(nationality: String): List<Author>
}
