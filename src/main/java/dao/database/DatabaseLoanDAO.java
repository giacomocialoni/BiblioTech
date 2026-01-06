package dao.database;

import dao.LoanDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Loan;
import utils.Constants;
import utils.LoanStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseLoanDAO implements LoanDAO {

    private static final String LOAN_COLUMNS = "id, user_email, book_id, reserved_date, loaned_date, returning_date, status";
    private static final String BASE_SELECT = "SELECT " + LOAN_COLUMNS + " FROM loans ";

    private final DBConnection dbConnection;

    public DatabaseLoanDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /* =======================
       CREATE
       ======================= */

    @Override
    public void addLoan(String userEmail, int bookId) throws DAOException {
        String sql = """
            INSERT INTO loans (user_email, book_id, reserved_date, status)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setString(4, LoanStatus.RESERVED.name());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta del prestito", e);
        }
    }

    /* =======================
       READ
       ======================= */

    @Override
    public Loan getLoanById(int loanId) throws DAOException {
        String sql = BASE_SELECT + " WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractLoanFromResultSet(rs);
                }
                throw new RecordNotFoundException("Prestito con ID " + loanId + " non trovato");
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero del prestito ID " + loanId, e);
        }
    }

    @Override
    public List<Loan> getLoansByUser(String userEmail) throws DAOException {
        String sql = BASE_SELECT + " WHERE user_email = ? ORDER BY reserved_date DESC";
        return executeLoanQuery(sql, stmt -> stmt.setString(1, userEmail));
    }

    @Override
    public List<Loan> getActiveLoansByUser(String userEmail) throws DAOException {
        String sql = "SELECT " + LOAN_COLUMNS +
                     " FROM loans WHERE user_email = ? AND status IN (?, ?) ORDER BY returning_date ASC";

        return executeLoanQuery(sql, stmt -> {
            stmt.setString(1, userEmail);
            stmt.setString(2, LoanStatus.LOANED.name());
            stmt.setString(3, LoanStatus.EXPIRED.name());
        });
    }

    @Override
    public List<Loan> getReservedLoansByUser(String userEmail) throws DAOException {
        String sql = BASE_SELECT + " WHERE user_email = ? AND status = ? ORDER BY reserved_date ASC";

        return executeLoanQuery(sql, stmt -> {
            stmt.setString(1, userEmail);
            stmt.setString(2, LoanStatus.RESERVED.name());
        });
    }

    @Override
    public List<Loan> getLoansByBook(int bookId) throws DAOException {
        String sql = BASE_SELECT + " WHERE book_id = ? ORDER BY reserved_date DESC";
        return executeLoanQuery(sql, stmt -> stmt.setInt(1, bookId));
    }

    @Override
    public List<Loan> getLoansByStatus(String status) throws DAOException {
        String sql = BASE_SELECT + " WHERE status = ? ORDER BY reserved_date DESC";
        return executeLoanQuery(sql, stmt -> stmt.setString(1, status));
    }

    @Override
    public List<Loan> getAllReservedLoans() throws DAOException {
        return getLoansByStatus(LoanStatus.RESERVED.name());
    }

    @Override
    public List<Loan> getAllActiveLoans() throws DAOException {
        String sql = BASE_SELECT + " WHERE status IN (?, ?) ORDER BY returning_date ASC";

        return executeLoanQuery(sql, stmt -> {
            stmt.setString(1, LoanStatus.LOANED.name());
            stmt.setString(2, LoanStatus.EXPIRED.name());
        });
    }

    @Override
    public List<Loan> getAllReturnedLoans() throws DAOException {
        return getLoansByStatus(LoanStatus.RETURNED.name());
    }

    @Override
    public List<Loan> getExpiredLoans() throws DAOException {
        return getLoansByStatus(LoanStatus.EXPIRED.name());
    }

    /* =======================
       UPDATE (TRANSACTIONAL)
       ======================= */

    @Override
    public void acceptLoan(int loanId) throws DAOException {
        executeTransactionalOperation(conn -> {

            String updateLoanSql = """
                UPDATE loans
                SET status = ?, loaned_date = ?, returning_date = ?
                WHERE id = ?
            """;

            String updateBookSql = """
                UPDATE books
                SET stock = stock - 1
                WHERE id = (SELECT book_id FROM loans WHERE id = ?)
            """;

            LocalDate today = LocalDate.now();
            LocalDate returningDate = today.plusDays(Constants.LOANING_DAYS);

            try (PreparedStatement loanStmt = conn.prepareStatement(updateLoanSql);
                 PreparedStatement bookStmt = conn.prepareStatement(updateBookSql)) {

                loanStmt.setString(1, LoanStatus.LOANED.name());
                loanStmt.setDate(2, Date.valueOf(today));
                loanStmt.setDate(3, Date.valueOf(returningDate));
                loanStmt.setInt(4, loanId);

                if (loanStmt.executeUpdate() == 0) {
                    throw new RecordNotFoundException("Prestito non trovato: " + loanId);
                }

                bookStmt.setInt(1, loanId);
                bookStmt.executeUpdate();
            }
        });
    }

    @Override
    public void returnLoan(int loanId) throws DAOException {
        executeTransactionalOperation(conn -> {

            String updateLoanSql = "UPDATE loans SET status = ? WHERE id = ?";
            String updateBookSql = """
                UPDATE books
                SET stock = stock + 1
                WHERE id = (SELECT book_id FROM loans WHERE id = ?)
            """;

            try (PreparedStatement loanStmt = conn.prepareStatement(updateLoanSql);
                 PreparedStatement bookStmt = conn.prepareStatement(updateBookSql)) {

                loanStmt.setString(1, LoanStatus.RETURNED.name());
                loanStmt.setInt(2, loanId);

                if (loanStmt.executeUpdate() == 0) {
                    throw new RecordNotFoundException("Prestito non trovato: " + loanId);
                }

                bookStmt.setInt(1, loanId);
                bookStmt.executeUpdate();
            }
        });
    }

    @Override
    public void updateLoanStatus(int loanId, String status) throws DAOException {
        String sql = "UPDATE loans SET status = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, loanId);

            if (stmt.executeUpdate() == 0) {
                throw new RecordNotFoundException("Prestito non trovato: " + loanId);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore aggiornamento stato prestito", e);
        }
    }

    @Override
    public void deleteLoan(int loanId) throws DAOException {
        String sql = "DELETE FROM loans WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);

            if (stmt.executeUpdate() == 0) {
                throw new RecordNotFoundException("Prestito non trovato: " + loanId);
            }

        } catch (SQLException e) {
            throw new DAOException("Errore eliminazione prestito", e);
        }
    }

    /* =======================
       SEARCH
       ======================= */

    @Override
    public List<Loan> searchLoansByUser(String searchText) throws DAOException {
        String sql = BASE_SELECT + """ 
            l JOIN users u ON l.user_email = u.email
            WHERE LOWER(u.email) LIKE ?
               OR LOWER(u.first_name) LIKE ?
               OR LOWER(u.last_name) LIKE ?
            ORDER BY l.reserved_date DESC
        """;

        String pattern = "%" + safeLower(searchText) + "%";
        return executeLoanQuery(sql, stmt -> {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
        });
    }

    @Override
    public List<Loan> searchLoansByBook(String searchText) throws DAOException {
        String sql = BASE_SELECT + """ 
            l JOIN books b ON l.book_id = b.id
            WHERE LOWER(b.title) LIKE ?
               OR LOWER(b.author) LIKE ?
            ORDER BY l.reserved_date DESC
        """;

        String pattern = "%" + safeLower(searchText) + "%";
        return executeLoanQuery(sql, stmt -> {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
        });
    }

    /* =======================
       UTILITY
       ======================= */

    @Override
    public int countActiveLoansByUser(String userEmail) throws DAOException {
        String sql = """
            SELECT COUNT(*) FROM loans
            WHERE user_email = ? AND status IN (?, ?)
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setString(2, LoanStatus.LOANED.name());
            stmt.setString(3, LoanStatus.EXPIRED.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new DAOException("Errore conteggio prestiti attivi", e);
        }
    }

    @Override
    public boolean hasUserActiveLoans(String userEmail) throws DAOException {
        return countActiveLoansByUser(userEmail) > 0;
    }

    private List<Loan> executeLoanQuery(String sql, StatementPreparer preparer)
            throws DAOException {

        List<Loan> loans = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preparer.prepare(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(extractLoanFromResultSet(rs));
                }
            }

            return loans;

        } catch (SQLException e) {
            throw new DAOException("Errore esecuzione query prestiti", e);
        }
    }

    private void executeTransactionalOperation(TransactionalOperation operation)
            throws DAOException {

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                operation.execute(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Errore operazione transazionale", e);
        }
    }

    private Loan extractLoanFromResultSet(ResultSet rs) throws SQLException {
        return new Loan(
                rs.getInt("id"),
                rs.getString("user_email"),
                rs.getInt("book_id"),
                toLocalDate(rs.getDate("reserved_date")),
                toLocalDate(rs.getDate("loaned_date")),
                toLocalDate(rs.getDate("returning_date")),
                LoanStatus.valueOf(rs.getString("status"))
        );
    }

    private LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    private String safeLower(String text) {
        return text == null ? "" : text.toLowerCase();
    }

    @FunctionalInterface
    private interface StatementPreparer {
        void prepare(PreparedStatement stmt) throws SQLException;
    }

    @FunctionalInterface
    private interface TransactionalOperation {
        void execute(Connection conn) throws SQLException, DAOException;
    }
}