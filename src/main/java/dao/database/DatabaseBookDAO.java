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

    /* ======================= COLUMN CONSTANTS ======================= */
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String AUTHOR = "author";
    private static final String CATEGORY = "category";
    private static final String YEAR = "year";
    private static final String PUBLISHER = "publisher";
    private static final String PAGES = "pages";
    private static final String ISBN = "isbn";
    private static final String STOCK = "stock";
    private static final String PLOT = "plot";
    private static final String IMAGE_PATH = "image_path";
    private static final String PRICE = "price";

    private static final String BOOK_COLUMNS_JOINED = String.join(", ",
            ID, TITLE, AUTHOR, CATEGORY, YEAR, PUBLISHER,
            PAGES, ISBN, STOCK, PLOT, IMAGE_PATH, PRICE
    );

    /* ======================= SQL CONSTANTS ======================= */
    private static final String FROM_BOOKS = " FROM books";
    private static final String ORDER_BY_TITLE = " ORDER BY title";
    
    private static final String SELECT_ALL_BOOKS = 
            "SELECT " + BOOK_COLUMNS_JOINED + FROM_BOOKS + ORDER_BY_TITLE;
            
    private static final String SELECT_BOOK_BY_ID = 
            "SELECT " + BOOK_COLUMNS_JOINED + FROM_BOOKS + " WHERE id = ?";
            
    private static final String SELECT_AVAILABLE_BOOKS = 
            "SELECT " + BOOK_COLUMNS_JOINED + FROM_BOOKS + " WHERE stock > 0" + ORDER_BY_TITLE;
            
    private static final String SELECT_BOOKS_BY_CATEGORY = 
            "SELECT " + BOOK_COLUMNS_JOINED + FROM_BOOKS + " WHERE category = ?" + ORDER_BY_TITLE;
            
    private static final String SELECT_BOOKS_BY_AUTHOR = 
            "SELECT " + BOOK_COLUMNS_JOINED + FROM_BOOKS + " WHERE author LIKE ?" +
            " ORDER BY year DESC, title";

    private static final String INSERT_BOOK = """
            INSERT INTO books (title, author, category, year, publisher, 
                             pages, isbn, stock, plot, image_path, price) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
    private static final String UPDATE_BOOK = """
            UPDATE books 
            SET title=?, author=?, category=?, year=?, publisher=?, 
                pages=?, isbn=?, stock=?, plot=?, image_path=?, price=? 
            WHERE id=?
            """;
            
    private static final String DELETE_BOOK = "DELETE FROM books WHERE id = ?";
    
    private static final String UPDATE_STOCK = 
            "UPDATE books SET stock = stock + ? WHERE id = ?";
            
    private static final String CHECK_AVAILABILITY = 
            "SELECT stock FROM books WHERE id = ?";

    public DatabaseBookDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /* =======================
       READ OPERATIONS
       ======================= */

    @Override
    public List<Book> getAllBooks() throws DAOException {
        return executeBookListQuery(SELECT_ALL_BOOKS);
    }

    @Override
    public Book getBookById(int id) throws DAOException {
        String errorMessage = "Libro con ID " + id + " non trovato";
        return executeSingleBookQuery(SELECT_BOOK_BY_ID, stmt -> stmt.setInt(1, id), errorMessage);
    }

    @Override
    public List<Book> getBooksByCategory(String category) throws DAOException {
        return executeBookListQuery(SELECT_BOOKS_BY_CATEGORY, stmt -> stmt.setString(1, category));
    }

    @Override
    public List<Book> getBooksByAuthor(String author) throws DAOException {
        String searchPattern = "%" + author + "%";
        return executeBookListQuery(SELECT_BOOKS_BY_AUTHOR, stmt -> stmt.setString(1, searchPattern));
    }

    @Override
    public List<Book> getAvailableBooks() throws DAOException {
        return executeBookListQuery(SELECT_AVAILABLE_BOOKS);
    }

    @Override
    public boolean isBookAvailable(int bookId) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CHECK_AVAILABILITY)) {

            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(STOCK) > 0;

        } catch (SQLException e) {
            LOGGER.error("Errore controllo disponibilità libro ID {}", bookId, e);
            throw new DAOException("Errore controllo disponibilità libro ID " + bookId, e);
        }
    }

    /* =======================
       SEARCH OPERATION
       ======================= */

    @Override
    public List<Book> searchBooks(String searchText,
                                  String searchMode,
                                  String category,
                                  String yearFrom,
                                  String yearTo,
                                  boolean includeUnavailable) throws DAOException {

        SearchCriteria criteria = new SearchCriteria(searchText, searchMode, category, 
                                                    yearFrom, yearTo, includeUnavailable);
        
        String sql = buildSearchQuery(criteria);
        List<Object> params = buildSearchParameters(criteria);

        return executeBookListQuery(sql, stmt -> {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
        });
    }

    /* =======================
       WRITE OPERATIONS
       ======================= */

    @Override
    public void addBook(Book book) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_BOOK, Statement.RETURN_GENERATED_KEYS)) {

            setBookParameters(stmt, book);
            stmt.executeUpdate();
            setGeneratedId(stmt, book);

        } catch (SQLException e) {
            handleAddBookException(e, book);
        }
    }

    @Override
    public void updateBook(Book book) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_BOOK)) {

            setBookParameters(stmt, book);
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

    /* =======================
       PRIVATE HELPER METHODS
       ======================= */

    private List<Book> executeBookListQuery(String sql) throws DAOException {
        return executeBookListQuery(sql, null);
    }

    private List<Book> executeBookListQuery(String sql, StatementPreparer preparer) throws DAOException {
        List<Book> books = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (preparer != null) {
                preparer.prepare(stmt);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(extractBookFromResultSet(rs));
                }
            }

            return books;

        } catch (SQLException e) {
            LOGGER.error("Errore esecuzione query libri", e);
            throw new DAOException("Errore esecuzione query libri", e);
        }
    }

    private Book executeSingleBookQuery(String sql, StatementPreparer preparer, String notFoundMsg) 
            throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preparer.prepare(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookFromResultSet(rs);
                }
                throw new RecordNotFoundException(notFoundMsg);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore recupero libro", e);
        }
    }

    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        return Book.builder()
                .id(rs.getInt(ID))
                .title(rs.getString(TITLE))
                .author(rs.getString(AUTHOR))
                .category(rs.getString(CATEGORY))
                .year(rs.getInt(YEAR))
                .publisher(rs.getString(PUBLISHER))
                .pages(rs.getInt(PAGES))
                .isbn(rs.getString(ISBN))
                .stock(rs.getInt(STOCK))
                .plot(rs.getString(PLOT))
                .imagePath(rs.getString(IMAGE_PATH))
                .price(rs.getDouble(PRICE))
                .build();
    }

    private void setBookParameters(PreparedStatement stmt, Book book) throws SQLException {
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

    private void setGeneratedId(PreparedStatement stmt, Book book) throws SQLException {
        try (ResultSet keys = stmt.getGeneratedKeys()) {
            if (keys.next()) {
                book.setId(keys.getInt(1));
            }
        }
    }

    private void handleAddBookException(SQLException e, Book book) throws DAOException {
        if (isDuplicateError(e)) {
            String duplicateType = detectDuplicateType(e);
            throw new DuplicateBookException(book.getIsbn(), book.getTitle(), duplicateType);
        }
        LOGGER.error("Errore aggiunta libro {}", book.getTitle(), e);
        throw new DAOException("Errore durante l'aggiunta del libro " + book.getTitle(), e);
    }

    private boolean isDuplicateError(SQLException e) {
        return e.getErrorCode() == 1062;
    }

    private String detectDuplicateType(SQLException e) {
        return e.getMessage() != null && e.getMessage().toLowerCase().contains("isbn")
                ? ISBN : TITLE;
    }

    /* =======================
       SEARCH HELPER CLASSES
       ======================= */

    private String buildSearchQuery(SearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(BOOK_COLUMNS_JOINED).append(FROM_BOOKS).append(" WHERE 1=1 ");
        
        if (criteria.hasSearchText()) {
            sql.append("AND LOWER(")
               .append(criteria.searchMode)
               .append(") LIKE ? ");
        }
        
        if (criteria.hasCategory()) {
            sql.append("AND category = ? ");
        }
        
        if (criteria.hasYearFrom()) {
            sql.append("AND year >= ? ");
        }
        
        if (criteria.hasYearTo()) {
            sql.append("AND year <= ? ");
        }
        
        if (!criteria.includeUnavailable) {
            sql.append("AND stock > 0 ");
        }
        
        sql.append(ORDER_BY_TITLE);
        return sql.toString();
    }

    private List<Object> buildSearchParameters(SearchCriteria criteria) {
        List<Object> params = new ArrayList<>();
        
        if (criteria.hasSearchText()) {
            params.add("%" + criteria.searchText.toLowerCase() + "%");
        }
        
        if (criteria.hasCategory()) {
            params.add(criteria.category);
        }
        
        if (criteria.hasYearFrom()) {
            try {
                params.add(Integer.parseInt(criteria.yearFrom));
            } catch (NumberFormatException e) {
                LOGGER.warn("Valore yearFrom non valido: {}", criteria.yearFrom);
            }
        }
        
        if (criteria.hasYearTo()) {
            try {
                params.add(Integer.parseInt(criteria.yearTo));
            } catch (NumberFormatException e) {
                LOGGER.warn("Valore yearTo non valido: {}", criteria.yearTo);
            }
        }
        
        return params;
    }

    private static class SearchCriteria {
        private final String searchText;
        private final String searchMode;
        private final String category;
        private final String yearFrom;
        private final String yearTo;
        private final boolean includeUnavailable;

        SearchCriteria(String searchText, String searchMode, String category,
                      String yearFrom, String yearTo, boolean includeUnavailable) {
            this.searchText = searchText;
            this.searchMode = searchMode;
            this.category = category;
            this.yearFrom = yearFrom;
            this.yearTo = yearTo;
            this.includeUnavailable = includeUnavailable;
        }

        boolean hasSearchText() {
            return searchText != null && !searchText.isBlank();
        }

        boolean hasCategory() {
            return category != null && !category.isBlank();
        }

        boolean hasYearFrom() {
            return yearFrom != null && !yearFrom.isBlank();
        }

        boolean hasYearTo() {
            return yearTo != null && !yearTo.isBlank();
        }
    }

    @FunctionalInterface
    private interface StatementPreparer {
        void prepare(PreparedStatement stmt) throws SQLException;
    }
}