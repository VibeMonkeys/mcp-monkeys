package com.monkeys.product.service

import com.monkeys.product.entity.*
import com.monkeys.product.repository.CategoryRepository
import com.monkeys.product.repository.InventoryRepository
import com.monkeys.product.repository.ProductRepository
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
class ProductServiceTest : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)
    override fun isolationMode() = IsolationMode.InstancePerTest

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var inventoryRepository: InventoryRepository

    private val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    init {
        describe("상품 조회") {
            context("이름으로 검색할 때") {
                it("해당 이름을 포함하는 상품 목록을 반환한다") {
                    // when
                    val result = productService.searchProducts("갤럭시")

                    // then
                    result.shouldNotBeEmpty()
                    result.forEach {
                        it.name.contains("갤럭시") shouldBe true
                    }
                }
            }

            context("SKU로 조회할 때") {
                it("해당 SKU의 상품을 반환한다") {
                    // when
                    val result = productService.findProductBySku("PHONE-001")

                    // then
                    result.shouldNotBeNull()
                    result.sku shouldBe "PHONE-001"
                }

                it("존재하지 않는 SKU이면 null을 반환한다") {
                    // when
                    val result = productService.findProductBySku("INVALID-SKU")

                    // then
                    result.shouldBeNull()
                }
            }

            context("카테고리로 조회할 때") {
                it("해당 카테고리의 상품 목록을 반환한다") {
                    // given
                    val category = categoryRepository.findByCode("PHONE").orElse(null)

                    // when
                    val result = productService.findProductsByCategory(category.id)

                    // then
                    result.shouldNotBeEmpty()
                }
            }

            context("브랜드로 조회할 때") {
                it("해당 브랜드의 상품 목록을 반환한다") {
                    // when
                    val result = productService.findProductsByBrand("Apple")

                    // then
                    result.shouldNotBeEmpty()
                    result.forEach {
                        it.brand?.lowercase() shouldBe "apple"
                    }
                }
            }
        }

        describe("재고 관리") {
            context("재고를 추가할 때") {
                it("재고가 증가한다") {
                    // given
                    val product = productService.findProductBySku("PHONE-001")!!
                    val initialQuantity = product.inventory?.quantity ?: 0

                    // when
                    val result = productService.addStock(product.id, 10)

                    // then
                    result.shouldNotBeNull()
                    result.quantity shouldBe initialQuantity + 10
                }
            }

            context("재고를 차감할 때") {
                it("재고가 충분하면 차감된다") {
                    // given
                    val product = productService.findProductBySku("PHONE-001")!!

                    // when
                    val result = productService.removeStock(product.id, 5)

                    // then
                    result.shouldNotBeNull()
                }

                it("재고가 부족하면 null을 반환한다") {
                    // given
                    val product = productService.findProductBySku("PHONE-001")!!

                    // when
                    val result = productService.removeStock(product.id, 10000)

                    // then
                    result.shouldBeNull()
                }
            }
        }

        describe("상품 상태 관리") {
            context("상품을 비활성화할 때") {
                it("상태가 INACTIVE로 변경된다") {
                    // given
                    val activeProduct = productService.findActiveProducts().first()

                    // when
                    val result = productService.deactivateProduct(activeProduct.id)

                    // then
                    result.shouldNotBeNull()
                    result.status shouldBe ProductStatus.INACTIVE
                }
            }
        }

        describe("카테고리 조회") {
            it("최상위 카테고리를 조회한다") {
                // when
                val result = productService.findTopLevelCategories()

                // then
                result.shouldNotBeEmpty()
                result.forEach { it.isTopLevel() shouldBe true }
            }

            it("하위 카테고리를 조회한다") {
                // given
                val topCategory = productService.findTopLevelCategories().first()

                // when
                val result = productService.findSubCategories(topCategory.id)

                // then
                result.shouldNotBeEmpty()
            }
        }

        describe("재고 부족 상품 조회") {
            it("재고 부족 상품 목록을 반환한다") {
                // when
                val result = productService.findLowStockProducts()

                // then
                // 초기 데이터에 재고 부족 상품이 있음
                result.forEach { product ->
                    val inventory = product.inventory
                    inventory.shouldNotBeNull()
                    inventory.isLowStock() shouldBe true
                }
            }
        }

        describe("통계 조회") {
            it("상품 통계를 반환한다") {
                // when
                val result = productService.getProductStats()

                // then
                result.totalProducts shouldBeGreaterThan 0
                result.totalCategories shouldBeGreaterThan 0
            }
        }
    }
}
