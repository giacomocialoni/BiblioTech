package dao.csv;

import dao.BookDAO;
import exception.DAOException;
import exception.DuplicateBookException;
import model.Book;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVBookDAO implements BookDAO {

    private static final String FILE_PATH = "src/main/resources/data/book.csv";
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CSVBookDAO.class);
    // Nuovo ordine delle colonne basato sul tuo file CSV
    private static final String[] COLUMNS = {
        "id", "title", "author", "year", "plot", "image_path", 
        "publisher", "pages", "isbn", "stock", "category", "price"
    };
    
    private static final String CSV_HEADER = String.join(",", COLUMNS);

    private final List<Book> books = new ArrayList<>();

    private void ensureLoaded() throws DAOException {
        if (books.isEmpty()) {
            loadBooks();
        }
    }

    @Override
    public List<Book> getAllBooks() throws DAOException {
        ensureLoaded();
        return new ArrayList<>(books);
    }

    @Override
    public Book getBookById(int id) throws DAOException {
        ensureLoaded();
        return books.stream()
                .filter(book -> book.getId() == id)
                .findFirst()
                .orElseThrow(() -> new DAOException("Libro con ID " + id + " non trovato"));
    }

    @Override
    public void addBook(Book book) throws DAOException {
        ensureLoaded();
        validateBookForAddition(book);

        if (book.getId() <= 0) {
            int maxId = books.stream().mapToInt(Book::getId).max().orElse(0);
            book.setId(maxId + 1);
        }

        books.add(book);
        saveBooks();
    }

    private void validateBookForAddition(Book book) throws DAOException {
        ensureLoaded();

        boolean isDuplicateISBN = books.stream()
                .anyMatch(b -> b.getIsbn().equals(book.getIsbn()));
        boolean isDuplicateTitleAuthor = books.stream()
                .anyMatch(b -> b.getTitle().equalsIgnoreCase(book.getTitle())
                        && b.getAuthor().equalsIgnoreCase(book.getAuthor()));

        if (isDuplicateISBN) {
            throw new DuplicateBookException(book.getIsbn(), book.getTitle(), "isbn");
        }
        if (isDuplicateTitleAuthor) {
            throw new DuplicateBookException(book.getIsbn(), book.getTitle(), "title");
        }
    }

    @Override
    public void updateBook(Book book) throws DAOException {
        ensureLoaded();
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == book.getId()) {
                books.set(i, book);
                saveBooks();
                return;
            }
        }
        throw new DAOException("Libro con ID " + book.getId() + " non trovato");
    }

    @Override
    public void deleteBook(int id) throws DAOException {
        ensureLoaded();
        boolean removed = books.removeIf(book -> book.getId() == id);
        if (!removed) {
            throw new DAOException("Libro con ID " + id + " non trovato");
        }
        saveBooks();
    }

    @Override
    public List<Book> searchBooks(String searchText, String searchMode, String category,
                                  String yearFrom, String yearTo, boolean includeUnavailable) throws DAOException {
        ensureLoaded();
        return books.stream()
                .filter(book -> includeUnavailable || book.getStock() > 0)
                .filter(book -> category == null || category.isEmpty() ||
                        book.getCategory().equalsIgnoreCase(category))
                .filter(book -> filterByYearFrom(book, yearFrom))
                .filter(book -> filterByYearTo(book, yearTo))
                .filter(book -> matchSearch(book, searchText, searchMode))
                .toList();
    }

    private boolean filterByYearFrom(Book book, String yearFrom) {
        if (yearFrom != null && !yearFrom.isEmpty()) {
            try {
                int yearFromInt = Integer.parseInt(yearFrom);
                return book.getYear() >= yearFromInt;
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return true;
    }

    private boolean filterByYearTo(Book book, String yearTo) {
        if (yearTo != null && !yearTo.isEmpty()) {
            try {
                int yearToInt = Integer.parseInt(yearTo);
                return book.getYear() <= yearToInt;
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return true;
    }

    @Override
    public void updateStock(int bookId, int quantity) throws DAOException {
        ensureLoaded();
        for (Book book : books) {
            if (book.getId() == bookId) {
                int newStock = Math.max(0, book.getStock() + quantity);
                book.setStock(newStock);
                saveBooks();
                return;
            }
        }
        throw new DAOException("Libro con ID " + bookId + " non trovato");
    }

    @Override
    public boolean isBookAvailable(int bookId) throws DAOException {
        ensureLoaded();
        return books.stream()
                .filter(book -> book.getId() == bookId)
                .findFirst()
                .map(book -> book.getStock() > 0)
                .orElse(false);
    }

    @Override
    public List<Book> getBooksByCategory(String category) throws DAOException {
        ensureLoaded();
        return books.stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @Override
    public List<Book> getBooksByAuthor(String author) throws DAOException {
        ensureLoaded();
        return books.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .toList();
    }

    @Override
    public List<Book> getAvailableBooks() throws DAOException {
        ensureLoaded();
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
        } else {
            return book.getTitle().toLowerCase().contains(text);
        }
    }

    private void loadBooks() throws DAOException {
        books.clear();
        try {
            URL resource = getClass().getClassLoader().getResource("data/book.csv");
            if (resource != null) {
                loadFromResource(resource);
            } else {
                Path path = Paths.get(FILE_PATH);
                if (Files.exists(path)) {
                    loadFromFile(path);
                } else {
                    saveBooks();
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore durante il caricamento dei libri", e);
        }
    }

    private void loadFromResource(URL resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
            loadFromReader(reader);
        }
    }

    private void loadFromFile(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            loadFromReader(reader);
        }
    }

    private void loadFromReader(BufferedReader reader) throws IOException {
        String header = reader.readLine();
        if (header == null) {
            throw new IOException("File CSV dei libri non valido: header mancante");
        }

        String[] actualColumns = header.split(",");
        if (actualColumns.length < COLUMNS.length) {
            throw new IOException("File CSV dei libri non valido: colonne insufficienti");
        }

        String line;
        int lineNumber = 1;
        while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
            try {
                Book book = parseBook(line);
                if (book != null) {
                    books.add(book);
                }
            } catch (Exception e) {
                LOGGER.error("Errore parsing riga " + lineNumber + ": ", e);
            }
            lineNumber++;
        }
    }

    private void saveBooks() throws DAOException {
        Path path = Paths.get(FILE_PATH);
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(CSV_HEADER);
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

    private Book parseBook(String line) {
        List<String> fields = parseCSVLine(line);
        if (fields.size() < COLUMNS.length) return null;

        try {
            return Book.builder()
                    .id(Integer.parseInt(fields.get(0)))        // id
                    .title(fields.get(1))                      // title
                    .author(fields.get(2))                     // author
                    .year(Integer.parseInt(fields.get(3)))     // year
                    .plot(fields.get(4))                       // plot
                    .imagePath(fields.get(5))                  // image_path
                    .publisher(fields.get(6))                  // publisher
                    .pages(Integer.parseInt(fields.get(7)))    // pages
                    .isbn(fields.get(8))                       // isbn
                    .stock(Integer.parseInt(fields.get(9)))    // stock
                    .category(fields.get(10))                  // category
                    .price(Double.parseDouble(fields.get(11))) // price
                    .build();
        } catch (NumberFormatException e) {
            LOGGER.error("Errore parsing numerico: ", e);
            return null;
        } catch (Exception e) {
            LOGGER.error("Errore parsing libro: ", e);
            return null;
        }
    }

    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote ""
                    currentField.append('"');
                    i += 2;
                    continue;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }

            i++;
        }

        fields.add(currentField.toString());
        return fields;
    }

    private String formatBook(Book book) {
        return String.join(",",
                String.valueOf(book.getId()),
                escapeCSV(book.getTitle()),
                escapeCSV(book.getAuthor()),
                String.valueOf(book.getYear()),
                escapeCSV(book.getPlot()),
                escapeCSV(book.getImagePath() != null ? book.getImagePath() : ""),
                escapeCSV(book.getPublisher()),
                String.valueOf(book.getPages()),
                escapeCSV(book.getIsbn()),
                String.valueOf(book.getStock()),
                escapeCSV(book.getCategory()),
                String.valueOf(book.getPrice())
        );
    }

    private String escapeCSV(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}