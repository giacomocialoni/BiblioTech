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

    private static final String PURCHASE_COLUMNS = "id, user_email, book_id, quantity, status, status_date";
    private static final String BASE_SELECT = "SELECT " + PURCHASE_COLUMNS + " FROM purchases ";

    private final DBConnection dbConnection;
    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(DatabasePurchaseDAO.class);

    public DatabasePurchaseDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /* =======================
       CREATE (con diminuzione stock)
       ======================= */
    @Override
    public void addPurchase(String userEmail, int bookId, int quantity) throws DAOException {
        if (quantity <= 0) {
            throw new DAOException("Quantità non valida: " + quantity);
        }

        executeTransactionalOperation(conn -> {
            // 1. Verifica che lo stock sia sufficiente
            String checkStockSql = "SELECT stock FROM books WHERE id = ? FOR UPDATE";
            int currentStock;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSql)) {
                checkStmt.setInt(1, bookId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new RecordNotFoundException("Libro non trovato: " + bookId);
                    }
                    currentStock = rs.getInt("stock");
                    if (currentStock < quantity) {
                        throw new DAOException("Stock insufficiente: " + currentStock + " < " + quantity);
                    }
                }
            }

            // 2. Diminuisce lo stock
            String updateStockSql = "UPDATE books SET stock = stock - ? WHERE id = ?";
            try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSql)) {
                stockStmt.setInt(1, quantity);
                stockStmt.setInt(2, bookId);
                int rowsUpdated = stockStmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new RecordNotFoundException("Libro non trovato per aggiornamento stock: " + bookId);
                }
            }

            // 3. Crea la prenotazione
            String insertSql = "INSERT INTO purchases (user_email, book_id, quantity, status, status_date) VALUES (?, ?, ?, 'RESERVED', CURRENT_DATE)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, userEmail);
                insertStmt.setInt(2, bookId);
                insertStmt.setInt(3, quantity);
                insertStmt.executeUpdate();
            }
        });
    }

    /* =======================
       READ
       ======================= */
    @Override
    public Purchase getPurchaseById(int purchaseId) throws DAOException {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPurchaseFromResultSet(rs);
                }
                throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
            }
        } catch (SQLException e) {
            throw new DAOException("Errore recupero acquisto ID " + purchaseId, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        return executeQueryWithStringParam(BASE_SELECT + " WHERE user_email = ? ORDER BY status_date DESC", userEmail);
    }

    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        return executeQueryWithIntParam(BASE_SELECT + " WHERE book_id = ? ORDER BY status_date DESC", bookId);
    }

    @Override
    public List<Purchase> getPurchasesByStatus(PurchaseStatus status) throws DAOException {
        return executeQueryWithStringParam(BASE_SELECT + " WHERE status = ? ORDER BY status_date DESC", status.name());
    }

    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(BASE_SELECT + " ORDER BY status_date DESC");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore recupero tutti gli acquisti", e);
        }
        return purchases;
    }

    /* =======================
       UPDATE (solo cambio status)
       ======================= */
    @Override
    public void updatePurchaseStatus(int purchaseId, PurchaseStatus status) throws DAOException {
        
        String sql = "UPDATE purchases SET status = ?, status_date = CURRENT_DATE WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, purchaseId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Errore aggiornamento stato acquisto", e);
        }
    }

    /* =======================
       REJECT (con ripristino stock)
       ======================= */
    @Override
    public void rejectPurchase(int purchaseId) throws DAOException {

        executeTransactionalOperation(conn -> {
            int bookId, quantity;

            // 1. Recupera i dati dell'acquisto
            String selectSql = "SELECT book_id, quantity FROM purchases WHERE id = ? AND status = 'RESERVED' FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, purchaseId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new RecordNotFoundException("Acquisto non trovato o già processato: " + purchaseId);
                    }
                    bookId = rs.getInt("book_id");
                    quantity = rs.getInt("quantity");
                }
            }

            // 2. Ripristina lo stock
            String updateStockSql = "UPDATE books SET stock = stock + ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateStockSql)) {
                stmt.setInt(1, quantity);
                stmt.setInt(2, bookId);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new RecordNotFoundException("Libro non trovato per ripristino stock: " + bookId);
                }
            }

            // 3. Elimina la prenotazione
            String deleteSql = "DELETE FROM purchases WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, purchaseId);
                stmt.executeUpdate();
            }
        });
    }

    /* =======================
       RICERCA
       ======================= */
    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        String sql = BASE_SELECT + " WHERE LOWER(user_email) LIKE ? ORDER BY status_date DESC";
        return executeQueryWithStringParam(sql, "%" + safeLower(searchText) + "%");
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        String sql = BASE_SELECT + " JOIN books b ON book_id = b.id WHERE LOWER(b.title) LIKE ? ORDER BY p.status_date DESC";
        return executeQueryWithStringParam(sql, "%" + safeLower(searchText) + "%");
    }

    /* =======================
       VERIFICHE E STATISTICHE
       ======================= */
    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        String sql = "SELECT 1 FROM purchases WHERE user_email = ? AND book_id = ? AND status = 'PURCHASED' LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore verifica acquisto utente/libro", e);
        }
    }

    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
        String sql = "SELECT book_id FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";
        List<Integer> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("book_id"));
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
        return list;
    }

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        String sql = "SELECT SUM(quantity) FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        String sql = "SELECT COALESCE(SUM(b.price * p.quantity), 0) " +
                     "FROM purchases p JOIN books b ON p.book_id = b.id " +
                     "WHERE p.user_email = ? AND p.status = 'PURCHASED'";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    /* =======================
       METODI DI SUPPORTO
       ======================= */
    private Purchase extractPurchaseFromResultSet(ResultSet rs) throws SQLException {
        return new Purchase(
                rs.getInt("id"),
                rs.getString("user_email"),
                rs.getInt("book_id"),
                rs.getInt("quantity"),
                rs.getDate("status_date") != null ? rs.getDate("status_date").toLocalDate() : LocalDate.now(),
                PurchaseStatus.valueOf(rs.getString("status"))
        );
    }

    private List<Purchase> executeQueryWithStringParam(String sql, String param) throws DAOException {
        List<Purchase> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (param != null) stmt.setString(1, param);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(extractPurchaseFromResultSet(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new DAOException("Errore esecuzione query", e);
        }
    }

    private List<Purchase> executeQueryWithIntParam(String sql, int param) throws DAOException {
        List<Purchase> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, param);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(extractPurchaseFromResultSet(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new DAOException("Errore esecuzione query", e);
        }
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase();
    }

    private void executeTransactionalOperation(TransactionOperation operation) throws DAOException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            operation.execute(conn);
            conn.commit();
        } catch (SQLException | DAOException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.error("Transazione fallita, rollback eseguito", e);
                } catch (SQLException rollbackEx) {
                    logger.error("Errore durante il rollback", rollbackEx);
                }
            }
            throw new DAOException("Errore durante l'esecuzione della transazione", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Errore chiusura connessione", e);
                }
            }
        }
    }

    @FunctionalInterface
    private interface TransactionOperation {
        void execute(Connection conn) throws DAOException, SQLException;
    }
}