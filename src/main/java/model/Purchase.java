package model;

import java.time.LocalDate;
import utils.PurchaseStatus;

public class Purchase {
    private int id;
    private String userEmail;
    private int bookId;
    private LocalDate statusDate;
    private PurchaseStatus status;

    public Purchase(int id, String userEmail, int bookId, LocalDate statusDate, PurchaseStatus status) {
        this.id = id;
        this.userEmail = userEmail;
        this.bookId = bookId;
        this.statusDate = statusDate;
        this.status = status;
    }

    // Getter
    public int getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public int getBookId() { return bookId; }
    public LocalDate getStatusDate() { return statusDate; }
    public PurchaseStatus getStatus() { return status; }

    // Setter
    public void setId(int id) { this.id = id; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setStatusDate(LocalDate statusDate) { this.statusDate = statusDate; }
    public void setStatus(PurchaseStatus status) { 
        this.status = status; 
    }
    
    // Metodi utility
    public boolean isPurchased() { return status == PurchaseStatus.PURCHASED; }
    public boolean isReserved() { return status == PurchaseStatus.RESERVED; }
    
    @Override
    public String toString() {
        return "Purchase [id=" + id + ", userEmail=" + userEmail + ", bookId=" + bookId 
               + ", statusDate=" + statusDate + ", status=" + status + "]";
    }
}