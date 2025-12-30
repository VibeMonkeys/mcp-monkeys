package com.monkeys.employee.service

import com.monkeys.employee.entity.*
import com.monkeys.shared.util.ValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Employee MCP Tool Provider
 * 직원 관리 시스템의 MCP 도구들을 제공합니다.
 */
@Service
@Transactional(readOnly = true)
class EmployeeMcpService(
    private val employeeService: EmployeeService
) {
    private val logger = LoggerFactory.getLogger(EmployeeMcpService::class.java)

    // ===== 직원 조회 =====

    @Tool(
        name = "searchEmployees",
        description = "이름으로 직원을 검색합니다."
    )
    fun searchEmployees(
        @ToolParam(description = "검색할 직원 이름", required = true)
        name: String
    ): List<EmployeeInfo> {
        val validatedName = ValidationUtils.requireNotBlank(name, "직원 이름")
        logger.info("MCP Tool 호출: searchEmployees - name=$validatedName")
        return employeeService.searchEmployees(validatedName).map { it.toInfo() }
    }

    @Tool(
        name = "getEmployeeByNumber",
        description = "사번으로 직원을 조회합니다."
    )
    fun getEmployeeByNumber(
        @ToolParam(description = "사번", required = true)
        employeeNumber: String
    ): EmployeeInfo? {
        logger.info("MCP Tool 호출: getEmployeeByNumber - employeeNumber=$employeeNumber")
        return employeeService.findEmployeeByNumber(employeeNumber)?.toInfo()
    }

    @Tool(
        name = "getEmployeesByDepartment",
        description = "부서별 직원 목록을 조회합니다."
    )
    fun getEmployeesByDepartment(
        @ToolParam(description = "부서 ID", required = true)
        departmentId: Long
    ): List<EmployeeInfo> {
        logger.info("MCP Tool 호출: getEmployeesByDepartment - departmentId=$departmentId")
        return employeeService.findEmployeesByDepartment(departmentId).map { it.toInfo() }
    }

    @Tool(
        name = "getEmployeesByStatus",
        description = "상태별 직원 목록을 조회합니다."
    )
    fun getEmployeesByStatus(
        @ToolParam(description = "직원 상태 (ACTIVE, ON_LEAVE, RESIGNED)", required = true)
        status: String
    ): List<EmployeeInfo> {
        logger.info("MCP Tool 호출: getEmployeesByStatus - status=$status")
        val statusEnum = try {
            EmployeeStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            EmployeeStatus.ACTIVE
        }
        return employeeService.findEmployeesByStatus(statusEnum).map { it.toInfo() }
    }

    // ===== 직원 관리 =====

    @Tool(
        name = "changeDepartment",
        description = "직원의 부서를 변경합니다."
    )
    @Transactional
    fun changeDepartment(
        @ToolParam(description = "직원 ID", required = true)
        employeeId: Long,
        @ToolParam(description = "새 부서 ID", required = true)
        departmentId: Long
    ): EmployeeResult {
        logger.info("MCP Tool 호출: changeDepartment - employeeId=$employeeId, departmentId=$departmentId")

        val employee = employeeService.changeDepartment(employeeId, departmentId)
        return if (employee != null) {
            EmployeeResult(
                success = true,
                message = "부서가 변경되었습니다.",
                employee = employee.toInfo()
            )
        } else {
            EmployeeResult(
                success = false,
                message = "직원 또는 부서를 찾을 수 없습니다.",
                employee = null
            )
        }
    }

    @Tool(
        name = "changePosition",
        description = "직원의 직급을 변경합니다."
    )
    @Transactional
    fun changePosition(
        @ToolParam(description = "직원 ID", required = true)
        employeeId: Long,
        @ToolParam(description = "새 직급 ID", required = true)
        positionId: Long
    ): EmployeeResult {
        logger.info("MCP Tool 호출: changePosition - employeeId=$employeeId, positionId=$positionId")

        val employee = employeeService.changePosition(employeeId, positionId)
        return if (employee != null) {
            EmployeeResult(
                success = true,
                message = "직급이 변경되었습니다.",
                employee = employee.toInfo()
            )
        } else {
            EmployeeResult(
                success = false,
                message = "직원 또는 직급을 찾을 수 없습니다.",
                employee = null
            )
        }
    }

    @Tool(
        name = "updateSalary",
        description = "직원의 급여를 변경합니다."
    )
    @Transactional
    fun updateSalary(
        @ToolParam(description = "직원 ID", required = true)
        employeeId: Long,
        @ToolParam(description = "새 급여", required = true)
        newSalary: Double
    ): EmployeeResult {
        ValidationUtils.requirePositive(employeeId, "직원 ID")
        ValidationUtils.requirePositive(newSalary, "급여")
        logger.info("MCP Tool 호출: updateSalary - employeeId=$employeeId, newSalary=$newSalary")

        val employee = employeeService.updateSalary(employeeId, BigDecimal.valueOf(newSalary))
        return if (employee != null) {
            EmployeeResult(
                success = true,
                message = "급여가 변경되었습니다.",
                employee = employee.toInfo()
            )
        } else {
            EmployeeResult(
                success = false,
                message = "직원을 찾을 수 없습니다.",
                employee = null
            )
        }
    }

    @Tool(
        name = "takeLeave",
        description = "직원을 휴직 처리합니다."
    )
    @Transactional
    fun takeLeave(
        @ToolParam(description = "직원 ID", required = true)
        employeeId: Long
    ): EmployeeResult {
        logger.info("MCP Tool 호출: takeLeave - employeeId=$employeeId")

        val employee = employeeService.takeLeave(employeeId)
        return if (employee != null) {
            EmployeeResult(
                success = true,
                message = "휴직 처리되었습니다.",
                employee = employee.toInfo()
            )
        } else {
            EmployeeResult(
                success = false,
                message = "직원을 찾을 수 없습니다.",
                employee = null
            )
        }
    }

    @Tool(
        name = "returnFromLeave",
        description = "직원의 휴직을 해제합니다."
    )
    @Transactional
    fun returnFromLeave(
        @ToolParam(description = "직원 ID", required = true)
        employeeId: Long
    ): EmployeeResult {
        logger.info("MCP Tool 호출: returnFromLeave - employeeId=$employeeId")

        val employee = employeeService.returnFromLeave(employeeId)
        return if (employee != null) {
            EmployeeResult(
                success = true,
                message = "복직 처리되었습니다.",
                employee = employee.toInfo()
            )
        } else {
            EmployeeResult(
                success = false,
                message = "직원을 찾을 수 없습니다.",
                employee = null
            )
        }
    }

    @Tool(
        name = "resignEmployee",
        description = "직원을 퇴사 처리합니다."
    )
    @Transactional
    fun resignEmployee(
        @ToolParam(description = "직원 ID", required = true)
        employeeId: Long,
        @ToolParam(description = "퇴사일 (YYYY-MM-DD 형식). 비워두면 오늘 날짜")
        resignDate: String? = null
    ): EmployeeResult {
        logger.info("MCP Tool 호출: resignEmployee - employeeId=$employeeId")

        val date = resignDate?.let {
            try { LocalDate.parse(it) } catch (e: Exception) { LocalDate.now() }
        } ?: LocalDate.now()

        val employee = employeeService.resign(employeeId, date)
        return if (employee != null) {
            EmployeeResult(
                success = true,
                message = "퇴사 처리되었습니다.",
                employee = employee.toInfo()
            )
        } else {
            EmployeeResult(
                success = false,
                message = "직원을 찾을 수 없습니다.",
                employee = null
            )
        }
    }

    // ===== 부서 조회 =====

    @Tool(
        name = "getAllDepartments",
        description = "모든 부서 목록을 조회합니다."
    )
    fun getAllDepartments(): List<DepartmentInfo> {
        logger.info("MCP Tool 호출: getAllDepartments")
        return employeeService.findAllDepartments().map { it.toInfo() }
    }

    @Tool(
        name = "getDepartmentDetail",
        description = "부서 상세 정보를 조회합니다."
    )
    fun getDepartmentDetail(
        @ToolParam(description = "부서 ID", required = true)
        departmentId: Long
    ): DepartmentDetailInfo? {
        logger.info("MCP Tool 호출: getDepartmentDetail - departmentId=$departmentId")
        return employeeService.findDepartmentById(departmentId)?.toDetailInfo()
    }

    // ===== 직급 조회 =====

    @Tool(
        name = "getAllPositions",
        description = "모든 직급 목록을 조회합니다."
    )
    fun getAllPositions(): List<PositionInfo> {
        logger.info("MCP Tool 호출: getAllPositions")
        return employeeService.findAllPositions().map { it.toInfo() }
    }

    // ===== 통계 =====

    @Tool(
        name = "getEmployeeStats",
        description = "직원 통계를 조회합니다."
    )
    fun getEmployeeStats(): EmployeeStats {
        logger.info("MCP Tool 호출: getEmployeeStats")
        return employeeService.getEmployeeStats()
    }
}

