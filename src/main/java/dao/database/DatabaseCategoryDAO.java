package dao.database;

import dao.CategoryDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import exception.DuplicateRecordException;
import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCategoryDAO implements CategoryDAO {

    private final DBConnection dbConnection;

    public DatabaseCategoryDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<Category> getAllCategories() throws DAOException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category FROM categories ORDER BY category";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new Category(rs.getString("category")));
            }
            
            return categories;
        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero delle categorie", e);
        }
    }

    @Override
    public void addCategory(Category category)
            throws DAOException, DuplicateRecordException {

        try {
            if (exists(category.getCategory())) {
                throw new DuplicateRecordException("La categoria esiste già: " + category.getCategory());
            }

            String sql = "INSERT INTO categories (category) VALUES (?)";

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, category.getCategory());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta della categoria", e);
        }
    }

    @Override
    public void deleteCategory(String category)
            throws DAOException {

        try {
            if (!exists(category)) {
                throw new RecordNotFoundException("Categoria non trovata: " + category);
            }

            String sql = "DELETE FROM categories WHERE category = ?";

            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, category);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore durante l'eliminazione della categoria", e);
        }
    }

    private boolean exists(String category) throws SQLException {
        String sql = "SELECT 1 FROM categories WHERE category = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}