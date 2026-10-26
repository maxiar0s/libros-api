package cl.duoc.libros.book;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "books_generator")
    @SequenceGenerator(name = "books_generator", sequenceName = "books_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Column(nullable = false, length = 100)
    private String genre;

    protected Book() {
    }

    Book(BookRequest request) {
        update(request);
    }

    void update(BookRequest request) {
        title = request.title();
        author = request.author();
        publicationYear = request.publicationYear();
        genre = request.genre();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public String getGenre() {
        return genre;
    }
}
