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

public class DatabasePurchaseDAO implements PurchaseDAO {

    private final DBConnection dbConnection;

    // Costanti per le query SQL complete (non costruite dinamicamente)
    private static final String SELECT_ALL_COLUMNS = "SELECT id, user_email, book_id, status, purchase_date, price FROM purchases";
    
    // Query specifiche
    private static final String SQL_GET_BY_ID = SELECT_ALL_COLUMNS + " WHERE id = ?";
    private static final String SQL_GET_ALL = SELECT_ALL_COLUMNS;
    private static final String SQL_GET_BY_USER = SELECT_ALL_COLUMNS + " WHERE user_email = ?";
    private static final String SQL_GET_BY_BOOK = SELECT_ALL_COLUMNS + " WHERE book_id = ?";
    private static final String SQL_GET_BY_STATUS = SELECT_ALL_COLUMNS + " WHERE status = ?";
    private static final String SQL_SEARCH_BY_USER = SELECT_ALL_COLUMNS + " WHERE LOWER(user_email) LIKE ?";
    private static final String SQL_SEARCH_BY_BOOK = SELECT_ALL_COLUMNS + " JOIN books b ON book_id = b.id WHERE LOWER(b.title) LIKE ?";
    
    private static final String SQL_ADD = "INSERT INTO purchases (user_email, book_id, status, purchase_date) VALUES (?, ?, 'RESERVED', CURRENT_TIMESTAMP)";
    private static final String SQL_UPDATE_STATUS = "UPDATE purchases SET status = ? WHERE id = ?";
    private static final String SQL_HAS_PURCHASED = "SELECT 1 FROM purchases WHERE user_email = ? AND book_id = ? AND status = 'PURCHASED' LIMIT 1";
    private static final String SQL_GET_BOOK_IDS = "SELECT book_id FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";
    private static final String SQL_TOTAL_SPENT = "SELECT COALESCE(SUM(price), 0) FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";

    public DatabasePurchaseDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /* ================= CRUD ================= */

    @Override
    public void addPurchase(String userEmail, int bookId) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ADD)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'inserimento dell'acquisto per utente " + userEmail, e);
        }
    }

    @Override
    public void updatePurchaseStatus(int purchaseId, PurchaseStatus status)
            throws DAOException {

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, purchaseId);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                String errorMessage = "Acquisto con ID " + purchaseId + " non trovato per l'aggiornamento";
                throw new RecordNotFoundException(errorMessage);
            }

        } catch (SQLException e) {
            String errorMessage = "Errore durante l'aggiornamento dello stato dell'acquisto ID " + purchaseId + " a " + status;
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public void rejectPurchase(int purchaseId)
            throws DAOException {
        updatePurchaseStatus(purchaseId, PurchaseStatus.RESERVED);
    }

    /* ============== RECUPERO ============== */

    @Override
    public Purchase getPurchaseById(int purchaseId)
            throws DAOException {

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_BY_ID)) {

            stmt.setInt(1, purchaseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPurchaseFromResultSet(rs);
                }
            }

            String errorMessage = "Acquisto con ID " + purchaseId + " non trovato";
            throw new RecordNotFoundException(errorMessage);

        } catch (SQLException e) {
            String errorMessage = "Errore durante il recupero dell'acquisto con ID " + purchaseId;
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        try {
            return executeQueryWithStringParam(SQL_GET_BY_USER, userEmail);
        } catch (DAOException e) {
            throw new DAOException("Errore durante il recupero degli acquisti per l'utente " + userEmail, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        try {
            return executeQueryWithIntParam(SQL_GET_BY_BOOK, bookId);
        } catch (DAOException e) {
            throw new DAOException("Errore durante il recupero degli acquisti per il libro ID " + bookId, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByStatus(PurchaseStatus status) throws DAOException {
        try {
            return executeQueryWithStringParam(SQL_GET_BY_STATUS, status.name());
        } catch (DAOException e) {
            throw new DAOException("Errore durante il recupero degli acquisti con stato " + status, e);
        }
    }

    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_ALL);
             ResultSet rs = stmt.executeQuery()) {

            List<Purchase> purchases = new ArrayList<>();
            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }
            return purchases;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero di tutti gli acquisti", e);
        }
    }

    /* ============== RICERCA ============== */

    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        try {
            return executeQueryWithStringParam(SQL_SEARCH_BY_USER, "%" + searchText.toLowerCase() + "%");
        } catch (DAOException e) {
            throw new DAOException("Errore durante la ricerca degli acquisti per utente: " + searchText, e);
        }
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        try {
            return executeQueryWithStringParam(SQL_SEARCH_BY_BOOK, "%" + searchText.toLowerCase() + "%");
        } catch (DAOException e) {
            throw new DAOException("Errore durante la ricerca degli acquisti per libro: " + searchText, e);
        }
    }

    /* ============== VERIFICHE ============== */

    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId)
            throws DAOException {

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_HAS_PURCHASED)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            String errorMessage = "Errore durante la verifica dell'acquisto per utente " + userEmail + " e libro " + bookId;
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail)
            throws DAOException {

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_BOOK_IDS)) {

            stmt.setString(1, userEmail);

            List<Integer> ids = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("book_id"));
                }
            }
            return ids;

        } catch (SQLException e) {
            String errorMessage = "Errore durante il recupero degli ID libri acquistati da " + userEmail;
            throw new DAOException(errorMessage, e);
        }
    }

    /* ============== STATISTICHE ============== */

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT)) {

            stmt.setString(1, userEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            String errorMessage = "Errore durante il conteggio degli acquisti per l'utente " + userEmail;
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_TOTAL_SPENT)) {

            stmt.setString(1, userEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
                return 0.0;
            }

        } catch (SQLException e) {
            String errorMessage = "Errore durante il calcolo della spesa totale per l'utente " + userEmail;
            throw new DAOException(errorMessage, e);
        }
    }

    /* ============== METODI DI SUPPORTO ============== */

    private List<Purchase> executeQueryWithStringParam(String sql, String param) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, param);

            List<Purchase> list = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractPurchaseFromResultSet(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            String errorMessage = "Errore durante l'esecuzione della query SQL con parametro stringa";
            throw new DAOException(errorMessage, e);
        }
    }

    private List<Purchase> executeQueryWithIntParam(String sql, int param) throws DAOException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, param);

            List<Purchase> list = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractPurchaseFromResultSet(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            String errorMessage = "Errore durante l'esecuzione della query SQL con parametro intero";
            throw new DAOException(errorMessage, e);
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