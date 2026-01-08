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

    private static final String[] BOOK_COLUMNS = {
            ID, TITLE, AUTHOR, CATEGORY, YEAR, PUBLISHER,
            PAGES, ISBN, STOCK, PLOT, IMAGE_PATH, PRICE
    };

    private static final String BOOK_COLUMNS_JOINED = String.join(", ", BOOK_COLUMNS);

    /* ======================= SQL CONSTANTS ======================= */
    private static final String SELECT_PREFIX = "SELECT ";
    private static final String FROM = " FROM books";
    private static final String WHERE = " WHERE ";
    private static final String ORDER_BY = " ORDER BY ";

    private static final String SELECT_ALL_BOOKS = SELECT_PREFIX + BOOK_COLUMNS_JOINED + FROM + ORDER_BY + TITLE;
    private static final String SELECT_BOOK_BY_ID = SELECT_PREFIX + BOOK_COLUMNS_JOINED + FROM + WHERE + ID + " = ?";
    private static final String SELECT_AVAILABLE_BOOKS = SELECT_PREFIX + BOOK_COLUMNS_JOINED + FROM + WHERE + STOCK + " > 0" + ORDER_BY + TITLE;
    private static final String SELECT_BOOKS_BY_CATEGORY = SELECT_PREFIX + BOOK_COLUMNS_JOINED + FROM + WHERE + CATEGORY + " = ?" + ORDER_BY + TITLE;
    private static final String SELECT_BOOKS_BY_AUTHOR = SELECT_PREFIX + BOOK_COLUMNS_JOINED + FROM + WHERE + AUTHOR + " LIKE ?" + ORDER_BY + YEAR + " DESC, " + TITLE;

    private static final String INSERT_BOOK = "INSERT INTO books (title, author, category, year, publisher, pages, isbn, stock, plot, image_path, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_BOOK = "UPDATE books SET title=?, author=?, category=?, year=?, publisher=?, pages=?, isbn=?, stock=?, plot=?, image_path=?, price=? WHERE id=?";
    private static final String DELETE_BOOK = "DELETE FROM books" + WHERE + ID + " = ?";
    private static final String UPDATE_STOCK = "UPDATE books SET " + STOCK + " = " + STOCK + " + ? " + WHERE + ID + " = ?";
    private static final String CHECK_AVAILABILITY = SELECT_PREFIX + STOCK + FROM + WHERE + ID + " = ?";

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
        StringBuilder sql = new StringBuilder(SELECT_PREFIX + BOOK_COLUMNS_JOINED + FROM + WHERE + "1=1 ");
        List<Object> params = new ArrayList<>();

        appendSearchCriteria(sql, params, searchText, searchMode, category, yearFrom, yearTo, includeUnavailable);

        sql.append(ORDER_BY).append(TITLE);

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

        } catch (SQLException e) {
            LOGGER.error("Errore durante la ricerca dei libri", e);
            throw new DAOException("Errore durante la ricerca dei libri", e);
        }
    }

    private void appendSearchCriteria(StringBuilder sql, List<Object> params, String searchText, String searchMode,
                                      String category, String yearFrom, String yearTo, boolean includeUnavailable) {
        if (searchText != null && !searchText.isBlank()) {
            if (TITLE.equalsIgnoreCase(searchMode)) {
                sql.append("AND LOWER(").append(TITLE).append(") LIKE ? ");
                params.add("%" + searchText.toLowerCase() + "%");
            } else if (AUTHOR.equalsIgnoreCase(searchMode)) {
                sql.append("AND LOWER(").append(AUTHOR).append(") LIKE ? ");
                params.add("%" + searchText.toLowerCase() + "%");
            }
        }

        if (category != null && !category.isBlank()) {
            sql.append("AND ").append(CATEGORY).append(" = ? ");
            params.add(category);
        }

        if (yearFrom != null && !yearFrom.isBlank()) {
            try {
                sql.append("AND ").append(YEAR).append(" >= ? ");
                params.add(Integer.parseInt(yearFrom));
            } catch (NumberFormatException e) {
                LOGGER.warn("Valore yearFrom non valido: {}", yearFrom);
            }
        }

        if (yearTo != null && !yearTo.isBlank()) {
            try {
                sql.append("AND ").append(YEAR).append(" <= ? ");
                params.add(Integer.parseInt(yearTo));
            } catch (NumberFormatException e) {
                LOGGER.warn("Valore yearTo non valido: {}", yearTo);
            }
        }

        if (!includeUnavailable) {
            sql.append("AND ").append(STOCK).append(" > 0 ");
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
            LOGGER.error("Errore aggiunta libro {}", book.getTitle(), e);
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
            LOGGER.error("Errore aggiornamento libro ID {}", book.getId(), e);
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
            LOGGER.error("Errore eliminazione libro ID {}", id, e);
            throw new DAOException("Errore durante l'eliminazione del libro ID " + id, e);
        }
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

    @Override
    public void updateStock(int bookId, int quantity) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STOCK)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error("Errore aggiornamento stock libro ID {}", bookId, e);
            throw new DAOException("Errore aggiornamento stock libro ID " + bookId, e);
        }
    }

    @Override
    public List<Book> getBooksByCategory(String category) throws DAOException {
        return executeBookListQuery(SELECT_BOOKS_BY_CATEGORY, stmt -> stmt.setString(1, category));
    }

    @Override
    public List<Book> getBooksByAuthor(String author) throws DAOException {
        return executeBookListQuery(SELECT_BOOKS_BY_AUTHOR, stmt -> stmt.setString(1, "%" + author + "%"));
    }

    @Override
    public List<Book> getAvailableBooks() throws DAOException {
        return executeBookListQuery(SELECT_AVAILABLE_BOOKS, null);
    }

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
            LOGGER.error("Errore esecuzione query libri", e);
            throw new DAOException("Errore esecuzione query libri", e);
        }
    }

    private Book executeSingleBookQuery(String sql, StatementFiller filler, String notFoundMsg) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            filler.fill(stmt);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractBookFromResultSet(rs);
            }
            throw new RecordNotFoundException(notFoundMsg);

        } catch (SQLException e) {
            LOGGER.error("Errore recupero libro", e);
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
                ? ISBN : TITLE;
    }

    @FunctionalInterface
    private interface StatementFiller {
        void fill(PreparedStatement stmt) throws SQLException;
    }
}