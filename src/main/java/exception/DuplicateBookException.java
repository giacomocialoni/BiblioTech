package exception;

@SuppressWarnings("serial")
public class DuplicateBookException extends Exception {
    
    private final String isbn;
    private final String title;
    private final String duplicateType;
    
    public DuplicateBookException(String isbn, String title, String duplicateType) {
        super(createMessage(isbn, title, duplicateType));
        this.isbn = isbn;
        this.title = title;
        this.duplicateType = duplicateType;
    }
    
    private static String createMessage(String isbn, String title, String duplicateType) {
        if ("isbn".equals(duplicateType)) {
            return "Un libro con ISBN '" + isbn + "' esiste già.";
        } else if ("title".equals(duplicateType)) {
            return "Un libro con titolo '" + title + "' esiste già.";
        }
        return "Libro duplicato.";
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDuplicateType() {
        return duplicateType;
    }
    
    public String getUserFriendlyMessage() {
        if ("isbn".equals(duplicateType)) {
            return "ISBN già presente nel sistema: " + isbn;
        } else if ("title".equals(duplicateType)) {
            return "Titolo già presente: " + title;
        }
        return "Libro già presente nel catalogo.";
    }
}