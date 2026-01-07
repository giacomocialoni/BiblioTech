package dao.csv;

import dao.CategoryDAO;
import exception.DAOException;
import exception.DuplicateRecordException;
import exception.RecordNotFoundException;
import model.Category;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVCategoryDAO implements CategoryDAO {
    
    private static final String FILE_PATH = "src/main/resources/data/categories.csv";
    private static final String CSV_HEADER = "category";
    
    @Override
    public List<Category> getAllCategories() throws DAOException {
        List<Category> categories = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return categories;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
        	String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV categorie non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                String categoryName = line.trim();
                if (!categoryName.isEmpty()) {
                    categories.add(new Category(categoryName));
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il recupero delle categorie", e);
        }
        
        return categories;
    }
    
    @Override
    public void addCategory(Category category) throws DAOException, DuplicateRecordException {
        List<Category> categories = getAllCategories();
        
        for (Category existing : categories) {
            if (existing.getCategory().equalsIgnoreCase(category.getCategory())) {
                throw new DuplicateRecordException(
                    "La categoria esiste già: " + category.getCategory());
            }
        }
        
        categories.add(category);
        saveAllCategories(categories);
    }
    
    @Override
    public void deleteCategory(String categoryName) throws DAOException, RecordNotFoundException {
        List<Category> categories = getAllCategories();
        
        boolean removed = categories.removeIf(
            cat -> cat.getCategory().equalsIgnoreCase(categoryName));
        
        if (!removed) {
            throw new RecordNotFoundException("Categoria non trovata: " + categoryName);
        }
        
        saveAllCategories(categories);
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
            throw new DAOException("Errore durante il salvataggio delle categorie", e);
        }
    }
}