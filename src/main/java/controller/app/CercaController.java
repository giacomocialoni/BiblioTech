package controller.app;

import dao.BookDAO;
import dao.CategoryDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.DuplicateRecordException;
import exception.RecordNotFoundException;
import model.Book;
import model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import bean.BookBean;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CercaController {

    private static final Logger logger = LoggerFactory.getLogger(CercaController.class);

    private final BookDAO bookDAO;
    private final CategoryDAO categoryDAO;

    public CercaController() {
        this.bookDAO = DAOFactory.getInstance().getBookDAO();
        this.categoryDAO = DAOFactory.getInstance().getCategoryDAO();
    }

    public List<String> getAllCategoryNames() {
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            return categories.stream()
                    .map(Category::getCategory)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore nel recupero delle categorie", e);
            return List.of(); 
        }
    }

    public List<BookBean> searchBooks(String searchText, String searchMode, String category,
                                      String yearFrom, String yearTo, boolean includeUnavailable) {
        try {
            List<Book> books = bookDAO.searchBooks(searchText, searchMode, category, 
                                                   yearFrom, yearTo, includeUnavailable);

            return books.stream().map(book -> {
                BookBean bean = new BookBean();
                try {
                    bean.setId(book.getId());
                    bean.setTitle(book.getTitle());
                    bean.setAuthor(book.getAuthor());
                    bean.setCategory(book.getCategory());
                    if (book.getImagePath() != null) {
                        bean.setImagePath(book.getImagePath());
                    }
                    bean.setStock(book.getStock());
                } catch (Exception e) {
                    logger.warn("Dati libro non validi: " + book.getId(), e);
                }
                return bean;
            }).collect(Collectors.toList());

        } catch (DAOException e) {
            logger.error("Errore durante la ricerca dei libri", e);
            return Collections.emptyList();
        }
    }

    public boolean addCategory(Category category) {
        try {
            categoryDAO.addCategory(category);
            return true;
        } catch (DuplicateRecordException e) {
            logger.warn("Categoria già esistente: " + category.getCategory());
            return false;
        } catch (DAOException e) {
            logger.error("Errore durante l'aggiunta della categoria: " + category.getCategory(), e);
            return false;
        }
    }

    public boolean deleteCategory(String categoryName) {
        try {
            categoryDAO.deleteCategory(categoryName);
            return true;
        } catch (RecordNotFoundException e) {
            logger.warn("Categoria non trovata: " + categoryName);
            return false;
        } catch (DAOException e) {
            logger.error("Errore durante l'eliminazione della categoria: " + categoryName, e);
            return false;
        }
    }
}