package com.monkeys.employee.service

import com.monkeys.employee.entity.*
import com.monkeys.employee.repository.DepartmentRepository
import com.monkeys.employee.repository.EmployeeRepository
import com.monkeys.employee.repository.PositionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val departmentRepository: DepartmentRepository,
    private val positionRepository: PositionRepository
) {

    // ===== Employee Operations =====

    fun findAllEmployees(): List<Employee> =
        employeeRepository.findAll()

    fun findEmployeeById(id: Long): Employee? =
        employeeRepository.findById(id).orElse(null)

    fun findEmployeeByNumber(employeeNumber: String): Employee? =
        employeeRepository.findByEmployeeNumber(employeeNumber).orElse(null)

    fun findEmployeeByEmail(email: String): Employee? =
        employeeRepository.findByEmail(email).orElse(null)

    fun findEmployeesByStatus(status: EmployeeStatus): List<Employee> =
        employeeRepository.findByStatus(status)

    fun findEmployeesByDepartment(departmentId: Long): List<Employee> =
        employeeRepository.findByDepartmentId(departmentId)

    fun findEmployeesByPosition(positionId: Long): List<Employee> =
        employeeRepository.findByPositionId(positionId)

    fun searchEmployees(name: String): List<Employee> =
        employeeRepository.findByNameContainingIgnoreCase(name)

    @Transactional
    fun createEmployee(
        employeeNumber: String,
        name: String,
        email: String,
        phone: String?,
        departmentId: Long?,
        positionId: Long?,
        hireDate: LocalDate,
        salary: BigDecimal
    ): Employee {
        val department = departmentId?.let { departmentRepository.findById(it).orElse(null) }
        val position = positionId?.let { positionRepository.findById(it).orElse(null) }

        val employee = Employee(
            employeeNumber = employeeNumber,
            name = name,
            email = email,
            phone = phone,
            department = department,
            position = position,
            hireDate = hireDate,
            salary = salary
        )

        return employeeRepository.save(employee)
    }

    @Transactional
    fun updateEmployee(id: Long, name: String, email: String, phone: String?): Employee? {
        val employee = employeeRepository.findById(id).orElse(null) ?: return null
        employee.update(name, email, phone)
        return employeeRepository.save(employee)
    }

    @Transactional
    fun changeDepartment(employeeId: Long, departmentId: Long): Employee? {
        val employee = employeeRepository.findById(employeeId).orElse(null) ?: return null
        val department = departmentRepository.findById(departmentId).orElse(null) ?: return null
        employee.changeDepartment(department)
        return employeeRepository.save(employee)
    }

    @Transactional
    fun changePosition(employeeId: Long, positionId: Long): Employee? {
        val employee = employeeRepository.findById(employeeId).orElse(null) ?: return null
        val position = positionRepository.findById(positionId).orElse(null) ?: return null
        employee.changePosition(position)
        return employeeRepository.save(employee)
    }

    @Transactional
    fun updateSalary(employeeId: Long, newSalary: BigDecimal): Employee? {
        val employee = employeeRepository.findById(employeeId).orElse(null) ?: return null
        employee.updateSalary(newSalary)
        return employeeRepository.save(employee)
    }

    @Transactional
    fun takeLeave(employeeId: Long): Employee? {
        val employee = employeeRepository.findById(employeeId).orElse(null) ?: return null
        employee.takeLeave()
        return employeeRepository.save(employee)
    }

    @Transactional
    fun returnFromLeave(employeeId: Long): Employee? {
        val employee = employeeRepository.findById(employeeId).orElse(null) ?: return null
        employee.returnFromLeave()
        return employeeRepository.save(employee)
    }

    @Transactional
    fun resign(employeeId: Long, resignDate: LocalDate = LocalDate.now()): Employee? {
        val employee = employeeRepository.findById(employeeId).orElse(null) ?: return null
        employee.resign(resignDate)
        return employeeRepository.save(employee)
    }

    // ===== Department Operations =====

    fun findAllDepartments(): List<Department> =
        departmentRepository.findAll()

    fun findDepartmentById(id: Long): Department? =
        departmentRepository.findById(id).orElse(null)

    fun findDepartmentByCode(code: String): Department? =
        departmentRepository.findByCode(code).orElse(null)

    @Transactional
    fun createDepartment(name: String, code: String, description: String?): Department {
        val department = Department(name = name, code = code, description = description)
        return departmentRepository.save(department)
    }

    @Transactional
    fun assignDepartmentManager(departmentId: Long, employeeId: Long): Department? {
        val department = departmentRepository.findById(departmentId).orElse(null) ?: return null
        val employee = employeeRepository.findById(employeeId).orElse(null) ?: return null
        department.assignManager(employee)
        return departmentRepository.save(department)
    }

    // ===== Position Operations =====

    fun findAllPositions(): List<Position> =
        positionRepository.findAll()

    fun findPositionById(id: Long): Position? =
        positionRepository.findById(id).orElse(null)

    fun findPositionByName(name: String): Position? =
        positionRepository.findByName(name).orElse(null)

    // ===== Statistics =====

    fun getEmployeeStats(): EmployeeStats {
        // 최적화: 개별 count 쿼리 사용 (전체 데이터 로드 대신)
        val totalEmployees = employeeRepository.count()
        val active = employeeRepository.countByStatus(EmployeeStatus.ACTIVE)
        val onLeave = employeeRepository.countByStatus(EmployeeStatus.ON_LEAVE)
        val resigned = employeeRepository.countByStatus(EmployeeStatus.RESIGNED)
        val departments = departmentRepository.count()
        val positions = positionRepository.count()
        val avgSalary = employeeRepository.findAverageSalaryOfActiveEmployees() ?: BigDecimal.ZERO

        return EmployeeStats(
            totalEmployees = totalEmployees.toInt(),
            activeEmployees = active.toInt(),
            onLeaveEmployees = onLeave.toInt(),
            resignedEmployees = resigned.toInt(),
            totalDepartments = departments,
            totalPositions = positions,
            averageSalary = avgSalary
        )
    }
}

data class EmployeeStats(
    val totalEmployees: Int,
    val activeEmployees: Int,
    val onLeaveEmployees: Int,
    val resignedEmployees: Int,
    val totalDepartments: Long,
    val totalPositions: Long,
    val averageSalary: BigDecimal
)
