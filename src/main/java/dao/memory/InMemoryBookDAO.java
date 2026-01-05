package dao.memory;

import dao.BookDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryBookDAO implements BookDAO {

    private final List<Book> books = new ArrayList<>();
    private int nextId = 4;

    public InMemoryBookDAO() {
        // Libri di esempio

        // Avventura
        books.add(new Book(
                1,
                "Into the Wild",
                "Jon Krakauer",
                "Adventure",
                1996,
                "Villard",
                224,
                "9780679428502",
                4,
                "The true story of a young man who abandoned everything to live in the Alaskan wilderness.",
                null,
                14.99
        ));

        // Biografia
        books.add(new Book(
                2,
                "Steve Jobs",
                "Walter Isaacson",
                "Biography",
                2011,
                "Simon & Schuster",
                656,
                "9781451648539",
                3,
                "The authorized self-titled biography of Apple co-founder Steve Jobs.",
                null,
                19.99
        ));

        // Narrativo
        books.add(new Book(
                3,
                "The Old Man and the Sea",
                "Ernest Hemingway",
                "Narrative",
                1952,
                "Charles Scribner's Sons",
                128,
                "9780684801223",
                6,
                "A short novel about an aging fisherman's struggle with a giant marlin.",
                null,
                11.99
        ));
    }

    @Override
    public List<Book> getAllBooks() throws DAOException {
        return new ArrayList<>(books);
    }

    @Override
    public Book getBookById(int id) throws DAOException, RecordNotFoundException {
        return books.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RecordNotFoundException("Libro con ID " + id + " non trovato"));
    }

    @Override
    public void addBook(Book book) throws DAOException {
        book.setId(nextId++);
        books.add(book);
    }

    @Override
    public void updateBook(Book book) throws DAOException, RecordNotFoundException {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == book.getId()) {
                books.set(i, book);
                return;
            }
        }
        throw new RecordNotFoundException("Libro con ID " + book.getId() + " non trovato");
    }

    @Override
    public void deleteBook(int id) throws DAOException, RecordNotFoundException {
        Optional<Book> bookToRemove = books.stream()
                .filter(b -> b.getId() == id)
                .findFirst();
        
        if (bookToRemove.isPresent()) {
            books.remove(bookToRemove.get());
        } else {
            throw new RecordNotFoundException("Libro con ID " + id + " non trovato");
        }
    }

    @Override
    public List<Book> searchBooks(String searchText, String searchMode, String category,
                                 String yearFrom, String yearTo, boolean includeUnavailable) 
                                 throws DAOException {
        List<Book> result = new ArrayList<>(books);
        
        // Filtro per ricerca testo
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lowerText = searchText.toLowerCase();
            if ("title".equalsIgnoreCase(searchMode)) {
                result.removeIf(b -> !b.getTitle().toLowerCase().contains(lowerText));
            } else if ("author".equalsIgnoreCase(searchMode)) {
                result.removeIf(b -> !b.getAuthor().toLowerCase().contains(lowerText));
            }
        }
        
        // Filtro per categoria
        if (category != null && !category.trim().isEmpty()) {
            result.removeIf(b -> !b.getCategory().equalsIgnoreCase(category));
        }
        
        // Filtro per anno
        if (yearFrom != null && !yearFrom.trim().isEmpty()) {
            try {
                int yearFromInt = Integer.parseInt(yearFrom);
                result.removeIf(b -> b.getYear() < yearFromInt);
            } catch (NumberFormatException e) {
                // Ignora
            }
        }
        
        if (yearTo != null && !yearTo.trim().isEmpty()) {
            try {
                int yearToInt = Integer.parseInt(yearTo);
                result.removeIf(b -> b.getYear() > yearToInt);
            } catch (NumberFormatException e) {
                // Ignora
            }
        }
        
        // Filtro per disponibilità
        if (!includeUnavailable) {
            result.removeIf(b -> b.getStock() <= 0);
        }
        
        return result;
    }

    @Override
    public void updateStock(int bookId, int quantity) throws DAOException {
        books.stream()
                .filter(b -> b.getId() == bookId)
                .findFirst()
                .ifPresent(book -> {
                    int newStock = Math.max(0, book.getStock() + quantity);
                    book.setStock(newStock);
                });
    }

    @Override
    public boolean isBookAvailable(int bookId) throws DAOException {
        return books.stream()
                .filter(b -> b.getId() == bookId)
                .findFirst()
                .map(b -> b.getStock() > 0)
                .orElse(false);
    }

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
}