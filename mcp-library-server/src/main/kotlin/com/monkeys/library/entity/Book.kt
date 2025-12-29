package com.monkeys.library.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "books")
class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, unique = true, length = 20)
    var isbn: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: Author,

    @Column(length = 100)
    var publisher: String? = null,

    @Column(name = "published_date")
    var publishedDate: LocalDate? = null,

    @Column(length = 50)
    var category: String? = null,

    @Column(length = 2000)
    var description: String? = null,

    @Column(name = "total_copies", nullable = false)
    var totalCopies: Int = 1,

    @Column(name = "available_copies", nullable = false)
    var availableCopies: Int = 1,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BookStatus = BookStatus.AVAILABLE,

    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val loans: MutableList<Loan> = mutableListOf()
) {
    fun isAvailable(): Boolean = availableCopies > 0 && status == BookStatus.AVAILABLE

    fun borrow(): Boolean {
        if (!isAvailable()) return false
        availableCopies--
        if (availableCopies == 0) {
            status = BookStatus.ALL_BORROWED
        }
        return true
    }

    fun returnBook() {
        availableCopies++
        if (availableCopies > 0) {
            status = BookStatus.AVAILABLE
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

enum class BookStatus {
    AVAILABLE,      // 대출 가능
    ALL_BORROWED,   // 전부 대출 중
    UNAVAILABLE     // 대출 불가 (분실, 폐기 등)
}
