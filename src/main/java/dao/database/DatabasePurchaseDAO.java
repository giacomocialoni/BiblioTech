package dao.database;

import dao.PurchaseDAO;
import model.Purchase;
import utils.PurchaseStatus;
import exception.DAOException;
import exception.RecordNotFoundException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabasePurchaseDAO implements PurchaseDAO {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(DatabasePurchaseDAO.class);

    private final DBConnection dbConnection;

    private static final String[] PURCHASE_COLUMNS = {
            "id", "user_email", "book_id", "status", "status_date"
    };

    private static final String PURCHASE_COLUMNS_JOINED =
            String.join(", ", PURCHASE_COLUMNS);

    /* ======================= SQL ======================= */

    private static final String SELECT_BY_ID =
            "SELECT " + PURCHASE_COLUMNS_JOINED + " FROM purchases WHERE id = ?";

    private static final String SELECT_BY_USER =
            "SELECT " + PURCHASE_COLUMNS_JOINED + " FROM purchases WHERE user_email = ? ORDER BY status_date DESC";

    private static final String SELECT_BY_BOOK =
            "SELECT " + PURCHASE_COLUMNS_JOINED + " FROM purchases WHERE book_id = ? ORDER BY status_date DESC";

    private static final String SELECT_BY_STATUS =
            "SELECT " + PURCHASE_COLUMNS_JOINED + " FROM purchases WHERE status = ? ORDER BY status_date DESC";

    private static final String SELECT_ALL =
            "SELECT " + PURCHASE_COLUMNS_JOINED + " FROM purchases ORDER BY status_date DESC";

    private static final String INSERT_PURCHASE =
            "INSERT INTO purchases (user_email, book_id, status, status_date) VALUES (?, ?, 'RESERVED', ?)";

    private static final String UPDATE_STATUS =
            "UPDATE purchases SET status = ?, status_date = ? WHERE id = ?";

    private static final String DELETE_PURCHASE =
            "DELETE FROM purchases WHERE id = ?";

    private static final String CHECK_PURCHASED =
            "SELECT 1 FROM purchases WHERE user_email = ? AND book_id = ? AND status = 'PURCHASED'";

    private static final String COUNT_PURCHASES =
            "SELECT COUNT(*) FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";

    private static final String TOTAL_SPENT = """
        SELECT SUM(b.price)
        FROM purchases p
        JOIN books b ON p.book_id = b.id
        WHERE p.user_email = ? AND p.status = 'PURCHASED'
    """;

    /* =================================================== */

    public DatabasePurchaseDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void addPurchase(String userEmail, int bookId) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PURCHASE)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore inserimento acquisto", e);
        }
    }

    @Override
    public Purchase getPurchaseById(int purchaseId) throws DAOException {
        return executeSinglePurchaseQuery(SELECT_BY_ID, stmt -> stmt.setInt(1, purchaseId),
                "Acquisto con ID " + purchaseId + " non trovato");
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        return executePurchaseListQuery(SELECT_BY_USER, stmt -> stmt.setString(1, userEmail));
    }

    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        return executePurchaseListQuery(SELECT_BY_BOOK, stmt -> stmt.setInt(1, bookId));
    }

    @Override
    public List<Purchase> getPurchasesByStatus(String status) throws DAOException {
        return executePurchaseListQuery(SELECT_BY_STATUS, stmt -> stmt.setString(1, status));
    }

    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        return executePurchaseListQuery(SELECT_ALL, null);
    }

    @Override
    public void updatePurchaseStatus(int purchaseId, String status) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {

            stmt.setString(1, status);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            stmt.setInt(3, purchaseId);

            if (stmt.executeUpdate() == 0) {
                throw new RecordNotFoundException("Acquisto non trovato");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore aggiornamento stato acquisto", e);
        }
    }

    @Override
    public void rejectPurchase(int purchaseId) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PURCHASE)) {

            stmt.setInt(1, purchaseId);

            if (stmt.executeUpdate() == 0) {
                throw new RecordNotFoundException("Acquisto non trovato");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore eliminazione acquisto", e);
        }
    }

    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CHECK_PURCHASED)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            return stmt.executeQuery().next();

        } catch (SQLException e) {
            throw new DAOException("Errore controllo acquisto", e);
        }
    }

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_PURCHASES)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            throw new DAOException("Errore conteggio acquisti", e);
        }
    }

    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(TOTAL_SPENT)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;

        } catch (SQLException e) {
            throw new DAOException("Errore calcolo spesa totale", e);
        }
    }

    /* ======================= HELPERS ======================= */

    private List<Purchase> executePurchaseListQuery(String sql, StatementFiller filler)
            throws DAOException {

        List<Purchase> list = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (filler != null) {
                filler.fill(stmt);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractPurchaseFromResultSet(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DAOException("Errore query acquisti", e);
        }
    }

    private Purchase executeSinglePurchaseQuery(String sql,
                                                StatementFiller filler,
                                                String notFoundMsg) throws DAOException {

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            filler.fill(stmt);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractPurchaseFromResultSet(rs);
            }
            throw new RecordNotFoundException(notFoundMsg);

        } catch (SQLException e) {
            throw new DAOException("Errore recupero acquisto", e);
        }
    }

    private Purchase extractPurchaseFromResultSet(ResultSet rs) throws SQLException {
        return new Purchase(
                rs.getInt("id"),
                rs.getString("user_email"),
                rs.getInt("book_id"),
                rs.getDate("status_date").toLocalDate(),
                PurchaseStatus.valueOf(rs.getString("status"))
        );
    }

    @FunctionalInterface
    private interface StatementFiller {
        void fill(PreparedStatement stmt) throws SQLException;
    }

	@Override
	public List<Purchase> getAllReservedPurchases() throws DAOException {
		return getPurchasesByStatus("RESERVED");
	}

	@Override
	public List<Purchase> getAllCompletedPurchases() throws DAOException {
		return getPurchasesByStatus("PURCHASED");
	}

	@Override
	public void acceptPurchase(int purchaseId) throws DAOException {
	    String sql = "UPDATE purchases SET status = ?, status_date = ? WHERE id = ?";

	    try (Connection conn = dbConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, "PURCHASED");
	        stmt.setDate(2, Date.valueOf(LocalDate.now()));
	        stmt.setInt(3, purchaseId);

	        int rowsAffected = stmt.executeUpdate();
	        if (rowsAffected == 0) {
	            throw new RecordNotFoundException(
	                "Nessun acquisto trovato con ID " + purchaseId
	            );
	        }

	    } catch (SQLException e) {
	        LOGGER.error("Errore durante l'accettazione dell'acquisto {}", purchaseId, e);
	        throw new DAOException(
	            "Errore durante l'accettazione dell'acquisto " + purchaseId, e
	        );
	    }
	}

	@Override
	public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
	    List<Purchase> purchases = new ArrayList<>();

	    String sql = """
	        SELECT p.id, p.user_email, p.book_id, p.status, p.status_date
	        FROM purchases p
	        JOIN users u ON p.user_email = u.email
	        WHERE LOWER(u.email) LIKE ?
	           OR LOWER(u.first_name) LIKE ?
	           OR LOWER(u.last_name) LIKE ?
	        ORDER BY p.status_date DESC
	    """;

	    String pattern = "%" + (searchText == null ? "" : searchText.toLowerCase()) + "%";

	    try (Connection conn = dbConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, pattern);
	        stmt.setString(2, pattern);
	        stmt.setString(3, pattern);

	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            purchases.add(extractPurchaseFromResultSet(rs));
	        }

	        return purchases;

	    } catch (SQLException e) {
	        LOGGER.error("Errore ricerca acquisti per utente {}", searchText, e);
	        throw new DAOException(
	            "Errore ricerca acquisti per utente " + searchText, e
	        );
	    }
	}

	@Override
	public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
	    List<Purchase> purchases = new ArrayList<>();

	    String sql = """
	        SELECT p.id, p.user_email, p.book_id, p.status, p.status_date
	        FROM purchases p
	        JOIN books b ON p.book_id = b.id
	        WHERE LOWER(b.title) LIKE ?
	           OR LOWER(b.author) LIKE ?
	        ORDER BY p.status_date DESC
	    """;

	    String pattern = "%" + (searchText == null ? "" : searchText.toLowerCase()) + "%";

	    try (Connection conn = dbConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, pattern);
	        stmt.setString(2, pattern);

	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            purchases.add(extractPurchaseFromResultSet(rs));
	        }

	        return purchases;

	    } catch (SQLException e) {
	        LOGGER.error("Errore ricerca acquisti per libro {}", searchText, e);
	        throw new DAOException(
	            "Errore ricerca acquisti per libro " + searchText, e
	        );
	    }
	}

	@Override
	public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
	    List<Integer> bookIds = new ArrayList<>();

	    String sql = """
	        SELECT book_id
	        FROM purchases
	        WHERE user_email = ?
	          AND status = 'PURCHASED'
	    """;

	    try (Connection conn = dbConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, userEmail);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            bookIds.add(rs.getInt("book_id"));
	        }

	        return bookIds;

	    } catch (SQLException e) {
	        LOGGER.error("Errore recupero libri acquistati da {}", userEmail, e);
	        throw new DAOException(
	            "Errore recupero libri acquistati da " + userEmail, e
	        );
	    }
	}
}