// ===== DTO =====

data class EmployeeInfo(
    val id: Long,
    val employeeNumber: String,
    val name: String,
    val email: String,
    val phone: String?,
    val departmentName: String?,
    val positionName: String?,
    val status: String,
    val hireDate: String,
    val yearsOfService: Int,
    val salary: String
)

data class DepartmentInfo(
    val id: Long,
    val name: String,
    val code: String,
    val description: String?,
    val managerName: String?,
    val employeeCount: Int
)

data class DepartmentDetailInfo(
    val id: Long,
    val name: String,
    val code: String,
    val description: String?,
    val managerName: String?,
    val employees: List<EmployeeInfo>
)

data class PositionInfo(
    val id: Long,
    val name: String,
    val level: Int,
    val description: String?,
    val minSalary: String,
    val maxSalary: String,
    val employeeCount: Int
)

data class EmployeeResult(
    val success: Boolean,
    val message: String,
    val employee: EmployeeInfo?
)

// ===== Extension Functions =====

private fun Employee.toInfo() = EmployeeInfo(
    id = id,
    employeeNumber = employeeNumber,
    name = name,
    email = email,
    phone = phone,
    departmentName = department?.name,
    positionName = position?.name,
    status = status.name,
    hireDate = hireDate.toString(),
    yearsOfService = getYearsOfService(),
    salary = salary.toString()
)

private fun Department.toInfo() = DepartmentInfo(
    id = id,
    name = name,
    code = code,
    description = description,
    managerName = manager?.name,
    employeeCount = getEmployeeCount()
)

private fun Department.toDetailInfo() = DepartmentDetailInfo(
    id = id,
    name = name,
    code = code,
    description = description,
    managerName = manager?.name,
    employees = employees.map { it.toInfo() }
)

private fun Position.toInfo() = PositionInfo(
    id = id,
    name = name,
    level = level,
    description = description,
    minSalary = minSalary.toString(),
    maxSalary = maxSalary.toString(),
    employeeCount = getEmployeeCount()
)
