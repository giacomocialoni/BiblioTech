package dao.csv;

import dao.LoanDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Loan;
import utils.Constants;
import utils.LoanStatus;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVLoanDAO implements LoanDAO {
    
    private static final String FILE_PATH = "src/main/resources/data/loans.csv";
    
    @Override
    public void addLoan(String userEmail, int bookId) throws DAOException {
        try {
            Path path = Paths.get(FILE_PATH);
            boolean fileExists = Files.exists(path);
            
            try (BufferedWriter writer = Files.newBufferedWriter(path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                
                if (!fileExists || Files.size(path) == 0) {
                    writer.write("id,user_email,book_id,status,reserved_date,loaned_date,returning_date");
                    writer.newLine();
                }
                
                int nextId = getNextId();
                String line = String.join(",",
                    String.valueOf(nextId),
                    userEmail,
                    String.valueOf(bookId),
                    "RESERVED",
                    LocalDate.now().toString(),
                    "",
                    ""
                );
                
                writer.write(line);
                writer.newLine();
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiunta del prestito", e);
        }
    }
    
    @Override
    public void updateLoanStatus(int loanId, String status) throws DAOException, RecordNotFoundException {
        updateLoan(loanId, loan -> {
            loan.setStatus(LoanStatus.valueOf(status));
            if ("LOANED".equals(status)) {
                loan.setLoanedDate(LocalDate.now());
                loan.setReturningDate(LocalDate.now().plusDays(Constants.LOANING_DAYS));
            }
        });
    }
    
    @Override
    public void deleteLoan(int loanId) throws DAOException, RecordNotFoundException {
        List<Loan> loans = loadAllLoans();
        boolean removed = loans.removeIf(l -> l.getId() == loanId);
        
        if (!removed) {
            throw new RecordNotFoundException("Prestito con ID " + loanId + " non trovato");
        }
        
        saveAllLoans(loans);
    }
    
    @Override
    public Loan getLoanById(int loanId) throws DAOException, RecordNotFoundException {
        return loadAllLoans().stream()
                .filter(l -> l.getId() == loanId)
                .findFirst()
                .orElseThrow(() -> new RecordNotFoundException("Prestito con ID " + loanId + " non trovato"));
    }
    
    @Override
    public List<Loan> getLoansByUser(String userEmail) throws DAOException {
        return loadAllLoans().stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .toList();
    }
    
    @Override
    public List<Loan> getActiveLoansByUser(String userEmail) throws DAOException {
        return loadAllLoans().stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .filter(l -> l.getStatus() == LoanStatus.LOANED || l.getStatus() == LoanStatus.EXPIRED)
                .toList();
    }
    
    @Override
    public List<Loan> getReservedLoansByUser(String userEmail) throws DAOException {
        return loadAllLoans().stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .filter(l -> l.getStatus() == LoanStatus.RESERVED)
                .toList();
    }
    
    @Override
    public List<Loan> getLoansByBook(int bookId) throws DAOException {
        return loadAllLoans().stream()
                .filter(l -> l.getBookId() == bookId)
                .toList();
    }
    
    @Override
    public List<Loan> getLoansByStatus(String status) throws DAOException {
        LoanStatus loanStatus = LoanStatus.valueOf(status);
        return loadAllLoans().stream()
                .filter(l -> l.getStatus() == loanStatus)
                .toList();
    }
    
    @Override
    public List<Loan> getAllReservedLoans() throws DAOException {
        return getLoansByStatus("RESERVED");
    }
    
    @Override
    public List<Loan> getAllActiveLoans() throws DAOException {
        return loadAllLoans().stream()
                .filter(l -> l.getStatus() == LoanStatus.LOANED || l.getStatus() == LoanStatus.EXPIRED)
                .toList();
    }
    
    @Override
    public List<Loan> getAllReturnedLoans() throws DAOException {
        return getLoansByStatus("RETURNED");
    }
    
    @Override
    public void acceptLoan(int loanId) throws DAOException, RecordNotFoundException {
        updateLoan(loanId, loan -> {
            loan.setStatus(LoanStatus.LOANED);
            loan.setLoanedDate(LocalDate.now());
            loan.setReturningDate(LocalDate.now().plusDays(Constants.LOANING_DAYS));
        });
    }
    
    @Override
    public void returnLoan(int loanId) throws DAOException, RecordNotFoundException {
        updateLoan(loanId, loan -> {
            loan.setStatus(LoanStatus.RETURNED);
        });
    }
    
    @Override
    public List<Loan> searchLoansByUser(String searchText) throws DAOException {
        String lowerSearch = searchText.toLowerCase();
        return loadAllLoans().stream()
                .filter(l -> l.getUserEmail().toLowerCase().contains(lowerSearch))
                .toList();
    }
    
    @Override
    public List<Loan> searchLoansByBook(String searchText) throws DAOException {
        // In CSV senza join con books, non possiamo cercare per titolo/autore
        return new ArrayList<>();
    }
    
    @Override
    public int countActiveLoansByUser(String userEmail) throws DAOException {
        return (int) loadAllLoans().stream()
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
        return loadAllLoans().stream()
                .filter(l -> l.getStatus() == LoanStatus.LOANED)
                .filter(l -> l.getReturningDate() != null && l.getReturningDate().isBefore(today))
                .peek(l -> l.setStatus(LoanStatus.EXPIRED))
                .toList();
    }
    
    private List<Loan> loadAllLoans() throws DAOException {
        List<Loan> loans = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return loans;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                try {
                    Loan loan = parseLoan(line);
                    if (loan != null) {
                        loans.add(loan);
                    }
                } catch (Exception e) {
                    System.err.println("Errore nel parsing del prestito: " + line);
                    e.printStackTrace();
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura dei prestiti", e);
        }
        
        return loans;
    }
    
    private void saveAllLoans(List<Loan> loans) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        
        try {
            Files.createDirectories(path.getParent());
            
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("id,user_email,book_id,status,reserved_date,loaned_date,returning_date");
                writer.newLine();
                
                for (Loan loan : loans) {
                    writer.write(formatLoan(loan));
                    writer.newLine();
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante il salvataggio dei prestiti", e);
        }
    }
    
    private Loan parseLoan(String line) {
        String[] fields = line.split(",", -1);
        
        try {
            int id = Integer.parseInt(fields[0]);
            String userEmail = fields[1];
            int bookId = Integer.parseInt(fields[2]);
            LoanStatus status = LoanStatus.valueOf(fields[3]);
            
            LocalDate reservedDate = fields[4].isEmpty() ? null : LocalDate.parse(fields[4]);
            LocalDate loanedDate = fields[5].isEmpty() ? null : LocalDate.parse(fields[5]);
            LocalDate returningDate = fields[6].isEmpty() ? null : LocalDate.parse(fields[6]);
            
            return new Loan(id, userEmail, bookId, reservedDate, loanedDate, returningDate, status);
            
        } catch (Exception e) {
            System.err.println("Errore nel parsing della riga: " + line);
            e.printStackTrace();
            return null;
        }
    }
    
    private String formatLoan(Loan loan) {
        return String.join(",",
            String.valueOf(loan.getId()),
            loan.getUserEmail(),
            String.valueOf(loan.getBookId()),
            loan.getStatus().name(),
            loan.getReservedDate() != null ? loan.getReservedDate().toString() : "",
            loan.getLoanedDate() != null ? loan.getLoanedDate().toString() : "",
            loan.getReturningDate() != null ? loan.getReturningDate().toString() : ""
        );
    }
    
    private int getNextId() throws DAOException {
        List<Loan> loans = loadAllLoans();
        return loans.stream()
                .mapToInt(Loan::getId)
                .max()
                .orElse(0) + 1;
    }
    
    private void updateLoan(int loanId, LoanUpdater updater) throws DAOException, RecordNotFoundException {
        List<Loan> loans = loadAllLoans();
        boolean found = false;
        
        for (Loan loan : loans) {
            if (loan.getId() == loanId) {
                updater.update(loan);
                found = true;
                break;
            }
        }
        
        if (!found) {
            throw new RecordNotFoundException("Prestito con ID " + loanId + " non trovato");
        }
        
        saveAllLoans(loans);
    }
    
    @FunctionalInterface
    private interface LoanUpdater {
        void update(Loan loan);
    }
}