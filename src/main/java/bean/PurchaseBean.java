package bean;

import java.time.LocalDate;
import exception.IncorrectDataException;
import utils.PurchaseStatus;

public class PurchaseBean {
    private int id;
    private String userEmail;
    private int bookId;
    private int quantity;  // Campo aggiunto
    private LocalDate statusDate;
    private PurchaseStatus status;
    private BookBean book; // completo per GUI

    public int getId() { return id; }
    public void setId(int id) throws IncorrectDataException {
        if (id <= 0) throw new IncorrectDataException("Purchase id non valido");
        this.id = id;
    }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) throws IncorrectDataException {
        if (userEmail == null || userEmail.isBlank()) throw new IncorrectDataException("Email utente non valida");
        this.userEmail = userEmail;
    }
    
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) throws IncorrectDataException {
        if (bookId <= 0) throw new IncorrectDataException("Book id non valido");
        this.bookId = bookId;
    }
    
    public int getQuantity() { return quantity; }  // Getter aggiunto
    public void setQuantity(int quantity) throws IncorrectDataException {  // Setter aggiunto
        if (quantity <= 0) throw new IncorrectDataException("Quantità non valida");
        this.quantity = quantity;
    }
    
    public LocalDate getStatusDate() { return statusDate; }
    public void setStatusDate(LocalDate statusDate) throws IncorrectDataException {
        if (statusDate == null) throw new IncorrectDataException("Data stato acquisto non valida");
        this.statusDate = statusDate;
    }
    
    public PurchaseStatus getStatus() { return status; }
    public void setStatus(PurchaseStatus status) throws IncorrectDataException {
        if (status == null) throw new IncorrectDataException("Stato acquisto non valido");
        this.status = status;
    }
    
    public BookBean getBook() { return book; }
    public void setBook(BookBean book) { this.book = book; }

    public boolean isPurchased() { return status == PurchaseStatus.PURCHASED; }
    public boolean isReserved() { return status == PurchaseStatus.RESERVED; }
}