---
name: test-writing-guide
description: 테스트 작성 가이드. 테스트 코드 작성, 단위 테스트, 통합 테스트, Kotest 사용 시 적용. Kotest + Fixture Monkey 패턴.
---

# 테스트 작성 가이드

이 프로젝트의 테스트 작성 표준입니다. Kotest + Fixture Monkey를 사용합니다.

## 테스트 스택

```kotlin
// build.gradle.kts
dependencies {
    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:6.0.0.M1")
    testImplementation("io.kotest:kotest-assertions-core:6.0.0.M1")
    testImplementation("io.kotest:kotest-property:6.0.0.M1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

    // Fixture Monkey
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.1.4")

    // Spring Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

## 테스트 파일 구조

```
src/test/kotlin/com/monkeys/{module}/
├── service/
│   ├── {Domain}ServiceTest.kt      # 단위 테스트
│   └── {Domain}ServiceIntegrationTest.kt  # 통합 테스트
├── repository/
│   └── {Entity}RepositoryTest.kt   # Repository 테스트
└── fixture/
    └── {Entity}Fixtures.kt         # 테스트 데이터 생성
```

## Kotest 스타일

### BehaviorSpec (권장)

```kotlin
class BookServiceTest : BehaviorSpec({

    Given("도서가 존재할 때") {
        val book = createBook(title = "클린 코드", status = BookStatus.AVAILABLE)

        When("대출을 요청하면") {
            val result = bookService.borrow(book.id, "user@example.com")

            Then("대출이 성공한다") {
                result.shouldNotBeNull()
                result.status shouldBe LoanStatus.ACTIVE
            }

            Then("도서 상태가 BORROWED로 변경된다") {
                book.status shouldBe BookStatus.BORROWED
            }
        }

        When("이미 대출 중인 도서를 대출 요청하면") {
            book.status = BookStatus.BORROWED

            Then("대출이 실패한다") {
                val result = bookService.borrow(book.id, "other@example.com")
                result.shouldBeNull()
            }
        }
    }
})
```

### FunSpec (간단한 테스트)

```kotlin
class ValidationUtilsTest : FunSpec({

    test("빈 문자열은 검증 실패") {
        shouldThrow<IllegalArgumentException> {
            ValidationUtils.requireNotBlank("", "필드명")
        }
    }

    test("유효한 이메일은 검증 통과") {
        val result = ValidationUtils.validateEmail("test@example.com")
        result shouldBe "test@example.com"
    }
})
```

### DescribeSpec (그룹화)

```kotlin
class ProductServiceTest : DescribeSpec({

    describe("상품 검색") {
        it("키워드로 검색하면 일치하는 상품 반환") {
            // ...
        }

        it("존재하지 않는 키워드는 빈 목록 반환") {
            // ...
        }
    }

    describe("재고 관리") {
        context("재고 추가 시") {
            it("수량이 증가한다") {
                // ...
            }
        }

        context("재고 차감 시") {
            it("충분한 재고가 있으면 성공") {
                // ...
            }

            it("재고 부족 시 실패") {
                // ...
            }
        }
    }
})
```

## Fixture Monkey 사용

### 기본 사용법

```kotlin
class ProductFixtures {
    companion object {
        private val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

        fun createProduct(
            name: String = fixtureMonkey.giveMeOne(),
            price: BigDecimal = BigDecimal("10000"),
            status: ProductStatus = ProductStatus.ACTIVE
        ): Product {
            return fixtureMonkey.giveMeBuilder<Product>()
                .set("name", name)
                .set("price", price)
                .set("status", status)
                .sample()
        }

        fun createProducts(count: Int): List<Product> {
            return fixtureMonkey.giveMe<Product>(count)
        }
    }
}
```

### 복잡한 객체 생성

```kotlin
fun createEmployee(
    name: String = "홍길동",
    department: Department? = null,
    status: EmployeeStatus = EmployeeStatus.ACTIVE
): Employee {
    return fixtureMonkey.giveMeBuilder<Employee>()
        .set("name", name)
        .set("department", department ?: createDepartment())
        .set("status", status)
        .set("hireDate", LocalDate.now().minusYears(1))
        .sample()
}

fun createDepartment(
    name: String = "개발팀",
    code: String = "DEV"
): Department {
    return fixtureMonkey.giveMeBuilder<Department>()
        .set("name", name)
        .set("code", code)
        .sample()
}
```

## Spring 통합 테스트

### @SpringBootTest

```kotlin
@SpringBootTest
class BookServiceIntegrationTest(
    private val bookService: BookService,
    private val bookRepository: BookRepository
) : BehaviorSpec({

    beforeEach {
        bookRepository.deleteAll()
    }

    Given("도서가 데이터베이스에 저장되어 있을 때") {
        val book = bookRepository.save(createBook())

        When("ID로 조회하면") {
            val found = bookService.findById(book.id)

            Then("도서가 반환된다") {
                found.shouldNotBeNull()
                found.id shouldBe book.id
            }
        }
    }
})
```

### @DataJpaTest (Repository 테스트)

```kotlin
@DataJpaTest
class ProductRepositoryTest(
    private val productRepository: ProductRepository
) : FunSpec({

    test("상태별 상품 수 조회") {
        // Given
        productRepository.saveAll(listOf(
            createProduct(status = ProductStatus.ACTIVE),
            createProduct(status = ProductStatus.ACTIVE),
            createProduct(status = ProductStatus.INACTIVE)
        ))

        // When
        val count = productRepository.countByStatus(ProductStatus.ACTIVE)

        // Then
        count shouldBe 2
    }
})
```

## Kotest Assertions

### 기본 Assertions

```kotlin
// 동등성
result shouldBe expected
result shouldNotBe unexpected

// Null 체크
result.shouldNotBeNull()
result.shouldBeNull()

// 컬렉션
list shouldHaveSize 3
list shouldContain item
list.shouldBeEmpty()

// 예외
shouldThrow<IllegalArgumentException> {
    // 예외 발생 코드
}

// 범위
value shouldBeInRange 1..10
value shouldBeGreaterThan 0
```

### 커스텀 Matchers

```kotlin
// 도메인 특화 검증
infix fun Product.shouldBeAvailable(expected: Boolean) {
    this.isAvailable() shouldBe expected
}

// 사용
product shouldBeAvailable true
```

## 테스트 명명 규칙

```kotlin
// Given-When-Then 패턴 (BehaviorSpec)
Given("조건") {
    When("동작") {
        Then("결과") { }
    }
}

// 한글 서술형 (FunSpec)
test("빈 문자열을 입력하면 검증 실패한다") { }
test("유효한 이메일 형식이면 검증 통과한다") { }

// describe-it 패턴 (DescribeSpec)
describe("상품 검색 기능") {
    it("키워드가 포함된 상품을 반환한다") { }
}
```

## 테스트 체크리스트

- [ ] Happy Path 테스트 작성
- [ ] Edge Case 테스트 작성 (null, 빈 값, 경계값)
- [ ] 예외 상황 테스트 작성
- [ ] 테스트 데이터 독립성 보장 (beforeEach에서 초기화)
- [ ] 테스트 이름이 동작을 명확히 설명하는가?
