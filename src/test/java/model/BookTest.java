package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void testBuilderAndGetters() {
        Book book = Book.builder()
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
        Book book = Book.builder().build();
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

    @Test
    void testEqualsAndHashCode() {
        Book b1 = Book.builder()
            .id(1)
            .title("A")
            .author("B")
            .build();
            
        Book b2 = Book.builder()
            .id(1)
            .title("X")
            .author("Y")
            .build();
            
        Book b3 = Book.builder()
            .id(2)
            .title("A")
            .author("B")
            .build();

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
    }

    @Test
    void testBuilderWithMissingRequiredFields() {
        // Single assertion with lambda that may throw runtime exception
        assertThrows(IllegalArgumentException.class, () -> Book.builder().build());
        
        // Another single assertion for missing author
        assertThrows(IllegalArgumentException.class, () -> Book.builder().title("Only Title").build());
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
        assertEquals("default.jpg", book.getImagePath());
    }
}