package model;

import java.util.Objects;

public class Book {
    private int id;
    private String title;
    private String author;
    private String category;
    private int year;
    private String publisher;
    private int pages;
    private String isbn;
    private int stock;
    private String plot;
    private String imagePath;
    private double price;

    Book() {}

    // Builder class
    public static class Builder {
        private final Book book = new Book();
        
        public Builder id(int id) {
            book.id = id;
            return this;
        }
        
        public Builder title(String title) {
            book.title = title;
            return this;
        }
        
        public Builder author(String author) {
            book.author = author;
            return this;
        }
        
        public Builder category(String category) {
            book.category = category;
            return this;
        }
        
        public Builder year(int year) {
            book.year = year;
            return this;
        }
        
        public Builder publisher(String publisher) {
            book.publisher = publisher;
            return this;
        }
        
        public Builder pages(int pages) {
            book.pages = pages;
            return this;
        }
        
        public Builder isbn(String isbn) {
            book.isbn = isbn;
            return this;
        }
        
        public Builder stock(int stock) {
            book.stock = stock;
            return this;
        }
        
        public Builder plot(String plot) {
            book.plot = plot;
            return this;
        }
        
        public Builder imagePath(String imagePath) {
            book.imagePath = imagePath;
            return this;
        }
        
        public Builder price(double price) {
            book.price = price;
            return this;
        }
        
        public Book build() {
            // Validazioni opzionali
            if (book.title == null || book.title.isBlank()) {
                throw new IllegalArgumentException("Title is required");
            }
            if (book.author == null || book.author.isBlank()) {
                throw new IllegalArgumentException("Author is required");
            }
            return book;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }

    // --- GETTER & SETTER (rimangono uguali) ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // --- EQUALITY ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return id == book.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}