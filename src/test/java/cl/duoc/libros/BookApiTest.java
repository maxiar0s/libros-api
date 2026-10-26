package cl.duoc.libros;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookApiTest {
    private static final String BOOK = """
            {"title":"Cien años de soledad","author":"Gabriel García Márquez",
             "publicationYear":1967,"genre":"Novel"}
            """;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void clearTestDatabase() {
        jdbc.update("DELETE FROM books");
    }

    @Test
    void createsAndRetrievesAllBookAttributes() throws Exception {
        long id = createBook();
        mvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Cien años de soledad"))
                .andExpect(jsonPath("$.author").value("Gabriel García Márquez"))
                .andExpect(jsonPath("$.publicationYear").value(1967))
                .andExpect(jsonPath("$.genre").value("Novel"));
        assertThat(jdbc.queryForObject("SELECT title FROM books WHERE id = ?", String.class, id))
                .isEqualTo("Cien años de soledad");
    }

    @Test
    void updatesExistingBookAndPersistsEveryAttribute() throws Exception {
        long id = createBook();
        String update = """
                {"title":"The Hobbit","author":"J. R. R. Tolkien",
                 "publicationYear":1937,"genre":"Fantasy"}
                """;
        mvc.perform(put("/api/books/{id}", id).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Hobbit"))
                .andExpect(jsonPath("$.author").value("J. R. R. Tolkien"))
                .andExpect(jsonPath("$.publicationYear").value(1937))
                .andExpect(jsonPath("$.genre").value("Fantasy"));
        assertThat(jdbc.queryForObject("SELECT title FROM books WHERE id = ?", String.class, id))
                .isEqualTo("The Hobbit");
        assertThat(countBooks()).isEqualTo(1);
    }

    @Test
    void deletesBookFromDatabase() throws Exception {
        long id = createBook();
        mvc.perform(delete("/api/books/{id}", id))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/api/books/{id}", id)).andExpect(status().isNotFound());
        assertThat(countBooks()).isZero();
    }

    @Test
    void missingBookReturns404WithoutCreatingOrDeletingOtherRecords() throws Exception {
        long existingId = createBook();
        mvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Book 999999 was not found"));
        mvc.perform(put("/api/books/999999").contentType(MediaType.APPLICATION_JSON).content(BOOK))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/books/999999")).andExpect(status().isNotFound());
        assertThat(countBooks()).isEqualTo(1);
        mvc.perform(get("/api/books/{id}", existingId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Cien años de soledad"));
    }

    @Test
    void rejectsInvalidInputWithoutChangingData() throws Exception {
        long id = createBook();
        String invalid = """
                {"title":" ","author":"","publicationYear":null,"genre":""}
                """;
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.title").exists());
        mvc.perform(put("/api/books/{id}", id).contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Cien años de soledad"));
        assertThat(countBooks()).isEqualTo(1);
    }

    private long createBook() throws Exception {
        String response = mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(BOOK))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        long id = mapper.readTree(response).get("id").asLong();
        assertThat(id).isPositive();
        return id;
    }

    private long countBooks() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM books", Long.class);
    }
}
