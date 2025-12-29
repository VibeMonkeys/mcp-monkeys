package com.monkeys.client.dto

import com.fasterxml.jackson.annotation.JsonPropertyDescription

// 도서관 관련 응답
data class LibraryResponse(
    @JsonPropertyDescription("검색된 도서 목록")
    val books: List<BookInfo>,

    @JsonPropertyDescription("검색 키워드")
    val searchQuery: String,

    @JsonPropertyDescription("총 도서 수")
    val totalCount: Int,

    @JsonPropertyDescription("요약")
    val summary: String? = null
)

data class BookInfo(
    @JsonPropertyDescription("도서 제목")
    val title: String,

    @JsonPropertyDescription("저자")
    val author: String,

    @JsonPropertyDescription("ISBN")
    val isbn: String,

    @JsonPropertyDescription("대출 가능 여부")
    val available: Boolean
)

// 할일 관련 응답
data class TodoResponse(
    @JsonPropertyDescription("할일 목록")
    val todos: List<TodoInfo>,

    @JsonPropertyDescription("총 할일 수")
    val totalCount: Int,

    @JsonPropertyDescription("완료된 할일 수")
    val completedCount: Int,

    @JsonPropertyDescription("요약")
    val summary: String? = null
)

data class TodoInfo(
    @JsonPropertyDescription("할일 제목")
    val title: String,

    @JsonPropertyDescription("상태")
    val status: String,

    @JsonPropertyDescription("우선순위")
    val priority: String,

    @JsonPropertyDescription("마감일")
    val dueDate: String? = null
)

// 직원 관련 응답
data class EmployeeResponse(
    @JsonPropertyDescription("직원 목록")
    val employees: List<EmployeeInfo>,

    @JsonPropertyDescription("총 직원 수")
    val totalCount: Int,

    @JsonPropertyDescription("부서명")
    val department: String? = null,

    @JsonPropertyDescription("요약")
    val summary: String? = null
)

data class EmployeeInfo(
    @JsonPropertyDescription("직원 이름")
    val name: String,

    @JsonPropertyDescription("사번")
    val employeeNumber: String,

    @JsonPropertyDescription("부서")
    val department: String,

    @JsonPropertyDescription("직급")
    val position: String
)

// 상품 관련 응답
data class ProductResponse(
    @JsonPropertyDescription("상품 목록")
    val products: List<ProductInfo>,

    @JsonPropertyDescription("총 상품 수")
    val totalCount: Int,

    @JsonPropertyDescription("카테고리")
    val category: String? = null,

    @JsonPropertyDescription("요약")
    val summary: String? = null
)

data class ProductInfo(
    @JsonPropertyDescription("상품명")
    val name: String,

    @JsonPropertyDescription("SKU")
    val sku: String,

    @JsonPropertyDescription("가격")
    val price: Double,

    @JsonPropertyDescription("재고 수량")
    val stockQuantity: Int
)

// 복합 서비스 응답
data class MultiServiceResponse(
    @JsonPropertyDescription("수행한 작업들의 요약")
    val summary: String,

    @JsonPropertyDescription("사용된 서비스 목록")
    val usedServices: List<String>,

    @JsonPropertyDescription("각 서비스별 결과")
    val results: Map<String, Any>,

    @JsonPropertyDescription("전체 작업 성공 여부")
    val success: Boolean,

    @JsonPropertyDescription("추가 권장사항")
    val recommendations: List<String>? = null
)

// 에러 응답
data class ErrorResponse(
    @JsonPropertyDescription("에러 메시지")
    val message: String,

    @JsonPropertyDescription("에러 코드")
    val errorCode: String,

    @JsonPropertyDescription("실패한 서비스 이름")
    val failedService: String? = null,

    @JsonPropertyDescription("해결 방법 제안")
    val solution: String? = null
)
