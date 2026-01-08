package dao.memory;

import dao.CategoryDAO;
import exception.DAOException;
import exception.DuplicateRecordException;
import exception.RecordNotFoundException;
import model.Category;

import java.util.ArrayList;
import java.util.List;

public class InMemoryCategoryDAO implements CategoryDAO {

    private static InMemoryCategoryDAO instance = null;
    private final List<Category> categories = new ArrayList<>();

    public InMemoryCategoryDAO() {
        // Inizializza con categorie predefinite
        categories.add(new Category("Adventure"));
        categories.add(new Category("Biography"));
        categories.add(new Category("Narrative"));
        categories.add(new Category("Programming"));
        categories.add(new Category("Science"));
        categories.add(new Category("History"));
    }

    public static InMemoryCategoryDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryCategoryDAO();
        }
        return instance;
    }

    @Override
    public List<Category> getAllCategories() throws DAOException {
        return new ArrayList<>(categories);
    }

    @Override
    public void addCategory(Category category) throws DAOException, DuplicateRecordException {
        // Verifica duplicati
        boolean duplicateExists = categories.stream()
                .anyMatch(c -> c.getCategory().equalsIgnoreCase(category.getCategory()));
        
        if (duplicateExists) {
            throw new DuplicateRecordException("Categoria già esistente: " + category.getCategory());
        }
        
        categories.add(category);
    }

    @Override
    public void deleteCategory(String category) throws DAOException {
        boolean removed = categories.removeIf(c -> c.getCategory().equalsIgnoreCase(category));
        
        if (!removed) {
            throw new RecordNotFoundException("Categoria non trovata: " + category);
        }
    }
}