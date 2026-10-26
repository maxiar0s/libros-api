package cl.duoc.libros;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.DriverManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OracleSchemaTest {
    @Test
    void seedsTwoBooksAndLeavesSequenceReadyForNewBooks() throws Exception {
        String url = "jdbc:h2:mem:seed_" + UUID.randomUUID() + ";MODE=Oracle";
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            ScriptUtils.executeSqlScript(connection,
                    new FileSystemResource("database/oracle-schema.sql"));
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery(
                         "SELECT id, title, author, publication_year, genre FROM books ORDER BY id")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getLong("id")).isEqualTo(1);
                assertThat(rows.getString("title")).isEqualTo("The Hobbit");
                assertThat(rows.getString("author")).isEqualTo("J. R. R. Tolkien");
                assertThat(rows.getInt("publication_year")).isEqualTo(1937);
                assertThat(rows.getString("genre")).isEqualTo("Fantasy");
                assertThat(rows.next()).isTrue();
                assertThat(rows.getLong("id")).isEqualTo(2);
                assertThat(rows.getString("title")).isEqualTo("Nineteen Eighty-Four");
                assertThat(rows.getString("author")).isEqualTo("George Orwell");
                assertThat(rows.getInt("publication_year")).isEqualTo(1949);
                assertThat(rows.getString("genre")).isEqualTo("Dystopian");
                assertThat(rows.next()).isFalse();
            }
            try (var statement = connection.createStatement();
                 var next = statement.executeQuery("SELECT books_seq.NEXTVAL FROM dual")) {
                assertThat(next.next()).isTrue();
                assertThat(next.getLong(1)).isEqualTo(3);
            }
        }
    }
}
