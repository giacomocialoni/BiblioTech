package dao.csv;

import dao.CategoryDAO;
import exception.DAOException;
import exception.DuplicateRecordException;
import exception.RecordNotFoundException;
import model.Category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVCategoryDAO implements CategoryDAO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVCategoryDAO.class);
    private static final String FILE_PATH = "src/main/resources/data/categories.csv";
    
    @Override
    public List<Category> getAllCategories() throws DAOException {
        List<Category> categories = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.info("File categorie non trovato, restituita lista vuota");
            return categories;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String categoryName = line.trim();
                if (!categoryName.isEmpty()) {
                    categories.add(new Category(categoryName));
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante il recupero delle categorie", e);
            throw new DAOException("Errore durante il recupero delle categorie", e);
        }
        
        LOGGER.debug("Recuperate {} categorie", categories.size());
        return categories;
    }
    
    @Override
    public void addCategory(Category category) throws DAOException, DuplicateRecordException {
        // Verifica duplicati
        List<Category> categories = getAllCategories();
        for (Category existing : categories) {
            if (existing.getCategory().equalsIgnoreCase(category.getCategory())) {
                LOGGER.warn("Tentativo di aggiunta categoria duplicata: {}", category.getCategory());
                throw new DuplicateRecordException(
                    "La categoria esiste già: " + category.getCategory());
            }
        }
        
        // Aggiungi nuova categoria
        categories.add(category);
        saveAllCategories(categories);
        LOGGER.info("Categoria aggiunta: {}", category.getCategory());
    }
    
    @Override
    public void deleteCategory(String categoryName) throws DAOException, RecordNotFoundException {
        List<Category> categories = getAllCategories();
        
        boolean removed = categories.removeIf(
            cat -> cat.getCategory().equalsIgnoreCase(categoryName));
        
        if (!removed) {
            LOGGER.warn("Tentativo di eliminazione categoria non trovata: {}", categoryName);
            throw new RecordNotFoundException("Categoria non trovata: " + categoryName);
        }
        
        saveAllCategories(categories);
        LOGGER.info("Categoria eliminata: {}", categoryName);
    }
    
    private void saveAllCategories(List<Category> categories) throws DAOException {
        try {
            Files.createDirectories(Paths.get(FILE_PATH).getParent());
            
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(FILE_PATH), 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                
                writer.write("category");
                writer.newLine();
                
                for (Category category : categories) {
                    writer.write(category.getCategory());
                    writer.newLine();
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante il salvataggio delle categorie", e);
            throw new DAOException("Errore durante il salvataggio delle categorie", e);
        }
    }
}