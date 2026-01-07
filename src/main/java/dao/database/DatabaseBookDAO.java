package dao.database;

import dao.BookDAO;
import exception.DAOException;
import exception.DuplicateBookException;
import exception.RecordNotFoundException;
import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBookDAO implements BookDAO {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(DatabaseBookDAO.class);

    private final DBConnection dbConnection;

    private static final String TITLE = "title";

    private static final String[] BOOK_COLUMNS = {
            "id", "title", "author", "category", "year", "publisher",
            "pages", "isbn", "stock", "plot", "image_path", "price"
    };

    private static final String BOOK_COLUMNS_JOINED =
            String.join(", ", BOOK_COLUMNS);

    /* ======================= SQL CONSTANTS ======================= */

    private static final String SELECT_ALL_BOOKS =
            "SELECT " + BOOK_COLUMNS_JOINED + " FROM books ORDER BY title";

    private static final String SELECT_BOOK_BY_ID =
            "SELECT " + BOOK_COLUMNS_JOINED + " FROM books WHERE id = ?";

    private static final String SELECT_AVAILABLE_BOOKS =
            "SELECT " + BOOK_COLUMNS_JOINED + " FROM books WHERE stock > 0 ORDER BY title";

    private static final String SELECT_BOOKS_BY_CATEGORY =
            "SELECT " + BOOK_COLUMNS_JOINED + " FROM books WHERE category = ? ORDER BY title";

    private static final String SELECT_BOOKS_BY_AUTHOR =
            "SELECT " + BOOK_COLUMNS_JOINED + " FROM books WHERE author LIKE ? ORDER BY year DESC, title";

    private static final String INSERT_BOOK =
            "INSERT INTO books (title, author, category, year, publisher, pages, isbn, stock, plot, image_path, price) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_BOOK =
            "UPDATE books SET title=?, author=?, category=?, year=?, publisher=?, pages=?, isbn=?, stock=?, plot=?, image_path=?, price=? WHERE id=?";

    private static final String DELETE_BOOK =
            "DELETE FROM books WHERE id=?";

    private static final String UPDATE_STOCK =
            "UPDATE books SET stock = stock + ? WHERE id = ?";

    private static final String CHECK_AVAILABILITY =
            "SELECT stock FROM books WHERE id = ?";

    /* ============================================================= */

    public DatabaseBookDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<Book> getAllBooks() throws DAOException {
        return executeBookListQuery(SELECT_ALL_BOOKS, null);
    }

    @Override
    public Book getBookById(int id) throws DAOException {
        return executeSingleBookQuery(SELECT_BOOK_BY_ID, stmt -> stmt.setInt(1, id),
                "Libro con ID " + id + " non trovato");
    }

    @Override
    public List<Book> searchBooks(String searchText,
                                  String searchMode,
                                  String category,
                                  String yearFrom,
                                  String yearTo,
                                  boolean includeUnavailable) throws DAOException {

        List<Book> books = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT " + BOOK_COLUMNS_JOINED + " FROM books WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (searchText != null && !searchText.isBlank()) {
            if (TITLE.equalsIgnoreCase(searchMode)) {
                sql.append("AND LOWER(title) LIKE ? ");
                params.add("%" + searchText.toLowerCase() + "%");
            } else if ("author".equalsIgnoreCase(searchMode)) {
                sql.append("AND LOWER(author) LIKE ? ");
                params.add("%" + searchText.toLowerCase() + "%");
            }
        }

        if (category != null && !category.isBlank()) {
            sql.append("AND category = ? ");
            params.add(category);
        }

        if (yearFrom != null && !yearFrom.isBlank()) {
            sql.append("AND year >= ? ");
            params.add(Integer.parseInt(yearFrom));
        }

        if (yearTo != null && !yearTo.isBlank()) {
            sql.append("AND year <= ? ");
            params.add(Integer.parseInt(yearTo));
        }

        if (!includeUnavailable) {
            sql.append("AND stock > 0 ");
        }

        sql.append("ORDER BY title");

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }

            return books;

        } catch (SQLException | NumberFormatException e) {
            LOGGER.error("Errore durante la ricerca dei libri", e);
            throw new DAOException("Errore durante la ricerca dei libri", e);
        }
    }
    
    @Override
    public void addBook(Book book) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_BOOK, Statement.RETURN_GENERATED_KEYS)) {

            fillBookPreparedStatement(stmt, book);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            if (isDuplicateError(e)) {
                throw new DuplicateBookException(book.getIsbn(), book.getTitle(), detectDuplicateType(e));
            }
            throw new DAOException("Errore durante l'aggiunta del libro " + book.getTitle(), e);
        }
    }

    @Override
    public void updateBook(Book book) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_BOOK)) {

            fillBookPreparedStatement(stmt, book);
            stmt.setInt(12, book.getId());

            if (stmt.executeUpdate() == 0) {
                throw new RecordNotFoundException("Nessun libro con ID " + book.getId());
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiornamento del libro ID " + book.getId(), e);
        }
    }

    @Override
    public void deleteBook(int id) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BOOK)) {

            stmt.setInt(1, id);

            if (stmt.executeUpdate() == 0) {
                throw new RecordNotFoundException("Libro con ID " + id + " non trovato");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'eliminazione del libro ID " + id, e);
        }
    }

    @Override
    public boolean isBookAvailable(int bookId) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CHECK_AVAILABILITY)) {

            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt("stock") > 0;

        } catch (SQLException e) {
            throw new DAOException("Errore controllo disponibilità libro ID " + bookId, e);
        }
    }

    @Override
    public void updateStock(int bookId, int quantity) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STOCK)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore aggiornamento stock libro ID " + bookId, e);
        }
    }

    @Override
    public List<Book> getBooksByCategory(String category) throws DAOException {
        return executeBookListQuery(SELECT_BOOKS_BY_CATEGORY,
                stmt -> stmt.setString(1, category));
    }

    @Override
    public List<Book> getBooksByAuthor(String author) throws DAOException {
        return executeBookListQuery(SELECT_BOOKS_BY_AUTHOR,
                stmt -> stmt.setString(1, "%" + author + "%"));
    }

    @Override
    public List<Book> getAvailableBooks() throws DAOException {
        return executeBookListQuery(SELECT_AVAILABLE_BOOKS, null);
    }

    /* ======================= HELPERS ======================= */

    private List<Book> executeBookListQuery(String sql, StatementFiller filler) throws DAOException {
        List<Book> books = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (filler != null) {
                filler.fill(stmt);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }
            return books;

        } catch (SQLException e) {
            throw new DAOException("Errore esecuzione query libri", e);
        }
    }

    private Book executeSingleBookQuery(String sql, StatementFiller filler, String notFoundMsg)
            throws DAOException {

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            filler.fill(stmt);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractBookFromResultSet(rs);
            }
            throw new RecordNotFoundException(notFoundMsg);

        } catch (SQLException e) {
            throw new DAOException("Errore recupero libro", e);
        }
    }

    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString(TITLE),
                rs.getString("author"),
                rs.getString("category"),
                rs.getInt("year"),
                rs.getString("publisher"),
                rs.getInt("pages"),
                rs.getString("isbn"),
                rs.getInt("stock"),
                rs.getString("plot"),
                rs.getString("image_path"),
                rs.getDouble("price")
        );
    }

    private void fillBookPreparedStatement(PreparedStatement stmt, Book book) throws SQLException {
        stmt.setString(1, book.getTitle());
        stmt.setString(2, book.getAuthor());
        stmt.setString(3, book.getCategory());
        stmt.setInt(4, book.getYear());
        stmt.setString(5, book.getPublisher());
        stmt.setInt(6, book.getPages());
        stmt.setString(7, book.getIsbn());
        stmt.setInt(8, book.getStock());
        stmt.setString(9, book.getPlot());
        stmt.setString(10, book.getImagePath());
        stmt.setDouble(11, book.getPrice());
    }

    private boolean isDuplicateError(SQLException e) {
        return e.getErrorCode() == 1062;
    }

    private String detectDuplicateType(SQLException e) {
        return e.getMessage() != null && e.getMessage().toLowerCase().contains("isbn")
                ? "isbn" : TITLE;
    }

    @FunctionalInterface
    private interface StatementFiller {
        void fill(PreparedStatement stmt) throws SQLException;
    }
}