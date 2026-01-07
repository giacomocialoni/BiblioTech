package controller.app;

import dao.BookDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.DuplicateBookException;
import exception.RecordNotFoundException;
import model.Book;
import bean.BookBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ManageBooksController {

    private static final Logger logger = LoggerFactory.getLogger(ManageBooksController.class);
    private final BookDAO bookDAO;

    public ManageBooksController() {
        this.bookDAO = DAOFactory.getInstance().getBookDAO();
    }

    // ===== RICERCHE LIBRI =====

    public List<BookBean> searchBooks(String searchText) {
        try {
            List<Book> allBooks = bookDAO.getAllBooks();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lower = searchText.toLowerCase();
                allBooks = allBooks.stream()
                        .filter(book -> book.getTitle().toLowerCase().contains(lower) ||
                                        book.getAuthor().toLowerCase().contains(lower) ||
                                        book.getIsbn().toLowerCase().contains(lower) ||
                                        book.getCategory().toLowerCase().contains(lower))
                        .collect(Collectors.toList());
            }
            return mapBooks(allBooks);
        } catch (DAOException e) {
            logger.error("Errore DAO durante la ricerca libri", e);
            return List.of();
        }
    }

    public List<BookBean> getAllBooks() {
        try {
            List<Book> allBooks = bookDAO.getAllBooks();
            return mapBooks(allBooks);
        } catch (DAOException e) {
            logger.error("Errore DAO durante il recupero di tutti i libri", e);
            return List.of();
        }
    }

    public BookBean getBookById(int bookId) {
        try {
            Book book = bookDAO.getBookById(bookId);
            return mapBook(book);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro non trovato: {}", bookId, e);
            return null;
        } catch (DAOException e) {
            logger.error("Errore DAO durante il recupero libro: {}", bookId, e);
            return null;
        }
    }
    
    public List<String> getAllCategoryNames() {
        try {
            return DAOFactory.getInstance().getCategoryDAO()
                             .getAllCategories()
                             .stream()
                             .map(model.Category::getCategory)
                             .toList();
        } catch (DAOException e) {
            logger.error("Errore DAO recupero categorie", e);
            return List.of();
        }
    }

    // ===== OPERAZIONI CRUD / STOCK =====

    public void addBook(BookBean bookBean) throws DuplicateBookException {
        try {
            Book book = mapBeanToModel(bookBean);
            bookDAO.addBook(book);

        } catch (DuplicateBookException e) {
            logger.warn("Libro duplicato: {}", e.getMessage());
            throw e;

        } catch (DAOException e) {
            logger.error("Errore DAO durante la creazione del libro", e);
            throw new RuntimeException("Errore durante la creazione del libro", e);
        }
    }

    public void updateBook(Book book) {
        try {
            bookDAO.updateBook(book);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro da aggiornare non trovato: {}", book.getId(), e);
        } catch (DAOException e) {
            logger.error("Errore DAO durante l'aggiornamento del libro: {}", book.getId(), e);
        }
    }

    public void deleteBook(int bookId) {
        try {
            bookDAO.deleteBook(bookId);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro da eliminare non trovato: {}", bookId, e);
        } catch (DAOException e) {
            logger.error("Errore DAO durante l'eliminazione del libro: {}", bookId, e);
        }
    }

    public void increaseStock(int bookId, int quantity) {
        try {
            Book book = bookDAO.getBookById(bookId);
            book.setStock(book.getStock() + quantity);
            bookDAO.updateBook(book);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro da aumentare stock non trovato: {}", bookId, e);
        } catch (DAOException e) {
            logger.error("Errore DAO durante aumento stock libro: {}", bookId, e);
        }
    }

    public void decreaseStock(int bookId, int quantity) {
        try {
            Book book = bookDAO.getBookById(bookId);
            int newStock = Math.max(0, book.getStock() - quantity);
            book.setStock(newStock);
            bookDAO.updateBook(book);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro da diminuire stock non trovato: {}", bookId, e);
        } catch (DAOException e) {
            logger.error("Errore DAO durante diminuzione stock libro: {}", bookId, e);
        }
    }

    public void setStock(int bookId, int newStock) {
        try {
            Book book = bookDAO.getBookById(bookId);
            book.setStock(Math.max(0, newStock));
            bookDAO.updateBook(book);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro da impostare stock non trovato: {}", bookId, e);
        } catch (DAOException e) {
            logger.error("Errore DAO durante impostazione stock libro: {}", bookId, e);
        }
    }

    // ===== METODI PRIVATI DI MAPPING =====

    private List<BookBean> mapBooks(List<Book> books) {
        return books.stream()
                .map(this::mapBook)
                .toList(); 
    }

    private BookBean mapBook(Book book) {
        BookBean bean = new BookBean();
        try {
            bean.setId(book.getId());
            bean.setTitle(book.getTitle());
            bean.setAuthor(book.getAuthor());
            bean.setCategory(book.getCategory());
            bean.setImagePath(book.getImagePath());
            bean.setStock(book.getStock());
            bean.setPrice(book.getPrice());
        } catch (Exception e) {
            logger.warn("Dati libro non validi id={}", book.getId(), e);
        }
        return bean;
    }
    
    private Book mapBeanToModel(BookBean bean) {
        return new Book(
                0, // id generato dal DB
                bean.getTitle(),
                bean.getAuthor(),
                bean.getCategory(),
                bean.getYear(),
                bean.getPublisher(),
                bean.getPages(),
                bean.getIsbn(),
                bean.getStock(),
                bean.getPlot(),
                bean.getImagePath(),
                bean.getPrice()
        );
    }
}