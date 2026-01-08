package dao.database;

import dao.WishlistDAO;
import exception.DAOException;
import model.User;
import model.Wishlist;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseWishlistDAO implements WishlistDAO {

    private final DBConnection dbConnection;

    private static final String TABLE_WISHLIST = "wishlist";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_BOOK_ID = "book_id";

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";

    private static final String WHERE = " WHERE ";

    public DatabaseWishlistDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void addToWishlist(String userEmail, int bookId) throws DAOException {
        String sql = "INSERT INTO " + TABLE_WISHLIST + " (" + COLUMN_USER_EMAIL + ", " + COLUMN_BOOK_ID + ") VALUES (?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta alla wishlist", e);
        }
    }

    @Override
    public void removeFromWishlist(String userEmail, int bookId) throws DAOException {
        String sql = "DELETE FROM " + TABLE_WISHLIST + WHERE + COLUMN_USER_EMAIL + " = ? AND " + COLUMN_BOOK_ID + " = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException("Errore durante la rimozione dalla wishlist", e);
        }
    }

    @Override
    public boolean isInWishlist(String userEmail, int bookId) throws DAOException {
        String sql = "SELECT 1 FROM " + TABLE_WISHLIST + WHERE + COLUMN_USER_EMAIL + " = ? AND " + COLUMN_BOOK_ID + " = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            stmt.setInt(2, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DAOException("Errore durante il controllo della wishlist", e);
        }
    }

    @Override
    public List<Wishlist> getWishlistByUser(String userEmail) throws DAOException {
        List<Wishlist> wishlist = new ArrayList<>();
        String sql = "SELECT " + COLUMN_USER_EMAIL + ", " + COLUMN_BOOK_ID +
                     " FROM " + TABLE_WISHLIST +
                     WHERE + COLUMN_USER_EMAIL + " = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    wishlist.add(new Wishlist(
                        rs.getString(COLUMN_USER_EMAIL),
                        rs.getInt(COLUMN_BOOK_ID)
                    ));
                }
            }

            return wishlist;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero della wishlist", e);
        }
    }

    @Override
    public List<User> getUsersWithBookInWishlist(int bookId) throws DAOException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u." + COLUMN_EMAIL + ", u." + COLUMN_PASSWORD + ", u." + COLUMN_FIRST_NAME + ", u." + COLUMN_LAST_NAME +
                     " FROM " + TABLE_WISHLIST + " w JOIN " + TABLE_USERS + " u ON w." + COLUMN_USER_EMAIL + " = u." + COLUMN_EMAIL +
                     WHERE + "w." + COLUMN_BOOK_ID + " = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString(COLUMN_EMAIL),
                        rs.getString(COLUMN_PASSWORD),
                        rs.getString(COLUMN_FIRST_NAME),
                        rs.getString(COLUMN_LAST_NAME)
                    );
                    users.add(user);
                }
            }

            return users;

        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero degli utenti che hanno il libro nella wishlist", e);
        }
    }
}