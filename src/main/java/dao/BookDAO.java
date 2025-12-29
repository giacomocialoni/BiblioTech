package dao;

import model.Book;
import exception.DAOException;
import exception.RecordNotFoundException;

import java.util.List;

public interface BookDAO {
    // Operazioni CRUD
    List<Book> getAllBooks() throws DAOException;
    Book getBookById(int id) throws DAOException, RecordNotFoundException;
    void addBook(Book book) throws DAOException;
    void updateBook(Book book) throws DAOException, RecordNotFoundException;
    void deleteBook(int id) throws DAOException, RecordNotFoundException;
    
    // Ricerca e filtri
    List<Book> searchBooks(String searchText, String searchMode, String category,
                          String yearFrom, String yearTo, boolean includeUnavailable) 
                          throws DAOException;
    
    // Metodi di utilità
    void updateStock(int bookId, int quantity) throws DAOException;
    boolean isBookAvailable(int bookId) throws DAOException;
    List<Book> getBooksByCategory(String category) throws DAOException;
    List<Book> getBooksByAuthor(String author) throws DAOException;
    List<Book> getAvailableBooks() throws DAOException;
}