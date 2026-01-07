package dao.database;

import dao.PurchaseDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Purchase;
import utils.PurchaseStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePurchaseDAO implements PurchaseDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePurchaseDAO.class);

    private final DBConnection dbConnection;

    private static final String[] PURCHASE_COLUMNS = {
            "id", "user_email", "book_id", "status", "purchase_date", "price"
    };

    private static final String COLUMNS_CSV = String.join(", ", PURCHASE_COLUMNS);

    public DatabasePurchaseDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /* ================= CRUD ================= */

    @Override
    public void addPurchase(String userEmail, int bookId) throws DAOException {
        String sql = """
                INSERT INTO purchases (user_email, book_id, status, purchase_date)
                VALUES (?, ?, 'RESERVED', CURRENT_TIMESTAMP)
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error("Errore inserimento acquisto", e);
            throw new DAOException("Errore inserimento acquisto", e);
        }
    }

    @Override
    public void updatePurchaseStatus(int purchaseId, PurchaseStatus status)
            throws DAOException, RecordNotFoundException {

        String sql = "UPDATE purchases SET status = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, purchaseId);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RecordNotFoundException(
                        "Acquisto con ID " + purchaseId + " non trovato");
            }

        } catch (SQLException e) {
            LOGGER.error("Errore aggiornamento stato acquisto {}", purchaseId, e);
            throw new DAOException("Errore aggiornamento stato acquisto", e);
        }
    }

    @Override
    public void rejectPurchase(int purchaseId)
            throws DAOException, RecordNotFoundException {
        updatePurchaseStatus(purchaseId, PurchaseStatus.RESERVED); // o gestisci come "REJECTED" se lo vuoi
    }

    /* ============== RECUPERO ============== */

    @Override
    public Purchase getPurchaseById(int purchaseId)
            throws DAOException, RecordNotFoundException {

        String sql = "SELECT " + COLUMNS_CSV + " FROM purchases WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPurchaseFromResultSet(rs);
                }
            }

            throw new RecordNotFoundException(
                    "Acquisto con ID " + purchaseId + " non trovato");

        } catch (SQLException e) {
            LOGGER.error("Errore recupero acquisto {}", purchaseId, e);
            throw new DAOException("Errore recupero acquisto", e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        return getPurchasesByField("user_email", userEmail);
    }

    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        return getPurchasesByField("book_id", bookId);
    }

    @Override
    public List<Purchase> getPurchasesByStatus(PurchaseStatus status) throws DAOException {
        return getPurchasesByField("status", status.name());
    }

    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        String sql = "SELECT " + COLUMNS_CSV + " FROM purchases";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Purchase> purchases = new ArrayList<>();
            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }
            return purchases;

        } catch (SQLException e) {
            LOGGER.error("Errore recupero acquisti", e);
            throw new DAOException("Errore recupero acquisti", e);
        }
    }

    /* ============== RICERCA ============== */

    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        String sql = "SELECT " + COLUMNS_CSV + " FROM purchases WHERE LOWER(user_email) LIKE ?";

        return search(sql, "%" + searchText.toLowerCase() + "%");
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        String sql = "SELECT " + COLUMNS_CSV +
                     " FROM purchases p JOIN books b ON p.book_id = b.id" +
                     " WHERE LOWER(b.title) LIKE ?";

        return search(sql, "%" + searchText.toLowerCase() + "%");
    }

    /* ============== VERIFICHE ============== */

    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId)
            throws DAOException {

        String sql = "SELECT 1 FROM purchases WHERE user_email = ? AND book_id = ? AND status = 'PURCHASED' LIMIT 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DAOException("Errore verifica acquisto utente", e);
        }
    }

    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail)
            throws DAOException {

        String sql = "SELECT book_id FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);

            List<Integer> ids = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("book_id"));
                }
            }
            return ids;

        } catch (SQLException e) {
            throw new DAOException("Errore recupero libri acquistati", e);
        }
    }

    /* ============== STATISTICHE ============== */

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        String sql = "SELECT COUNT(*) FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore conteggio acquisti", e);
        }
    }

    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        String sql = "SELECT COALESCE(SUM(price), 0) FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore totale spesa utente", e);
        }
    }

    /* ============== METODI DI SUPPORTO ============== */

    private List<Purchase> getPurchasesByField(String field, Object value)
            throws DAOException {

        String sql = "SELECT " + COLUMNS_CSV + " FROM purchases WHERE " + field + " = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, value);

            List<Purchase> list = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractPurchaseFromResultSet(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new DAOException("Errore recupero acquisti", e);
        }
    }

    private List<Purchase> search(String sql, String param) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, param);

            List<Purchase> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(extractPurchaseFromResultSet(rs));
                }
            }
            return results;

        } catch (SQLException e) {
            throw new DAOException("Errore ricerca acquisti", e);
        }
    }

    private Purchase extractPurchaseFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String userEmail = rs.getString("user_email");
        int bookId = rs.getInt("book_id");
        LocalDate statusDate = rs.getDate("purchase_date") != null ? rs.getDate("purchase_date").toLocalDate() : null;
        PurchaseStatus status = PurchaseStatus.valueOf(rs.getString("status"));

        return new Purchase(id, userEmail, bookId, statusDate, status);
    }
}