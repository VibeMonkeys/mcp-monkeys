package com.monkeys.employee.application.service

import com.monkeys.employee.application.port.`in`.DepartmentUseCase
import com.monkeys.employee.application.port.`in`.EmployeeStatsUseCase
import com.monkeys.employee.application.port.`in`.EmployeeUseCase
import com.monkeys.employee.application.port.`in`.PositionUseCase
import com.monkeys.employee.application.port.outbound.DepartmentRepository
import com.monkeys.employee.application.port.outbound.EmployeeRepository
import com.monkeys.employee.application.port.outbound.PositionRepository
import com.monkeys.employee.domain.model.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val departmentRepository: DepartmentRepository,
    private val positionRepository: PositionRepository
) : EmployeeUseCase, DepartmentUseCase, PositionUseCase, EmployeeStatsUseCase {

    // EmployeeUseCase implementation
    override fun searchEmployees(name: String): List<Employee> =
        employeeRepository.findByNameContaining(name)

    override fun findEmployeeByNumber(employeeNumber: String): Employee? =
        employeeRepository.findByEmployeeNumber(employeeNumber)

    override fun findEmployeeById(id: Long): Employee? =
        employeeRepository.findById(id)

    override fun findEmployeesByDepartment(departmentId: Long): List<Employee> =
        employeeRepository.findByDepartmentId(departmentId)

    override fun findEmployeesByStatus(status: EmployeeStatus): List<Employee> =
        employeeRepository.findByStatus(status)

    override fun changeDepartment(employeeId: Long, departmentId: Long): Employee? {
        val employee = employeeRepository.findById(employeeId) ?: return null
        val department = departmentRepository.findById(departmentId) ?: return null
        val updatedEmployee = employee.changeDepartment(department)
        return employeeRepository.save(updatedEmployee)
    }

    override fun changePosition(employeeId: Long, positionId: Long): Employee? {
        val employee = employeeRepository.findById(employeeId) ?: return null
        val position = positionRepository.findById(positionId) ?: return null
        val updatedEmployee = employee.changePosition(position)
        return employeeRepository.save(updatedEmployee)
    }

    override fun updateSalary(employeeId: Long, newSalary: BigDecimal): Employee? {
        val employee = employeeRepository.findById(employeeId) ?: return null
        val updatedEmployee = employee.updateSalary(newSalary)
        return employeeRepository.save(updatedEmployee)
    }

    override fun takeLeave(employeeId: Long): Employee? {
        val employee = employeeRepository.findById(employeeId) ?: return null
        val updatedEmployee = employee.takeLeave()
        return employeeRepository.save(updatedEmployee)
    }

    override fun returnFromLeave(employeeId: Long): Employee? {
        val employee = employeeRepository.findById(employeeId) ?: return null
        val updatedEmployee = employee.returnFromLeave()
        return employeeRepository.save(updatedEmployee)
    }

    override fun resign(employeeId: Long, resignDate: LocalDate): Employee? {
        val employee = employeeRepository.findById(employeeId) ?: return null
        val updatedEmployee = employee.resign(resignDate)
        return employeeRepository.save(updatedEmployee)
    }

    // DepartmentUseCase implementation
    override fun findAllDepartments(): List<Department> =
        departmentRepository.findAll()

    override fun findDepartmentById(id: Long): Department? =
        departmentRepository.findById(id)

    // PositionUseCase implementation
    override fun findAllPositions(): List<Position> =
        positionRepository.findAll()

    override fun findPositionById(id: Long): Position? =
        positionRepository.findById(id)

    // EmployeeStatsUseCase implementation
    override fun getEmployeeStats(): EmployeeStats {
        val totalEmployees = employeeRepository.count().toInt()
        val activeEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE).toInt()
        val onLeaveEmployees = employeeRepository.countByStatus(EmployeeStatus.ON_LEAVE).toInt()
        val resignedEmployees = employeeRepository.countByStatus(EmployeeStatus.RESIGNED).toInt()
        val totalDepartments = departmentRepository.count()
        val totalPositions = positionRepository.count()
        val averageSalary = employeeRepository.calculateAverageSalary() ?: BigDecimal.ZERO
        val averageYearsOfService = employeeRepository.calculateAverageYearsOfService() ?: 0.0

        return EmployeeStats(
            totalEmployees = totalEmployees,
            activeEmployees = activeEmployees,
            onLeaveEmployees = onLeaveEmployees,
            resignedEmployees = resignedEmployees,
            totalDepartments = totalDepartments,
            totalPositions = totalPositions,
            averageSalary = averageSalary,
            averageYearsOfService = averageYearsOfService
        )
    }
}
