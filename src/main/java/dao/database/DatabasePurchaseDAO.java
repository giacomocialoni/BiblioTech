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
            LOGGER.info("Acquisto aggiunto per utente {} libro {}", userEmail, bookId);

        } catch (SQLException e) {
            String errorMessage = "Errore durante l'inserimento dell'acquisto per utente " + userEmail + " libro " + bookId;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public void updatePurchaseStatus(int purchaseId, PurchaseStatus status)
            throws DAOException, RecordNotFoundException {

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, purchaseId);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                String errorMessage = "Acquisto con ID " + purchaseId + " non trovato per l'aggiornamento a stato " + status;
                LOGGER.warn(errorMessage);
                throw new RecordNotFoundException(errorMessage);
            }
            LOGGER.info("Stato acquisto {} aggiornato a {}", purchaseId, status);

        } catch (SQLException e) {
            String errorMessage = "Errore SQL durante l'aggiornamento dello stato dell'acquisto ID " + purchaseId;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public void rejectPurchase(int purchaseId)
            throws DAOException, RecordNotFoundException {
        
        LOGGER.info("Richiesta di rifiuto per acquisto ID {}", purchaseId);
        updatePurchaseStatus(purchaseId, PurchaseStatus.RESERVED);
        LOGGER.info("Acquisto ID {} rifiutato", purchaseId);
    }

    /* ============== RECUPERO ============== */

    @Override
    public Purchase getPurchaseById(int purchaseId)
            throws DAOException, RecordNotFoundException {

        LOGGER.debug("Recupero acquisto con ID {}", purchaseId);
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_BY_ID)) {

            stmt.setInt(1, purchaseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LOGGER.debug("Acquisto ID {} trovato", purchaseId);
                    return extractPurchaseFromResultSet(rs);
                }
            }

            String errorMessage = "Acquisto con ID " + purchaseId + " non trovato nel database";
            LOGGER.warn(errorMessage);
            throw new RecordNotFoundException(errorMessage);

        } catch (SQLException e) {
            String errorMessage = "Errore SQL durante il recupero dell'acquisto con ID " + purchaseId;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        LOGGER.debug("Recupero acquisti per utente {}", userEmail);
        
        try {
            List<Purchase> purchases = executeQueryWithStringParam(SQL_GET_BY_USER, userEmail);
            LOGGER.debug("Recuperati {} acquisti per utente {}", purchases.size(), userEmail);
            return purchases;
        } catch (DAOException e) {
            String errorMessage = "Errore durante il recupero degli acquisti per l'utente " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        LOGGER.debug("Recupero acquisti per libro ID {}", bookId);
        
        try {
            List<Purchase> purchases = executeQueryWithIntParam(SQL_GET_BY_BOOK, bookId);
            LOGGER.debug("Recuperati {} acquisti per libro ID {}", purchases.size(), bookId);
            return purchases;
        } catch (DAOException e) {
            String errorMessage = "Errore durante il recupero degli acquisti per il libro ID " + bookId;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByStatus(PurchaseStatus status) throws DAOException {
        LOGGER.debug("Recupero acquisti con stato {}", status);
        
        try {
            List<Purchase> purchases = executeQueryWithStringParam(SQL_GET_BY_STATUS, status.name());
            LOGGER.debug("Recuperati {} acquisti con stato {}", purchases.size(), status);
            return purchases;
        } catch (DAOException e) {
            String errorMessage = "Errore durante il recupero degli acquisti con stato " + status;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        LOGGER.debug("Recupero di tutti gli acquisti");
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_ALL);
             ResultSet rs = stmt.executeQuery()) {

            List<Purchase> purchases = new ArrayList<>();
            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }
            LOGGER.debug("Recuperati {} acquisti totali", purchases.size());
            return purchases;

        } catch (SQLException e) {
            String errorMessage = "Errore SQL durante il recupero di tutti gli acquisti";
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    /* ============== RICERCA ============== */

    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        LOGGER.debug("Ricerca acquisti per utente contenente '{}'", searchText);
        
        try {
            List<Purchase> purchases = executeQueryWithStringParam(SQL_SEARCH_BY_USER, "%" + searchText.toLowerCase() + "%");
            LOGGER.debug("Trovati {} acquisti nella ricerca utente '{}'", purchases.size(), searchText);
            return purchases;
        } catch (DAOException e) {
            String errorMessage = "Errore durante la ricerca degli acquisti per utente: '" + searchText + "'";
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        LOGGER.debug("Ricerca acquisti per libro contenente '{}'", searchText);
        
        try {
            List<Purchase> purchases = executeQueryWithStringParam(SQL_SEARCH_BY_BOOK, "%" + searchText.toLowerCase() + "%");
            LOGGER.debug("Trovati {} acquisti nella ricerca libro '{}'", purchases.size(), searchText);
            return purchases;
        } catch (DAOException e) {
            String errorMessage = "Errore durante la ricerca degli acquisti per libro: '" + searchText + "'";
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    /* ============== VERIFICHE ============== */

    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        LOGGER.debug("Verifica se utente {} ha acquistato libro {}", userEmail, bookId);
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_HAS_PURCHASED)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasPurchased = rs.next();
                LOGGER.debug("Utente {} ha acquistato libro {}: {}", userEmail, bookId, hasPurchased);
                return hasPurchased;
            }

        } catch (SQLException e) {
            String errorMessage = "Errore SQL durante la verifica dell'acquisto per utente " + userEmail + " e libro " + bookId;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
        LOGGER.debug("Recupero ID libri acquistati da utente {}", userEmail);
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_BOOK_IDS)) {

            stmt.setString(1, userEmail);

            List<Integer> ids = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("book_id"));
                }
            }
            LOGGER.debug("Utente {} ha acquistato {} libri", userEmail, ids.size());
            return ids;

        } catch (SQLException e) {
            String errorMessage = "Errore SQL durante il recupero degli ID libri acquistati da " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    /* ============== STATISTICHE ============== */

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        LOGGER.debug("Conteggio acquisti per utente {}", userEmail);
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT)) {

            stmt.setString(1, userEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    LOGGER.debug("Utente {} ha {} acquisti completati", userEmail, count);
                    return count;
                }
                LOGGER.debug("Utente {} non ha acquisti completati", userEmail);
                return 0;
            }

        } catch (SQLException e) {
            String errorMessage = "Errore SQL durante il conteggio degli acquisti per l'utente " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new DAOException(errorMessage, e);
        }
    }

    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        LOGGER.debug("Calcolo spesa totale per utente {}", userEmail);
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_TOTAL_SPENT)) {

            stmt.setString(1, userEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble(1);
                    LOGGER.debug("Utente {} ha speso totale: {}", userEmail, total);
                    return total;
                }
                LOGGER.debug("Utente {} non ha spese registrate", userEmail);
                return 0.0;
            }

        } catch (SQLException e) {
            String errorMessage = "Errore SQL durante il calcolo della spesa totale per l'utente " + userEmail;
            LOGGER.error(errorMessage, e);
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
            String errorMessage = "Errore SQL durante l'esecuzione della query con parametro stringa: " + sql;
            LOGGER.error(errorMessage + " - Parametro: {}", param, e);
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
            String errorMessage = "Errore SQL durante l'esecuzione della query con parametro intero: " + sql;
            LOGGER.error(errorMessage + " - Parametro: {}", param, e);
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