---
name: api-design-guide
description: |
  REST API 설계 가이드. 다음 상황에서 활성화:
  - "API 만들어줘", "엔드포인트 추가", "컨트롤러 작성"
  - "응답 형식", "에러 처리", "페이징 구현"
  - ApiResponse, DTO, Request/Response 클래스 작성 시
---

# API 설계 가이드

이 프로젝트의 REST API 설계 표준입니다.

## 표준 응답 형식

### ApiResponse 래퍼

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// 성공 응답
fun <T> success(data: T, message: String? = null) = ApiResponse(
    success = true,
    data = data,
    message = message
)

// 실패 응답
fun <T> failure(message: String) = ApiResponse<T>(
    success = false,
    message = message
)
```

### 응답 예시

```json
// 성공
{
  "success": true,
  "data": {
    "id": 1,
    "name": "상품명"
  },
  "message": null,
  "timestamp": 1703920000000
}

// 실패
{
  "success": false,
  "data": null,
  "message": "상품을 찾을 수 없습니다.",
  "timestamp": 1703920000000
}
```

## URL 설계 규칙

### RESTful 엔드포인트

```
# 컬렉션
GET    /api/products          # 목록 조회
POST   /api/products          # 생성

# 단일 리소스
GET    /api/products/{id}     # 단건 조회
PUT    /api/products/{id}     # 전체 수정
PATCH  /api/products/{id}     # 부분 수정
DELETE /api/products/{id}     # 삭제

# 하위 리소스
GET    /api/products/{id}/reviews
POST   /api/products/{id}/reviews

# 액션 (동사가 필요한 경우)
POST   /api/products/{id}/activate
POST   /api/products/{id}/deactivate
```

### 쿼리 파라미터

```
# 검색
GET /api/products?keyword=노트북

# 필터링
GET /api/products?status=ACTIVE&category=electronics

# 페이징
GET /api/products?page=0&size=20

# 정렬
GET /api/products?sort=createdAt,desc
```

## Controller 패턴

### 기본 구조

```kotlin
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun getProducts(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<List<ProductDto>> {
        val products = productService.search(keyword, page, size)
        return success(products.map { it.toDto() })
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ApiResponse<ProductDto> {
        val product = productService.findById(id)
            ?: return failure("상품을 찾을 수 없습니다.")
        return success(product.toDto())
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ApiResponse<ProductDto> {
        val product = productService.create(request)
        return success(product.toDto(), "상품이 생성되었습니다.")
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest
    ): ApiResponse<ProductDto> {
        val product = productService.update(id, request)
            ?: return failure("상품을 찾을 수 없습니다.")
        return success(product.toDto(), "상품이 수정되었습니다.")
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ApiResponse<Unit> {
        val deleted = productService.delete(id)
        return if (deleted) {
            success(Unit, "상품이 삭제되었습니다.")
        } else {
            failure("상품을 찾을 수 없습니다.")
        }
    }
}
```

## Request/Response DTO

### Request DTO (입력)

```kotlin
data class CreateProductRequest(
    @field:NotBlank(message = "상품명은 필수입니다")
    @field:Size(max = 100, message = "상품명은 100자 이하입니다")
    val name: String,

    @field:NotNull(message = "가격은 필수입니다")
    @field:Positive(message = "가격은 양수여야 합니다")
    val price: BigDecimal,

    @field:Size(max = 500, message = "설명은 500자 이하입니다")
    val description: String? = null
)

data class UpdateProductRequest(
    @field:Size(max = 100, message = "상품명은 100자 이하입니다")
    val name: String? = null,

    @field:Positive(message = "가격은 양수여야 합니다")
    val price: BigDecimal? = null,

    val description: String? = null
)
```

### Response DTO (출력)

```kotlin
data class ProductDto(
    val id: Long,
    val name: String,
    val price: String,  // 금액은 문자열로 (포맷팅)
    val description: String?,
    val status: String,
    val createdAt: String  // ISO 8601 형식
)

// Entity → DTO 변환
fun Product.toDto() = ProductDto(
    id = id,
    name = name,
    price = price.toString(),
    description = description,
    status = status.name,
    createdAt = createdAt.toString()
)
```

## 에러 처리

### GlobalExceptionHandler

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(e: MethodArgumentNotValidException): ApiResponse<Map<String, String>> {
        val errors = e.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "유효하지 않은 값") }
        return ApiResponse(
            success = false,
            data = errors,
            message = "입력값 검증 실패"
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(e: IllegalArgumentException): ApiResponse<Unit> {
        return failure(e.message ?: "잘못된 요청입니다.")
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ApiResponse<Unit> {
        logger.error("서버 오류 발생", e)
        return failure("서버 오류가 발생했습니다.")
    }
}
```

### HTTP 상태 코드

| 코드 | 상황 |
|------|------|
| 200 OK | 조회/수정 성공 |
| 201 Created | 생성 성공 |
| 204 No Content | 삭제 성공 (본문 없음) |
| 400 Bad Request | 잘못된 요청/검증 실패 |
| 401 Unauthorized | 인증 필요 |
| 403 Forbidden | 권한 없음 |
| 404 Not Found | 리소스 없음 |
| 500 Internal Server Error | 서버 오류 |

## 페이징 응답

### Page 응답 형식

```kotlin
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

fun <T, R> Page<T>.toPageResponse(transform: (T) -> R): PageResponse<R> {
    return PageResponse(
        content = content.map(transform),
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        hasNext = hasNext(),
        hasPrevious = hasPrevious()
    )
}
```

## API 문서화

### OpenAPI 어노테이션

```kotlin
@Operation(
    summary = "상품 검색",
    description = "키워드로 상품을 검색합니다"
)
@ApiResponses(
    ApiResponse(responseCode = "200", description = "검색 성공"),
    ApiResponse(responseCode = "400", description = "잘못된 파라미터")
)
@GetMapping
fun searchProducts(
    @Parameter(description = "검색 키워드")
    @RequestParam keyword: String
): ApiResponse<List<ProductDto>>
```

## 체크리스트

새 API 엔드포인트 추가 시:

- [ ] RESTful URL 규칙 준수
- [ ] 적절한 HTTP 메서드 사용
- [ ] Request DTO에 검증 어노테이션 추가
- [ ] Response DTO로 Entity 노출 방지
- [ ] 에러 응답 형식 통일
- [ ] 적절한 HTTP 상태 코드 반환
