package dao.database;

import dao.UserDAO;
import model.User;
import exception.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUserDAO implements UserDAO {

    private final DBConnection dbConnection;
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";

    public DatabaseUserDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public User getUser(String email) throws DAOException {
        String sql = "SELECT email, password, first_name, last_name FROM users WHERE email = ? AND role = 'logged_user'";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString(EMAIL),
                            rs.getString(PASSWORD),
                            rs.getString(FIRST_NAME),
                            rs.getString(LAST_NAME)
                    );
                } else {
                    throw new DAOException("Utente non trovato con email: " + email);
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante la ricerca dell'utente", e);
        }
    }

    @Override
    public List<User> getAllUsers() throws DAOException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT email, password, first_name, last_name FROM users WHERE role = 'logged_user' ORDER BY last_name, first_name";
        
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(new User(
                        rs.getString(EMAIL),
                        rs.getString(PASSWORD),
                        rs.getString(FIRST_NAME),
                        rs.getString(LAST_NAME)
                        ));
            }
            
            return users;
            
        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero degli utenti loggati", e);
        }
    }

    @Override
    public List<User> searchUsers(String searchTerm) throws DAOException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT email, password, first_name, last_name FROM users " +
                     "WHERE LOWER(email) LIKE LOWER(?) OR LOWER(first_name) LIKE LOWER(?) OR LOWER(last_name) LIKE LOWER(?) " +
                     "AND role = 'logged_user' " +
                     "ORDER BY email";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + (searchTerm == null ? "" : searchTerm) + "%";
            stmt.setString(1, likeTerm);
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getString(EMAIL),
                            rs.getString(PASSWORD),
                            rs.getString(FIRST_NAME),
                            rs.getString(LAST_NAME)
                    ));
                }
            }

            return users;

        } catch (SQLException e) {
            throw new DAOException("Errore durante la ricerca degli utenti", e);
        }
    }

    @Override
    public void deleteUser(String email) throws DAOException {
        String deletePurchasesSql = "DELETE FROM purchases WHERE user_email = ?";
        String deleteLoansSql = "DELETE FROM loans WHERE user_email = ?";
        String deleteWishlistSql = "DELETE FROM wishlist WHERE user_email = ?";
        String deleteUserSql = "DELETE FROM users WHERE email = ?";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(deletePurchasesSql)) {
                stmt.setString(1, email);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(deleteLoansSql)) {
                stmt.setString(1, email);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(deleteWishlistSql)) {
                stmt.setString(1, email);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(deleteUserSql)) {
                stmt.setString(1, email);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    throw new DAOException("Utente non trovato per la cancellazione: " + email);
                }
            }

            conn.commit();

        } catch (SQLException e) {
            throw new DAOException("Errore durante la cancellazione dell'utente", e);
        }
    }
}