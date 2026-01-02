package com.monkeys.library.adapter.outbound.persistence.entity

import com.monkeys.library.domain.model.Book
import com.monkeys.library.domain.model.BookStatus
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "books")
class BookEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, unique = true, length = 20)
    var isbn: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: AuthorEntity,

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
    val loans: MutableList<LoanEntity> = mutableListOf()
) {
    fun toDomain() = Book(
        id = id,
        title = title,
        isbn = isbn,
        author = author.toDomain(),
        publisher = publisher,
        publishedDate = publishedDate,
        category = category,
        description = description,
        totalCopies = totalCopies,
        availableCopies = availableCopies,
        status = status
    )

    fun updateFrom(book: Book) {
        this.title = book.title
        this.isbn = book.isbn
        this.publisher = book.publisher
        this.publishedDate = book.publishedDate
        this.category = book.category
        this.description = book.description
        this.totalCopies = book.totalCopies
        this.availableCopies = book.availableCopies
        this.status = book.status
    }
}
