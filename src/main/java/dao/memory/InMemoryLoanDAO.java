package dao.memory;

import dao.LoanDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Loan;
import utils.Constants;
import utils.LoanStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryLoanDAO implements LoanDAO {

    private final List<Loan> loans = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void addLoan(String userEmail, int bookId) throws DAOException {
        Loan loan = new Loan(
                nextId++,
                userEmail,
                bookId,
                LocalDate.now(),
                null,
                null,
                LoanStatus.RESERVED
        );
        loans.add(loan);
    }

    @Override
    public void updateLoanStatus(int loanId, String status) throws DAOException, RecordNotFoundException {
        Loan loan = getLoanById(loanId);
        loan.setStatus(LoanStatus.valueOf(status));
    }

    @Override
    public void deleteLoan(int loanId) throws DAOException, RecordNotFoundException {
        Optional<Loan> loanToRemove = loans.stream()
                .filter(l -> l.getId() == loanId)
                .findFirst();
        
        if (loanToRemove.isPresent()) {
            loans.remove(loanToRemove.get());
        } else {
            throw new RecordNotFoundException("Prestito con ID " + loanId + " non trovato");
        }
    }

    @Override
    public Loan getLoanById(int loanId) throws DAOException, RecordNotFoundException {
        return loans.stream()
                .filter(l -> l.getId() == loanId)
                .findFirst()
                .orElseThrow(() -> new RecordNotFoundException("Prestito con ID " + loanId + " non trovato"));
    }

    @Override
    public List<Loan> getLoansByUser(String userEmail) throws DAOException {
        return loans.stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .toList();
    }

    @Override
    public List<Loan> getActiveLoansByUser(String userEmail) throws DAOException {
        return loans.stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .filter(l -> l.getStatus() == LoanStatus.LOANED || l.getStatus() == LoanStatus.EXPIRED)
                .toList();
    }

    @Override
    public List<Loan> getReservedLoansByUser(String userEmail) throws DAOException {
        return loans.stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .filter(l -> l.getStatus() == LoanStatus.RESERVED)
                .toList();
    }

    @Override
    public List<Loan> getLoansByBook(int bookId) throws DAOException {
        return loans.stream()
                .filter(l -> l.getBookId() == bookId)
                .toList();
    }

    @Override
    public List<Loan> getLoansByStatus(String status) throws DAOException {
        LoanStatus loanStatus = LoanStatus.valueOf(status);
        return loans.stream()
                .filter(l -> l.getStatus() == loanStatus)
                .toList();
    }

    @Override
    public List<Loan> getAllReservedLoans() throws DAOException {
        return getLoansByStatus("RESERVED");
    }

    @Override
    public List<Loan> getAllActiveLoans() throws DAOException {
        return loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.LOANED || l.getStatus() == LoanStatus.EXPIRED)
                .toList();
    }

    @Override
    public List<Loan> getAllReturnedLoans() throws DAOException {
        return getLoansByStatus("RETURNED");
    }

    @Override
    public void acceptLoan(int loanId) throws DAOException, RecordNotFoundException {
        Loan loan = getLoanById(loanId);
        loan.setStatus(LoanStatus.LOANED);
        loan.setLoanedDate(LocalDate.now());
        loan.setReturningDate(LocalDate.now().plusDays(Constants.LOANING_DAYS));
    }

    @Override
    public void returnLoan(int loanId) throws DAOException, RecordNotFoundException {
        Loan loan = getLoanById(loanId);
        loan.setStatus(LoanStatus.RETURNED);
    }

    @Override
    public List<Loan> searchLoansByUser(String searchText) throws DAOException {
        String lowerSearch = searchText.toLowerCase();
        return loans.stream()
                .filter(l -> l.getUserEmail().toLowerCase().contains(lowerSearch))
                .toList();
    }

    @Override
    public List<Loan> searchLoansByBook(String searchText) throws DAOException {
        // In memoria non abbiamo informazioni sui libri, solo bookId
        // Questa implementazione richiederebbe un BookDAO per fare la ricerca
        return new ArrayList<>();
    }

    @Override
    public int countActiveLoansByUser(String userEmail) throws DAOException {
        return (int) loans.stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .filter(l -> l.getStatus() == LoanStatus.LOANED || l.getStatus() == LoanStatus.EXPIRED)
                .count();
    }

    @Override
    public boolean hasUserActiveLoans(String userEmail) throws DAOException {
        return countActiveLoansByUser(userEmail) > 0;
    }

    @Override
    public List<Loan> getExpiredLoans() throws DAOException {
        LocalDate today = LocalDate.now();
        return loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.LOANED)
                .filter(l -> l.getReturningDate() != null && l.getReturningDate().isBefore(today))
                .peek(l -> l.setStatus(LoanStatus.EXPIRED))
                .toList();
    }
}