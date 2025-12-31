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

    private final DBConnection dbConnection;

    public DatabasePurchaseDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void addPurchase(String userEmail, int bookId) throws DAOException {
        String sql = "INSERT INTO purchases (user_email, book_id, status, status_date) VALUES (?, ?, 'RESERVED', ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta dell'acquisto", e);
        }
    }

    @Override
    public Purchase getPurchaseById(int purchaseId) throws DAOException, RecordNotFoundException {
        String sql = "SELECT * FROM purchases WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractPurchaseFromResultSet(rs);
            } else {
                throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero dell'acquisto ID " + purchaseId, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases WHERE user_email = ? ORDER BY status_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }

            return purchases;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero degli acquisti per l'utente " + userEmail, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases WHERE book_id = ? ORDER BY status_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }

            return purchases;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero degli acquisti per il libro ID " + bookId, e);
        }
    }

    @Override
    public List<Purchase> getPurchasesByStatus(String status) throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases WHERE status = ? ORDER BY status_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }

            return purchases;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero degli acquisti con stato " + status, e);
        }
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
    public List<Purchase> getAllPurchases() throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases ORDER BY status_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }

            return purchases;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero di tutti gli acquisti", e);
        }
    }

    @Override
    public void acceptPurchase(int purchaseId) throws DAOException, RecordNotFoundException {
        String sql = "UPDATE purchases SET status = 'PURCHASED', status_date = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, purchaseId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RecordNotFoundException("Nessun acquisto trovato con ID: " + purchaseId);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'accettazione dell'acquisto ID " + purchaseId, e);
        }
    }

    @Override
    public void updatePurchaseStatus(int purchaseId, String status) throws DAOException, RecordNotFoundException {
        String sql = "UPDATE purchases SET status = ?, status_date = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            stmt.setInt(3, purchaseId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RecordNotFoundException("Nessun acquisto trovato con ID: " + purchaseId);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiornamento dello stato dell'acquisto ID " + purchaseId, e);
        }
    }

    @Override
    public void rejectPurchase(int purchaseId) throws DAOException, RecordNotFoundException {
        String sql = "DELETE FROM purchases WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RecordNotFoundException("Nessun acquisto trovato con ID: " + purchaseId);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'eliminazione dell'acquisto ID " + purchaseId, e);
        }
    }

    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = """
            SELECT p.* 
            FROM purchases p
            JOIN users u ON p.user_email = u.email
            WHERE LOWER(u.email) LIKE ? OR LOWER(u.first_name) LIKE ? OR LOWER(u.last_name) LIKE ?
            ORDER BY p.status_date DESC
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + (searchText == null ? "" : searchText.toLowerCase()) + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }

            return purchases;

        } catch (SQLException e) {
            throw new DAOException("Errore durante la ricerca degli acquisti per utente", e);
        }
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        String sql = """
            SELECT p.* 
            FROM purchases p
            JOIN books b ON p.book_id = b.id
            WHERE LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ?
            ORDER BY p.status_date DESC
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + (searchText == null ? "" : searchText.toLowerCase()) + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                purchases.add(extractPurchaseFromResultSet(rs));
            }

            return purchases;

        } catch (SQLException e) {
            throw new DAOException("Errore durante la ricerca degli acquisti per libro", e);
        }
    }

    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        String sql = "SELECT 1 FROM purchases WHERE user_email = ? AND book_id = ? AND status = 'PURCHASED'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next();

        } catch (SQLException e) {
            throw new DAOException("Errore durante il controllo dell'acquisto per utente " + userEmail + " e libro " + bookId, e);
        }
    }

    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
        List<Integer> bookIds = new ArrayList<>();
        String sql = "SELECT book_id FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bookIds.add(rs.getInt("book_id"));
            }

            return bookIds;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero degli ID libri acquistati da " + userEmail, e);
        }
    }

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        String sql = "SELECT COUNT(*) as count FROM purchases WHERE user_email = ? AND status = 'PURCHASED'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il conteggio degli acquisti per " + userEmail, e);
        }
    }

    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        String sql = """
            SELECT SUM(b.price) as total 
            FROM purchases p
            JOIN books b ON p.book_id = b.id
            WHERE p.user_email = ? AND p.status = 'PURCHASED'
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            return 0.0;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il calcolo della spesa totale per " + userEmail, e);
        }
    }

    private Purchase extractPurchaseFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String userEmail = rs.getString("user_email");
        int bookId = rs.getInt("book_id");
        LocalDate statusDate = rs.getDate("status_date") != null ? 
                               rs.getDate("status_date").toLocalDate() : null;
        PurchaseStatus status = PurchaseStatus.valueOf(rs.getString("status"));
        
        return new Purchase(id, userEmail, bookId, statusDate, status);
    }
}