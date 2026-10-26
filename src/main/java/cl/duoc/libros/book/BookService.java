package cl.duoc.libros.book;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public Book findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Book " + id + " was not found"));
    }

    @Transactional
    public Book create(BookRequest request) {
        return repository.save(new Book(request));
    }

    @Transactional
    public Book update(Long id, BookRequest request) {
        Book book = findById(id);
        book.update(request);
        return repository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
