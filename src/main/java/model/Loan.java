package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import utils.LoanStatus;

public class Loan {
    private int id;
    private String userEmail;
    private int bookId;
    private LocalDate reservedDate;
    private LocalDate loanedDate;
    private LocalDate returningDate;
    private LoanStatus status;

    public Loan(int id, String userEmail, int bookId, LocalDate reservedDate, 
                LocalDate loanedDate, LocalDate returningDate, LoanStatus status) {
        this.id = id;
        this.userEmail = userEmail;
        this.bookId = bookId;
        this.reservedDate = reservedDate;
        this.loanedDate = loanedDate;
        this.returningDate = returningDate;
        this.status = status;
    }

    // Getter
    public int getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public int getBookId() { return bookId; }
    public LocalDate getReservedDate() { return reservedDate; }
    public LocalDate getLoanedDate() { return loanedDate; }
    public LocalDate getReturningDate() { return returningDate; }
    public LoanStatus getStatus() { return status; }

    // Setter
    public void setId(int id) { this.id = id; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setReservedDate(LocalDate reservedDate) { this.reservedDate = reservedDate; }
    public void setLoanedDate(LocalDate loanedDate) { this.loanedDate = loanedDate; }
    public void setReturningDate(LocalDate returningDate) { this.returningDate = returningDate; }
    public void setStatus(LoanStatus status) { 
        this.status = status; 
    }
    
    // Metodi utility
    public boolean isReturned() { return status == LoanStatus.RETURNED; }
    public boolean isExpired() {
        return returningDate != null && LocalDate.now().isAfter(returningDate);
    }
    public long daysRemaining() {
        return returningDate != null ? ChronoUnit.DAYS.between(LocalDate.now(), returningDate) : -1;
    }
    
    // Metodi helper per gestione stato
    public void markAsLoaned(LocalDate loanDate, int loanDurationDays) {
        this.status = LoanStatus.LOANED;
        this.loanedDate = loanDate;
        this.returningDate = loanDate.plusDays(loanDurationDays);
    }
    
    public void markAsReturned() {
        this.status = LoanStatus.RETURNED;
    }
    
    public void markAsExpired() {
        this.status = LoanStatus.EXPIRED;
    }
    
    @Override
    public String toString() {
        return "Loan [id=" + id + ", userEmail=" + userEmail + ", bookId=" + bookId 
               + ", status=" + status + ", reservedDate=" + reservedDate 
               + ", loanedDate=" + loanedDate + ", returningDate=" + returningDate + "]";
    }
}