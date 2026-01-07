package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void testFullConstructorAndGetters() {
        Book b = new Book(
            1, "Title", "Author", "Fantasy",
            2000, "Publisher", 350, "1234567890123",
            5, "A great book", "book.jpg", 19.99
        );

        assertEquals(1, b.getId());
        assertEquals("Title", b.getTitle());
        assertEquals("Author", b.getAuthor());
        assertEquals("Fantasy", b.getCategory());
        assertEquals(2000, b.getYear());
        assertEquals("Publisher", b.getPublisher());
        assertEquals(350, b.getPages());
        assertEquals("1234567890123", b.getIsbn());
        assertEquals(5, b.getStock());
        assertEquals("A great book", b.getPlot());
        assertEquals("book.jpg", b.getImagePath());
        assertEquals(19.99, b.getPrice());
    }

    @Test
    void testSetters() {
        Book b = new Book();
        b.setId(10);
        b.setTitle("Dune");
        b.setAuthor("Herbert");
        b.setCategory("Sci-Fi");
        b.setYear(1965);
        b.setPublisher("Ace Books");
        b.setPages(550);
        b.setIsbn("9780441013593");
        b.setStock(7);
        b.setPlot("Epic sci-fi novel");
        b.setImagePath("dune.jpg");

        assertEquals(10, b.getId());
        assertEquals("Dune", b.getTitle());
        assertEquals("Herbert", b.getAuthor());
        assertEquals("Sci-Fi", b.getCategory());
        assertEquals(1965, b.getYear());
        assertEquals("Ace Books", b.getPublisher());
        assertEquals(550, b.getPages());
        assertEquals("9780441013593", b.getIsbn());
        assertEquals(7, b.getStock());
        assertEquals("Epic sci-fi novel", b.getPlot());
        assertEquals("dune.jpg", b.getImagePath());
    }

    @Test
    void testEqualsAndHashCode() {
        Book b1 = new Book(1, "A", "B", "", 0, "", 0, "", 0, "", "", 0);
        Book b2 = new Book(1, "X", "Y", "", 0, "", 0, "", 0, "", "", 0);
        Book b3 = new Book(2, "A", "B", "", 0, "", 0, "", 0, "", "", 0);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());

        assertNotEquals(b1, b3);
    }
}