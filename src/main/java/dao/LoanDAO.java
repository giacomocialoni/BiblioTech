package dao;

import model.Loan;
import exception.DAOException;
import exception.RecordNotFoundException;

import java.util.List;

public interface LoanDAO {
    // Operazioni CRUD
    void addLoan(String userEmail, int bookId) throws DAOException;
    void updateLoanStatus(int loanId, String status) throws DAOException, RecordNotFoundException;
    void deleteLoan(int loanId) throws DAOException, RecordNotFoundException;
    
    // Recupero prestiti
    Loan getLoanById(int loanId) throws DAOException, RecordNotFoundException;
    List<Loan> getLoansByUser(String userEmail) throws DAOException;
    List<Loan> getActiveLoansByUser(String userEmail) throws DAOException;
    List<Loan> getReservedLoansByUser(String userEmail) throws DAOException;
    List<Loan> getLoansByBook(int bookId) throws DAOException;
    
    // Recupero per stato
    List<Loan> getLoansByStatus(String status) throws DAOException;
    List<Loan> getAllReservedLoans() throws DAOException;
    List<Loan> getAllActiveLoans() throws DAOException; // LOANED + EXPIRED
    List<Loan> getAllReturnedLoans() throws DAOException;
    
    // Operazioni business
    void acceptLoan(int loanId) throws DAOException, RecordNotFoundException;
    void returnLoan(int loanId) throws DAOException, RecordNotFoundException;
    
    // Ricerca
    List<Loan> searchLoansByUser(String searchText) throws DAOException;
    List<Loan> searchLoansByBook(String searchText) throws DAOException;
    
    // Statistiche
    int countActiveLoansByUser(String userEmail) throws DAOException;
    boolean hasUserActiveLoans(String userEmail) throws DAOException;
    List<Loan> getExpiredLoans() throws DAOException;
}