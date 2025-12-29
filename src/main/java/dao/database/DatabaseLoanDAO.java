package dao.database;

import dao.LoanDAO;
import model.Loan;
import utils.Constants;
import utils.LoanStatus;
import exception.DAOException;
import exception.RecordNotFoundException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseLoanDAO implements LoanDAO {

    private final DBConnection dbConnection;

    public DatabaseLoanDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void addLoan(String userEmail, int bookId) throws DAOException {
        String sql = "INSERT INTO loans (user_email, book_id, reserved_date, status) VALUES (?, ?, ?, 'RESERVED')";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta del prestito", e);
        }
    }

    @Override
    public Loan getLoanById(int loanId) throws DAOException, RecordNotFoundException {
        String sql = "SELECT * FROM loans WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractLoanFromResultSet(rs);
            } else {
                throw new RecordNotFoundException("Prestito con ID " + loanId + " non trovato");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero del prestito ID " + loanId, e);
        }
    }

    @Override
    public List<Loan> getLoansByUser(String userEmail) throws DAOException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE user_email = ? ORDER BY reserved_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                loans.add(extractLoanFromResultSet(rs));
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero dei prestiti per l'utente " + userEmail, e);
        }
    }

    @Override
    public List<Loan> getActiveLoansByUser(String userEmail) throws DAOException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE user_email = ? AND status IN ('LOANED', 'EXPIRED') ORDER BY returning_date ASC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                loans.add(extractLoanFromResultSet(rs));
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero dei prestiti attivi per l'utente " + userEmail, e);
        }
    }

    @Override
    public List<Loan> getReservedLoansByUser(String userEmail) throws DAOException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE user_email = ? AND status = 'RESERVED' ORDER BY reserved_date ASC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                loans.add(extractLoanFromResultSet(rs));
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero dei prestiti riservati per l'utente " + userEmail, e);
        }
    }

    @Override
    public List<Loan> getLoansByBook(int bookId) throws DAOException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE book_id = ? ORDER BY reserved_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                loans.add(extractLoanFromResultSet(rs));
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero dei prestiti per il libro ID " + bookId, e);
        }
    }

    @Override
    public List<Loan> getLoansByStatus(String status) throws DAOException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE status = ? ORDER BY reserved_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                loans.add(extractLoanFromResultSet(rs));
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero dei prestiti con stato " + status, e);
        }
    }

    @Override
    public List<Loan> getAllReservedLoans() throws DAOException {
        return getLoansByStatus("RESERVED");
    }

    @Override
    public List<Loan> getAllActiveLoans() throws DAOException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE status IN ('LOANED', 'EXPIRED') ORDER BY returning_date ASC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                loans.add(extractLoanFromResultSet(rs));
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero di tutti i prestiti attivi", e);
        }
    }

    @Override
    public List<Loan> getAllReturnedLoans() throws DAOException {
        return getLoansByStatus("RETURNED");
    }

    @Override
    public void acceptLoan(int loanId) throws DAOException, RecordNotFoundException {
        String updateLoanSql = "UPDATE loans SET status = 'LOANED', loaned_date = ?, returning_date = ? WHERE id = ?";
        String updateBookSql = "UPDATE books SET stock = stock - 1 WHERE id = (SELECT book_id FROM loans WHERE id = ?)";
        
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement loanStmt = conn.prepareStatement(updateLoanSql);
                 PreparedStatement bookStmt = conn.prepareStatement(updateBookSql)) {

                // Aggiorna il prestito
                LocalDate today = LocalDate.now();
                LocalDate returningDate = today.plusDays(Constants.LOANING_DAYS);
                
                loanStmt.setDate(1, Date.valueOf(today));
                loanStmt.setDate(2, Date.valueOf(returningDate));
                loanStmt.setInt(3, loanId);
                
                int rowsAffected = loanStmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    throw new RecordNotFoundException("Nessun prestito trovato con ID: " + loanId);
                }

                // Aggiorna lo stock del libro
                bookStmt.setInt(1, loanId);
                bookStmt.executeUpdate();

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'accettazione del prestito ID " + loanId, e);
        }
    }

    @Override
    public void returnLoan(int loanId) throws DAOException, RecordNotFoundException {
        String updateLoanSql = "UPDATE loans SET status = 'RETURNED' WHERE id = ?";
        String updateBookSql = "UPDATE books SET stock = stock + 1 WHERE id = (SELECT book_id FROM loans WHERE id = ?)";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement loanStmt = conn.prepareStatement(updateLoanSql);
                 PreparedStatement bookStmt = conn.prepareStatement(updateBookSql)) {

                loanStmt.setInt(1, loanId);
                int rowsAffected = loanStmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    conn.rollback();
                    throw new RecordNotFoundException("Nessun prestito trovato con ID: " + loanId);
                }

                bookStmt.setInt(1, loanId);
                bookStmt.executeUpdate();

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante la restituzione del prestito ID " + loanId, e);
        }
    }

    @Override
    public void updateLoanStatus(int loanId, String status) throws DAOException, RecordNotFoundException {
        String sql = "UPDATE loans SET status = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, loanId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RecordNotFoundException("Nessun prestito trovato con ID: " + loanId);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiornamento dello stato del prestito ID " + loanId, e);
        }
    }

    @Override
    public void deleteLoan(int loanId) throws DAOException, RecordNotFoundException {
        String sql = "DELETE FROM loans WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RecordNotFoundException("Nessun prestito trovato con ID: " + loanId);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'eliminazione del prestito ID " + loanId, e);
        }
    }

    @Override
    public List<Loan> searchLoansByUser(String searchText) throws DAOException {
        List<Loan> loans = new ArrayList<>();
        String sql = """
            SELECT l.* 
            FROM loans l
            JOIN users u ON l.user_email = u.email
            WHERE LOWER(u.email) LIKE ? OR LOWER(u.first_name) LIKE ? OR LOWER(u.last_name) LIKE ?
            ORDER BY l.reserved_date DESC
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + (searchText == null ? "" : searchText.toLowerCase()) + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                loans.add(extractLoanFromResultSet(rs));
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore durante la ricerca dei prestiti per utente", e);
        }
    }

    @Override
    public List<Loan> searchLoansByBook(String searchText) throws DAOException {
        List<Loan> loans = new ArrayList<>();
        String sql = """
            SELECT l.* 
            FROM loans l
            JOIN books b ON l.book_id = b.id
            WHERE LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ?
            ORDER BY l.reserved_date DESC
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + (searchText == null ? "" : searchText.toLowerCase()) + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                loans.add(extractLoanFromResultSet(rs));
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore durante la ricerca dei prestiti per libro", e);
        }
    }

    @Override
    public int countActiveLoansByUser(String userEmail) throws DAOException {
        String sql = "SELECT COUNT(*) as count FROM loans WHERE user_email = ? AND status IN ('LOANED', 'EXPIRED')";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il conteggio dei prestiti attivi per " + userEmail, e);
        }
    }

    @Override
    public boolean hasUserActiveLoans(String userEmail) throws DAOException {
        return countActiveLoansByUser(userEmail) > 0;
    }

    @Override
    public List<Loan> getExpiredLoans() throws DAOException {
        return getLoansByStatus("EXPIRED");
    }

    private Loan extractLoanFromResultSet(ResultSet rs) throws SQLException {
        int loanId = rs.getInt("id");
        String userEmail = rs.getString("user_email");
        int bookId = rs.getInt("book_id");
        LoanStatus status = LoanStatus.valueOf(rs.getString("status"));
        
        LocalDate reservedDate = rs.getDate("reserved_date") != null ? 
                                rs.getDate("reserved_date").toLocalDate() : null;
        LocalDate loanedDate = rs.getDate("loaned_date") != null ? 
                              rs.getDate("loaned_date").toLocalDate() : null;
        LocalDate returningDate = rs.getDate("returning_date") != null ? 
                                 rs.getDate("returning_date").toLocalDate() : null;

        return new Loan(loanId, userEmail, bookId, reservedDate, loanedDate, returningDate, status);
    }
}