package com.monkeys.employee.service

import com.monkeys.employee.entity.*
import com.monkeys.employee.repository.DepartmentRepository
import com.monkeys.employee.repository.EmployeeRepository
import com.monkeys.employee.repository.PositionRepository
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.extensions.spring.SpringExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class EmployeeServiceTest : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)
    override fun isolationMode() = IsolationMode.InstancePerTest

    @Autowired
    private lateinit var employeeService: EmployeeService

    @Autowired
    private lateinit var employeeRepository: EmployeeRepository

    @Autowired
    private lateinit var departmentRepository: DepartmentRepository

    @Autowired
    private lateinit var positionRepository: PositionRepository

    private val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    init {
        describe("직원 조회") {
            context("이름으로 검색할 때") {
                it("해당 이름을 포함하는 직원 목록을 반환한다") {
                    // when
                    val result = employeeService.searchEmployees("김")

                    // then
                    result.shouldNotBeEmpty()
                    result.forEach {
                        it.name.contains("김") shouldBe true
                    }
                }
            }

            context("사번으로 조회할 때") {
                it("해당 사번의 직원을 반환한다") {
                    // when
                    val result = employeeService.findEmployeeByNumber("EMP001")

                    // then
                    result.shouldNotBeNull()
                    result.employeeNumber shouldBe "EMP001"
                }

                it("존재하지 않는 사번이면 null을 반환한다") {
                    // when
                    val result = employeeService.findEmployeeByNumber("EMP999")

                    // then
                    result.shouldBeNull()
                }
            }

            context("상태로 조회할 때") {
                it("해당 상태의 직원 목록을 반환한다") {
                    // when
                    val result = employeeService.findEmployeesByStatus(EmployeeStatus.ACTIVE)

                    // then
                    result.shouldNotBeEmpty()
                    result.forEach {
                        it.status shouldBe EmployeeStatus.ACTIVE
                    }
                }
            }

            context("부서로 조회할 때") {
                it("해당 부서의 직원 목록을 반환한다") {
                    // given
                    val department = departmentRepository.findByCode("DEV").orElse(null)

                    // when
                    val result = employeeService.findEmployeesByDepartment(department.id)

                    // then
                    result.shouldNotBeEmpty()
                }
            }
        }

        describe("부서 변경") {
            it("직원의 부서를 변경한다") {
                // given
                val employee = employeeService.findEmployeeByNumber("EMP012")!!
                val newDept = departmentRepository.findByCode("QA").orElse(null)!!

                // when
                val result = employeeService.changeDepartment(employee.id, newDept.id)

                // then
                result.shouldNotBeNull()
                result.department?.code shouldBe "QA"
            }
        }

        describe("휴직 처리") {
            it("재직 중인 직원을 휴직 처리한다") {
                // given
                val activeEmployee = employeeService.findEmployeesByStatus(EmployeeStatus.ACTIVE).first()

                // when
                val result = employeeService.takeLeave(activeEmployee.id)

                // then
                result.shouldNotBeNull()
                result.status shouldBe EmployeeStatus.ON_LEAVE
            }
        }

        describe("복직 처리") {
            it("휴직 중인 직원을 복직 처리한다") {
                // given
                val onLeaveEmployee = employeeService.findEmployeesByStatus(EmployeeStatus.ON_LEAVE).firstOrNull()

                if (onLeaveEmployee != null) {
                    // when
                    val result = employeeService.returnFromLeave(onLeaveEmployee.id)

                    // then
                    result.shouldNotBeNull()
                    result.status shouldBe EmployeeStatus.ACTIVE
                }
            }
        }

        describe("부서 조회") {
            it("모든 부서를 반환한다") {
                // when
                val result = employeeService.findAllDepartments()

                // then
                result.shouldNotBeEmpty()
            }

            it("부서 코드로 조회한다") {
                // when
                val result = employeeService.findDepartmentByCode("DEV")

                // then
                result.shouldNotBeNull()
                result.name shouldBe "개발팀"
            }
        }

        describe("직급 조회") {
            it("모든 직급을 반환한다") {
                // when
                val result = employeeService.findAllPositions()

                // then
                result.shouldNotBeEmpty()
            }
        }

        describe("통계 조회") {
            it("직원 통계를 반환한다") {
                // when
                val result = employeeService.getEmployeeStats()

                // then
                result.totalEmployees shouldBeGreaterThan 0
                result.totalDepartments shouldBeGreaterThan 0
                result.totalPositions shouldBeGreaterThan 0
            }
        }
    }
}
