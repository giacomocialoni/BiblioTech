package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void testBuilderAndGetters() {
        Book book = createCompleteBook();

        assertEquals(1, book.getId());
        assertEquals("Title", book.getTitle());
        assertEquals("Author", book.getAuthor());
        assertEquals("Fantasy", book.getCategory());
        assertEquals(2000, book.getYear());
        assertEquals("Publisher", book.getPublisher());
        assertEquals(350, book.getPages());
        assertEquals("1234567890123", book.getIsbn());
        assertEquals(5, book.getStock());
        assertEquals("A great book", book.getPlot());
        assertEquals("book.jpg", book.getImagePath());
        assertEquals(19.99, book.getPrice());
    }

    @Test
    void testSetters() {
        Book book = new Book(); // Usa il costruttore vuoto invece del builder
        setAllBookProperties(book);
        verifyAllBookProperties(book);
    }

    @Test
    void testEqualsAndHashCode() {
        Book b1 = createBasicBook(1, "A", "B");
        Book b2 = createBasicBook(1, "X", "Y");
        Book b3 = createBasicBook(2, "A", "B");

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
    }

    @Test
    void testBuilderWithMissingTitle() {
        executeBuilderThrowingException(() -> Book.builder().build());
    }

    @Test
    void testBuilderWithMissingAuthor() {
        executeBuilderThrowingException(() -> Book.builder().title("Only Title").build());
    }

    @Test
    void testBuilderWithRequiredFields() {
        Book book = Book.builder()
            .title("Valid Title")
            .author("Valid Author")
            .build();
            
        assertNotNull(book);
        assertEquals("Valid Title", book.getTitle());
        assertEquals("Valid Author", book.getAuthor());
    }

    @Test
    void testBuilderWithDefaultValues() {
        Book book = Book.builder()
            .title("Default Test")
            .author("Test Author")
            .build();

        assertEquals(0, book.getId());
        assertEquals(0, book.getYear());
        assertEquals(0, book.getPages());
        assertEquals(0, book.getStock());
        assertEquals(0.0, book.getPrice());
        assertNull(book.getPublisher());
        assertNull(book.getIsbn());
        assertNull(book.getPlot());
        assertNull(book.getImagePath()); // CORREZIONE: dovrebbe essere null, non "default.jpg"
    }

    // Helper methods to extract complex logic

    private Book createCompleteBook() {
        return Book.builder()
            .id(1)
            .title("Title")
            .author("Author")
            .category("Fantasy")
            .year(2000)
            .publisher("Publisher")
            .pages(350)
            .isbn("1234567890123")
            .stock(5)
            .plot("A great book")
            .imagePath("book.jpg")
            .price(19.99)
            .build();
    }

    private Book createBasicBook(int id, String title, String author) {
        return Book.builder()
            .id(id)
            .title(title)
            .author(author)
            .build();
    }

    private void setAllBookProperties(Book book) {
        book.setId(10);
        book.setTitle("Dune");
        book.setAuthor("Herbert");
        book.setCategory("Sci-Fi");
        book.setYear(1965);
        book.setPublisher("Ace Books");
        book.setPages(550);
        book.setIsbn("9780441013593");
        book.setStock(7);
        book.setPlot("Epic sci-fi novel");
        book.setImagePath("dune.jpg");
        book.setPrice(25.99);
    }

    private void verifyAllBookProperties(Book book) {
        assertEquals(10, book.getId());
        assertEquals("Dune", book.getTitle());
        assertEquals("Herbert", book.getAuthor());
        assertEquals("Sci-Fi", book.getCategory());
        assertEquals(1965, book.getYear());
        assertEquals("Ace Books", book.getPublisher());
        assertEquals(550, book.getPages());
        assertEquals("9780441013593", book.getIsbn());
        assertEquals(7, book.getStock());
        assertEquals("Epic sci-fi novel", book.getPlot());
        assertEquals("dune.jpg", book.getImagePath());
        assertEquals(25.99, book.getPrice());
    }

    private void executeBuilderThrowingException(Runnable builderAction) {
        assertThrows(IllegalArgumentException.class, builderAction::run);
    }
}