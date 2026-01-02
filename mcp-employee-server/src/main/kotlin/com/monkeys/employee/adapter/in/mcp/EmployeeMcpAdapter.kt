package com.monkeys.employee.adapter.`in`.mcp

import com.monkeys.employee.adapter.`in`.mcp.dto.DepartmentDto
import com.monkeys.employee.adapter.`in`.mcp.dto.EmployeeDto
import com.monkeys.employee.adapter.`in`.mcp.dto.EmployeeStatsDto
import com.monkeys.employee.adapter.`in`.mcp.dto.PositionDto
import com.monkeys.employee.application.port.`in`.DepartmentUseCase
import com.monkeys.employee.application.port.`in`.EmployeeStatsUseCase
import com.monkeys.employee.application.port.`in`.EmployeeUseCase
import com.monkeys.employee.application.port.`in`.PositionUseCase
import com.monkeys.employee.domain.model.EmployeeStatus
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
class EmployeeMcpAdapter(
    private val employeeUseCase: EmployeeUseCase,
    private val departmentUseCase: DepartmentUseCase,
    private val positionUseCase: PositionUseCase,
    private val employeeStatsUseCase: EmployeeStatsUseCase
) {

    @Tool(description = "ID로 직원을 조회합니다.")
    fun getEmployeeById(
        @ToolParam(description = "직원 ID") id: Long
    ): EmployeeDto? =
        employeeUseCase.findEmployeeById(id)?.let { EmployeeDto.fromDomain(it) }

    @Tool(description = "사번으로 직원을 조회합니다.")
    fun getEmployeeByNumber(
        @ToolParam(description = "사번") employeeNumber: String
    ): EmployeeDto? =
        employeeUseCase.findEmployeeByNumber(employeeNumber)?.let { EmployeeDto.fromDomain(it) }

    @Tool(description = "이름으로 직원을 검색합니다.")
    fun searchEmployees(
        @ToolParam(description = "검색할 이름") name: String
    ): List<EmployeeDto> =
        employeeUseCase.searchEmployees(name).map { EmployeeDto.fromDomain(it) }

    @Tool(description = "부서별 직원 목록을 조회합니다.")
    fun getEmployeesByDepartment(
        @ToolParam(description = "부서 ID") departmentId: Long
    ): List<EmployeeDto> =
        employeeUseCase.findEmployeesByDepartment(departmentId).map { EmployeeDto.fromDomain(it) }

    @Tool(description = "상태별 직원 목록을 조회합니다. (ACTIVE, ON_LEAVE, RESIGNED)")
    fun getEmployeesByStatus(
        @ToolParam(description = "직원 상태 (ACTIVE, ON_LEAVE, RESIGNED)") status: String
    ): List<EmployeeDto> {
        val employeeStatus = try {
            EmployeeStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            return emptyList()
        }
        return employeeUseCase.findEmployeesByStatus(employeeStatus).map { EmployeeDto.fromDomain(it) }
    }

    @Tool(description = "직원의 부서를 변경합니다.")
    fun changeDepartment(
        @ToolParam(description = "직원 ID") employeeId: Long,
        @ToolParam(description = "새 부서 ID") departmentId: Long
    ): EmployeeDto? =
        employeeUseCase.changeDepartment(employeeId, departmentId)?.let { EmployeeDto.fromDomain(it) }

    @Tool(description = "직원의 직급을 변경합니다.")
    fun changePosition(
        @ToolParam(description = "직원 ID") employeeId: Long,
        @ToolParam(description = "새 직급 ID") positionId: Long
    ): EmployeeDto? =
        employeeUseCase.changePosition(employeeId, positionId)?.let { EmployeeDto.fromDomain(it) }

    @Tool(description = "직원의 급여를 변경합니다.")
    fun updateSalary(
        @ToolParam(description = "직원 ID") employeeId: Long,
        @ToolParam(description = "새 급여") newSalary: BigDecimal
    ): EmployeeDto? =
        employeeUseCase.updateSalary(employeeId, newSalary)?.let { EmployeeDto.fromDomain(it) }

    @Tool(description = "직원을 휴직 처리합니다.")
    fun takeLeave(
        @ToolParam(description = "직원 ID") employeeId: Long
    ): EmployeeDto? =
        employeeUseCase.takeLeave(employeeId)?.let { EmployeeDto.fromDomain(it) }

    @Tool(description = "휴직 중인 직원을 복직 처리합니다.")
    fun returnFromLeave(
        @ToolParam(description = "직원 ID") employeeId: Long
    ): EmployeeDto? =
        employeeUseCase.returnFromLeave(employeeId)?.let { EmployeeDto.fromDomain(it) }

    @Tool(description = "직원을 퇴사 처리합니다.")
    fun resign(
        @ToolParam(description = "직원 ID") employeeId: Long,
        @ToolParam(description = "퇴사일 (YYYY-MM-DD 형식)") resignDate: String
    ): EmployeeDto? {
        val date = try {
            LocalDate.parse(resignDate)
        } catch (e: Exception) {
            LocalDate.now()
        }
        return employeeUseCase.resign(employeeId, date)?.let { EmployeeDto.fromDomain(it) }
    }

    @Tool(description = "모든 부서 목록을 조회합니다.")
    fun getAllDepartments(): List<DepartmentDto> =
        departmentUseCase.findAllDepartments().map { DepartmentDto.fromDomain(it) }

    @Tool(description = "ID로 부서를 조회합니다.")
    fun getDepartmentById(
        @ToolParam(description = "부서 ID") id: Long
    ): DepartmentDto? =
        departmentUseCase.findDepartmentById(id)?.let { DepartmentDto.fromDomain(it) }

    @Tool(description = "모든 직급 목록을 조회합니다.")
    fun getAllPositions(): List<PositionDto> =
        positionUseCase.findAllPositions().map { PositionDto.fromDomain(it) }

    @Tool(description = "ID로 직급을 조회합니다.")
    fun getPositionById(
        @ToolParam(description = "직급 ID") id: Long
    ): PositionDto? =
        positionUseCase.findPositionById(id)?.let { PositionDto.fromDomain(it) }

    @Tool(description = "직원 통계 정보를 조회합니다.")
    fun getEmployeeStats(): EmployeeStatsDto =
        EmployeeStatsDto.fromDomain(employeeStatsUseCase.getEmployeeStats())
}
