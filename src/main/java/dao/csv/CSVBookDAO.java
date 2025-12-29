package dao.csv;

import dao.BookDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Book;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVBookDAO implements BookDAO {
    
    private static final String FILE_PATH = "data/book.csv";
    private final List<Book> books;
    
    public CSVBookDAO() throws DAOException {
        this.books = new ArrayList<>();
        loadBooks();
    }
    
    @Override
    public List<Book> getAllBooks() throws DAOException {
        return new ArrayList<>(books);
    }
    
    @Override
    public Book getBookById(int id) throws DAOException, RecordNotFoundException {
        return books.stream()
                .filter(book -> book.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RecordNotFoundException("Libro con ID " + id + " non trovato"));
    }
    
    @Override
    public void addBook(Book book) throws DAOException {
        // Genera ID se non presente
        if (book.getId() <= 0) {
            int maxId = books.stream().mapToInt(Book::getId).max().orElse(0);
            book.setId(maxId + 1);
        }
        books.add(book);
        saveBooks();
    }
    
    @Override
    public void updateBook(Book book) throws DAOException, RecordNotFoundException {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == book.getId()) {
                books.set(i, book);
                saveBooks();
                return;
            }
        }
        throw new RecordNotFoundException("Libro con ID " + book.getId() + " non trovato");
    }
    
    @Override
    public void deleteBook(int id) throws DAOException, RecordNotFoundException {
        boolean removed = books.removeIf(book -> book.getId() == id);
        if (!removed) {
            throw new RecordNotFoundException("Libro con ID " + id + " non trovato");
        }
        saveBooks();
    }
    
    @Override
    public List<Book> searchBooks(String searchText, String searchMode, String category,
                                  String yearFrom, String yearTo, boolean includeUnavailable) 
                                  throws DAOException {
        
        return books.stream()
                .filter(book -> includeUnavailable || book.getStock() > 0)
                .filter(book -> category == null || category.isEmpty() || 
                        book.getCategory().equalsIgnoreCase(category))
                .filter(book -> {
                    if (yearFrom != null && !yearFrom.isEmpty()) {
                        try {
                            int yearFromInt = Integer.parseInt(yearFrom);
                            if (book.getYear() < yearFromInt) return false;
                        } catch (NumberFormatException e) {
                            return true;
                        }
                    }
                    return true;
                })
                .filter(book -> {
                    if (yearTo != null && !yearTo.isEmpty()) {
                        try {
                            int yearToInt = Integer.parseInt(yearTo);
                            if (book.getYear() > yearToInt) return false;
                        } catch (NumberFormatException e) {
                            return true;
                        }
                    }
                    return true;
                })
                .filter(book -> matchSearch(book, searchText, searchMode))
                .toList();
    }
    
    @Override
    public void updateStock(int bookId, int quantity) throws DAOException {
        books.stream()
                .filter(book -> book.getId() == bookId)
                .findFirst()
                .ifPresent(book -> {
                    int newStock = Math.max(0, book.getStock() + quantity);
                    book.setStock(newStock);
                    try {
                        saveBooks();
                    } catch (DAOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    
    @Override
    public boolean isBookAvailable(int bookId) throws DAOException {
        return books.stream()
                .filter(book -> book.getId() == bookId)
                .findFirst()
                .map(book -> book.getStock() > 0)
                .orElse(false);
    }
    
    @Override
    public List<Book> getBooksByCategory(String category) throws DAOException {
        return books.stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category))
                .toList();
    }
    
    @Override
    public List<Book> getBooksByAuthor(String author) throws DAOException {
        return books.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .toList();
    }
    
    @Override
    public List<Book> getAvailableBooks() throws DAOException {
        return books.stream()
                .filter(book -> book.getStock() > 0)
                .toList();
    }
    
    private boolean matchSearch(Book book, String searchText, String searchMode) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }
        
        String text = searchText.toLowerCase().trim();
        
        if ("author".equalsIgnoreCase(searchMode)) {
            return book.getAuthor().toLowerCase().contains(text);
        } else { // title è il default
            return book.getTitle().toLowerCase().contains(text);
        }
    }
    
    private void loadBooks() throws DAOException {
        books.clear();
        
        try {
            // Prova a caricare dalla risorsa
            URL resource = getClass().getClassLoader().getResource(FILE_PATH);
            if (resource != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.openStream()))) {
                    loadFromReader(reader);
                    return;
                }
            }
            
            // Prova a caricare dal filesystem
            Path path = Paths.get("src/main/resources", FILE_PATH);
            if (Files.exists(path)) {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    loadFromReader(reader);
                    return;
                }
            }
            
            // Se il file non esiste, crea uno di esempio
            createSampleData();
            saveBooks();
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il caricamento dei libri", e);
        }
    }
    
    private void createSampleData() {
        books.add(new Book(
                1,
                "Clean Code",
                "Robert C. Martin",
                "Programming",
                2008,
                "Prentice Hall",
                464,
                "9780132350884",
                5,
                "A Handbook of Agile Software Craftsmanship",
                null,
                39.99
        ));
    }
    
    private void loadFromReader(BufferedReader reader) throws IOException {
        String line = reader.readLine(); // Skip header
        
        int lineNumber = 1;
        while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
            try {
                Book book = parseBook(line, lineNumber);
                if (book != null) {
                    books.add(book);
                }
            } catch (Exception e) {
                System.err.println("Errore nel parsing della riga " + lineNumber + ": " + line);
                e.printStackTrace();
            }
            lineNumber++;
        }
    }
    
    private void saveBooks() throws DAOException {
        Path path = Paths.get("src/main/resources", FILE_PATH);
        
        try {
            Files.createDirectories(path.getParent());
            
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("id,title,author,category,year,publisher,pages,isbn,stock,plot,image_path,price");
                writer.newLine();
                
                for (Book book : books) {
                    writer.write(formatBook(book));
                    writer.newLine();
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il salvataggio dei libri", e);
        }
    }
    
    private Book parseBook(String line, int lineNumber) {
        List<String> fields = parseCSVLine(line);
        
        if (fields.size() < 12) {
            System.err.println("Linea " + lineNumber + ": Numero insufficiente di campi (" + fields.size() + " invece di 12)");
            return null;
        }
        
        try {
            return new Book(
                Integer.parseInt(fields.get(0).trim()),
                fields.get(1).trim(),
                fields.get(2).trim(),
                fields.get(3).trim(),
                Integer.parseInt(fields.get(4).trim()),
                fields.get(5).trim(),
                Integer.parseInt(fields.get(6).trim()),
                fields.get(7).trim(),
                Integer.parseInt(fields.get(8).trim()),
                fields.get(9).trim(),
                fields.get(10).trim(),
                Double.parseDouble(fields.get(11).trim())
            );
        } catch (NumberFormatException e) {
            System.err.println("Errore formato numerico in riga " + lineNumber + ": " + line);
            System.err.println("Campi: " + fields);
            return null;
        } catch (Exception e) {
            System.err.println("Errore generale in riga " + lineNumber + ": " + line);
            e.printStackTrace();
            return null;
        }
    }
    
    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields;
    }
    
    private String formatBook(Book book) {
        return String.join(",",
            String.valueOf(book.getId()),
            escapeComma(book.getTitle()),
            escapeComma(book.getAuthor()),
            escapeComma(book.getCategory()),
            String.valueOf(book.getYear()),
            escapeComma(book.getPublisher()),
            String.valueOf(book.getPages()),
            escapeComma(book.getIsbn()),
            String.valueOf(book.getStock()),
            escapeComma(book.getPlot()),
            escapeComma(book.getImagePath()),
            String.valueOf(book.getPrice())
        );
    }
    
    private String escapeComma(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}