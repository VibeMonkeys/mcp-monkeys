package com.monkeys.employee.adapter.outbound.persistence.repository

import com.monkeys.employee.adapter.outbound.persistence.entity.DepartmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DepartmentJpaRepository : JpaRepository<DepartmentEntity, Long>
