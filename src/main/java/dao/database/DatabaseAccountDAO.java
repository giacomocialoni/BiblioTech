package dao.database;

import dao.AccountDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import exception.EmailAlreadyRegisteredException;
import model.Admin;
import model.User;
import model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAccountDAO implements AccountDAO {

    private final DBConnection dbConnection;

    public DatabaseAccountDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Account login(String email, String password) throws DAOException, RecordNotFoundException {
        
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new RecordNotFoundException("Credenziali non valide");
            }

            String role = rs.getString("role");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            
            if ("admin".equalsIgnoreCase(role)) {
                Admin admin = new Admin(email, password, firstName, lastName);
                return admin;
            } else {
                User user = new User(email, password, firstName, lastName);
                return user;
            }
        } catch (SQLException e) {
            throw new DAOException("Errore durante il login", e);
        }
    }

 // DatabaseAccountDAO.java (modifica solo il metodo register)
    @Override
    public boolean register(String email, String password, String firstName, String lastName)
            throws DAOException, EmailAlreadyRegisteredException {  

        try {
            if (emailExists(email)) {
                throw new EmailAlreadyRegisteredException(email);
            }

            String sql = "INSERT INTO users (email, password, first_name, last_name, role) VALUES (?, ?, ?, ?, 'logged_user')";
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.setString(3, firstName);
                stmt.setString(4, lastName);

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate entry")) {
                throw new EmailAlreadyRegisteredException(email, 
                    "Questa email è già associata a un account esistente");
            }
            throw new DAOException("Errore durante la registrazione", e);
        }
    }

    private boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}