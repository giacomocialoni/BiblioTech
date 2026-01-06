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

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DatabaseBookDAO.class);
    
    private final DBConnection dbConnection;
    private static final String TITLE = "title";
    private static final String[] BOOK_COLUMNS = {
        "id", "title", "author", "category", "year", "publisher", 
        "pages", "isbn", "stock", "plot", "image_path", "price"
    };

    public DatabaseBookDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<Book> getAllBooks() throws DAOException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT " + String.join(", ", BOOK_COLUMNS) + " FROM books ORDER BY title";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }
            return books;

        } catch (SQLException e) {
            LOGGER.error("Errore durante il recupero di tutti i libri", e);
            throw new DAOException("Errore durante il recupero di tutti i libri", e);
        }
    }

    @Override
    public Book getBookById(int id) throws DAOException {
        String sql = "SELECT " + String.join(", ", BOOK_COLUMNS) + " FROM books WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractBookFromResultSet(rs);
            } else {
                throw new RecordNotFoundException("Libro con ID " + id + " non trovato.");
            }

        } catch (SQLException e) {
            LOGGER.error("Errore durante il caricamento del libro con ID {}", id, e);
            throw new DAOException("Errore durante il caricamento del libro con ID " + id, e);
        }
    }

    @Override
    public void addBook(Book book) throws DAOException {
        String sql = "INSERT INTO books (title, author, category, year, publisher, pages, isbn, stock, plot, image_path, price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            fillBookPreparedStatement(stmt, book);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            if (isDuplicateError(e)) {
                String duplicateType = detectDuplicateType(e, book);
                throw new DuplicateBookException(book.getIsbn(), book.getTitle(), duplicateType);
            }
            LOGGER.error("Errore durante l'aggiunta del libro: {}", book.getTitle(), e);
            throw new DAOException("Errore durante l'aggiunta del libro: " + book.getTitle(), e);
        }
    }
    
    private boolean isDuplicateError(SQLException e) {
        return e.getErrorCode() == 1062 || 
               e.getMessage() != null && 
               e.getMessage().contains("Duplicate entry");
    }
    
    private String detectDuplicateType(SQLException e, Book book) {
        if (e.getMessage() == null) return "unknown";
        
        String message = e.getMessage().toLowerCase();
        if (message.contains("isbn")) {
            return "isbn";
        }
        return TITLE;
    }

    @Override
    public void updateBook(Book book) throws DAOException {
        String sql = "UPDATE books SET title=?, author=?, category=?, year=?, publisher=?, pages=?, isbn=?, stock=?, plot=?, image_path=?, price=? WHERE id=?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            fillBookPreparedStatement(stmt, book);
            stmt.setInt(12, book.getId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RecordNotFoundException("Impossibile aggiornare: nessun libro con ID " + book.getId());
            }

        } catch (SQLException e) {
            LOGGER.error("Errore durante l'aggiornamento del libro ID {}", book.getId(), e);
            throw new DAOException("Errore durante l'aggiornamento del libro ID " + book.getId(), e);
        }
    }

    @Override
    public void deleteBook(int id) throws DAOException {
        String sql = "DELETE FROM books WHERE id=?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new RecordNotFoundException("Nessun libro trovato con ID " + id + " da eliminare.");
            }

        } catch (SQLException e) {
            LOGGER.error("Errore durante l'eliminazione del libro ID {}", id, e);
            throw new DAOException("Errore durante l'eliminazione del libro ID " + id, e);
        }
    }

    @Override
    public List<Book> searchBooks(String searchText, String searchMode, String category,
                                  String yearFrom, String yearTo, boolean includeUnavailable)
            throws DAOException {

        List<Book> books = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " + String.join(", ", BOOK_COLUMNS) + " FROM books WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (searchText != null && !searchText.trim().isEmpty()) {
            if (TITLE.equalsIgnoreCase(searchMode)) {
                sql.append("AND LOWER(title) LIKE ? ");
                params.add("%" + searchText.toLowerCase() + "%");
            } else if ("author".equalsIgnoreCase(searchMode)) {
                sql.append("AND LOWER(author) LIKE ? ");
                params.add("%" + searchText.toLowerCase() + "%");
            }
        }

        if (category != null && !category.trim().isEmpty()) {
            sql.append("AND category = ? ");
            params.add(category);
        }

        if (yearFrom != null && !yearFrom.trim().isEmpty()) {
            sql.append("AND year >= ? ");
            params.add(Integer.parseInt(yearFrom));
        }
        
        if (yearTo != null && !yearTo.trim().isEmpty()) {
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
    public void updateStock(int bookId, int quantity) throws DAOException {
        String sql = "UPDATE books SET stock = stock + ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error("Errore durante l'aggiornamento dello stock per il libro ID {}", bookId, e);
            throw new DAOException("Errore durante l'aggiornamento dello stock per il libro ID " + bookId, e);
        }
    }

    @Override
    public boolean isBookAvailable(int bookId) throws DAOException {
        String sql = "SELECT stock FROM books WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("stock") > 0;
            }
            return false;

        } catch (SQLException e) {
            LOGGER.error("Errore durante il controllo disponibilità del libro ID {}", bookId, e);
            throw new DAOException("Errore durante il controllo disponibilità del libro ID " + bookId, e);
        }
    }

    @Override
    public List<Book> getBooksByCategory(String category) throws DAOException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT " + String.join(", ", BOOK_COLUMNS) + " FROM books WHERE category = ? ORDER BY title";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }
            
            return books;

        } catch (SQLException e) {
            LOGGER.error("Errore durante il recupero dei libri per categoria {}", category, e);
            throw new DAOException("Errore durante il recupero dei libri per categoria " + category, e);
        }
    }

    @Override
    public List<Book> getBooksByAuthor(String author) throws DAOException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT " + String.join(", ", BOOK_COLUMNS) + " FROM books WHERE author LIKE ? ORDER BY year DESC, title";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + author + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }
            
            return books;

        } catch (SQLException e) {
            LOGGER.error("Errore durante il recupero dei libri per autore {}", author, e);
            throw new DAOException("Errore durante il recupero dei libri per autore " + author, e);
        }
    }

    @Override
    public List<Book> getAvailableBooks() throws DAOException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT " + String.join(", ", BOOK_COLUMNS) + " FROM books WHERE stock > 0 ORDER BY title";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(extractBookFromResultSet(rs));
            }
            
            return books;

        } catch (SQLException e) {
            LOGGER.error("Errore durante il recupero dei libri disponibili", e);
            throw new DAOException("Errore durante il recupero dei libri disponibili", e);
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
    
}