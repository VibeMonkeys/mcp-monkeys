package com.monkeys.library.adapter.outbound.persistence.entity

import com.monkeys.library.domain.model.Author
import jakarta.persistence.*

@Entity
@Table(name = "authors")
class AuthorEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 50)
    var nationality: String? = null,

    @Column(length = 2000)
    var biography: String? = null,

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val books: MutableList<BookEntity> = mutableListOf()
) {
    fun toDomain() = Author(
        id = id,
        name = name,
        nationality = nationality,
        biography = biography,
        bookCount = books.size
    )

    companion object {
        fun fromDomain(author: Author) = AuthorEntity(
            id = author.id,
            name = author.name,
            nationality = author.nationality,
            biography = author.biography
        )
    }
}
