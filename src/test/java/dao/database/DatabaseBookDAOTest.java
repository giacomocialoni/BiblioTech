package dao.database;

import static org.junit.jupiter.api.Assertions.*;

import dao.BookDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Book;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public class DatabaseBookDAOTest {

    private static DBConnection dbConnection;
    private BookDAO bookDAO;

    private static final String URL = "jdbc:mysql://localhost:3306/library_test";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    @BeforeAll
    static void setupDatabase() throws Exception {
        dbConnection = new DBConnection(URL, USER, PASSWORD);

        // pulizia tabella
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM books");
        }
    }

    @BeforeEach
    void setUp() {
        bookDAO = new DatabaseBookDAO(dbConnection);
    }

    @Test
    void testAddAndGetBookById() throws Exception {
        Book book = new Book(
            0,
            "Dune",
            "Frank Herbert",
            "Sci-Fi",
            1965,
            "Ace Books",
            500,
            "1234567890",
            3,
            "Epic sci-fi novel",
            "dune.jpg",
            19.99
        );

        bookDAO.addBook(book);

        assertTrue(book.getId() > 0);

        Book loaded = bookDAO.getBookById(book.getId());

        assertEquals("Dune", loaded.getTitle());
        assertEquals("Frank Herbert", loaded.getAuthor());
        assertEquals(3, loaded.getStock());
    }

    @Test
    void testGetBookByIdNotFound() {
        assertThrows(
            RecordNotFoundException.class,
            () -> bookDAO.getBookById(9999)
        );
    }

    @Test
    void testGetAvailableBooks() throws DAOException {
        List<Book> books = bookDAO.getAvailableBooks();

        // dopo addBook del test precedente ci aspettiamo almeno 1 libro
        assertNotNull(books);
        assertTrue(books.size() >= 1);

        assertTrue(
            books.stream().allMatch(b -> b.getStock() > 0)
        );
    }

    @AfterAll
    static void tearDown() {
        if (dbConnection != null) {
            dbConnection.close();
        }
    }
}