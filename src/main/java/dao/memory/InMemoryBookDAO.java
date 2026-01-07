package dao.memory;

import dao.BookDAO;
import exception.DAOException;
import exception.DuplicateBookException;
import exception.RecordNotFoundException;
import model.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryBookDAO implements BookDAO {

    private static InMemoryBookDAO instance = null;
    private final List<Book> books = new ArrayList<>();
    private int nextId = 4;

    public InMemoryBookDAO() {
        initializeSampleBooks();
    }

    public static InMemoryBookDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryBookDAO();
        }
        return instance;
    }

    /* =========================
       INIZIALIZZAZIONE
       ========================= */

    private void initializeSampleBooks() {
    	// Aventura
    	books.add(Book.builder()
    	    .id(1)
    	    .title("Into the Wild")
    	    .author("Jon Krakauer")
    	    .category("Adventure")
    	    .year(1996)
    	    .publisher("Villard")
    	    .pages(224)
    	    .isbn("9780679428502")
    	    .stock(4)
    	    .plot("The true story of a young man who abandoned everything to live in the Alaskan wilderness.")
    	    .price(14.99)
    	    .build());

    	// Biografia
    	books.add(Book.builder()
    	    .id(2)
    	    .title("Steve Jobs")
    	    .author("Walter Isaacson")
    	    .category("Biography")
    	    .year(2011)
    	    .publisher("Simon & Schuster")
    	    .pages(656)
    	    .isbn("9781451648539")
    	    .stock(3)
    	    .plot("The authorized self-titled biography of Apple co-founder Steve Jobs.")
    	    .price(19.99)
    	    .build());

    	// Narrativo
    	books.add(Book.builder()
    	    .id(3)
    	    .title("The Old Man and the Sea")
    	    .author("Ernest Hemingway")
    	    .category("Narrative")
    	    .year(1952)
    	    .publisher("Charles Scribner's Sons")
    	    .pages(128)
    	    .isbn("9780684801223")
    	    .stock(6)
    	    .plot("A short novel about an aging fisherman's struggle with a giant marlin.")
    	    .price(11.99)
    	    .build());
    }

    /* =========================
       CRUD
       ========================= */

    @Override
    public List<Book> getAllBooks() throws DAOException {
        return new ArrayList<>(books);
    }

    @Override
    public Book getBookById(int id) throws DAOException, RecordNotFoundException {
        return findBookById(id)
                .orElseThrow(() -> 
                        new RecordNotFoundException("Libro con ID " + id + " non trovato"));
    }

    @Override
    public void addBook(Book book) throws DAOException, DuplicateBookException {
        // Verifica duplicati per ISBN
        boolean duplicateExists = books.stream()
                .anyMatch(b -> b.getIsbn().equals(book.getIsbn()));
        
        if (duplicateExists) {
            throw new DuplicateBookException("ISBN già esistente: " + book.getIsbn(), null, null);
        }
        
        book.setId(nextId++);
        books.add(book);
    }

    @Override
    public void updateBook(Book book) throws DAOException, RecordNotFoundException {
        int index = findBookIndexById(book.getId());
        books.set(index, book);
    }

    @Override
    public void deleteBook(int id) throws DAOException, RecordNotFoundException {
        Book book = getBookById(id);
        books.remove(book);
    }

    /* =========================
       SEARCH
       ========================= */

    @Override
    public List<Book> searchBooks(String searchText,
                                  String searchMode,
                                  String category,
                                  String yearFrom,
                                  String yearTo,
                                  boolean includeUnavailable) throws DAOException {
        return books.stream()
                .filter(b -> matchesSearchText(b, searchText, searchMode))
                .filter(b -> matchesCategory(b, category))
                .filter(b -> matchesYearFrom(b, yearFrom))
                .filter(b -> matchesYearTo(b, yearTo))
                .filter(b -> includeUnavailable || b.getStock() > 0)
                .toList();
    }

    /* =========================
       STOCK & AVAILABILITY
       ========================= */

    @Override
    public void updateStock(int bookId, int quantity) throws DAOException {
        findBookById(bookId).ifPresent(book ->
                book.setStock(Math.max(0, book.getStock() + quantity))
        );
    }

    @Override
    public boolean isBookAvailable(int bookId) throws DAOException {
        return findBookById(bookId)
                .map(b -> b.getStock() > 0)
                .orElse(false);
    }

    /* =========================
       FILTRI SEMPLICI
       ========================= */

    @Override
    public List<Book> getBooksByCategory(String category) throws DAOException {
        return books.stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @Override
    public List<Book> getBooksByAuthor(String author) throws DAOException {
        return books.stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .toList();
    }

    @Override
    public List<Book> getAvailableBooks() throws DAOException {
        return books.stream()
                .filter(b -> b.getStock() > 0)
                .toList();
    }

    /* =========================
       METODI DI SUPPORTO
       ========================= */

    private Optional<Book> findBookById(int id) {
        return books.stream()
                .filter(b -> b.getId() == id)
                .findFirst();
    }

    private int findBookIndexById(int id) throws RecordNotFoundException {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == id) {
                return i;
            }
        }
        throw new RecordNotFoundException("Libro con ID " + id + " non trovato");
    }

    private boolean matchesSearchText(Book book, String text, String mode) {
        if (text == null || text.trim().isEmpty()) return true;

        String lowerText = text.toLowerCase();

        if ("author".equalsIgnoreCase(mode)) {
            return book.getAuthor().toLowerCase().contains(lowerText);
        }

        return book.getTitle().toLowerCase().contains(lowerText);
    }

    private boolean matchesCategory(Book book, String category) {
        return category == null || category.trim().isEmpty()
                || book.getCategory().equalsIgnoreCase(category);
    }

    private boolean matchesYearFrom(Book book, String yearFrom) {
        Integer year = parseYear(yearFrom);
        return year == null || book.getYear() >= year;
    }

    private boolean matchesYearTo(Book book, String yearTo) {
        Integer year = parseYear(yearTo);
        return year == null || book.getYear() <= year;
    }

    private Integer parseYear(String year) {
        try {
            return (year == null || year.trim().isEmpty())
                    ? null
                    : Integer.parseInt(year);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}