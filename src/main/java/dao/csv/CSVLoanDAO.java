package dao.csv;

import dao.LoanDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Loan;
import utils.Constants;
import utils.LoanStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVLoanDAO implements LoanDAO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVLoanDAO.class);
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
                
                LOGGER.info("Prestito aggiunto: ID {} per utente {} libro {}", nextId, userEmail, bookId);
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante l'aggiunta del prestito per utente {} libro {}", userEmail, bookId, e);
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
        LOGGER.info("Stato prestito aggiornato: ID {} -> {}", loanId, status);
    }
    
    @Override
    public void deleteLoan(int loanId) throws DAOException, RecordNotFoundException {
        List<Loan> loans = loadAllLoans();
        boolean removed = loans.removeIf(l -> l.getId() == loanId);
        
        if (!removed) {
            LOGGER.warn("Tentativo di eliminazione prestito non trovato: ID {}", loanId);
            throw new RecordNotFoundException("Prestito con ID " + loanId + " non trovato");
        }
        
        saveAllLoans(loans);
        LOGGER.info("Prestito eliminato: ID {}", loanId);
    }
    
    @Override
    public Loan getLoanById(int loanId) throws DAOException, RecordNotFoundException {
        return loadAllLoans().stream()
                .filter(l -> l.getId() == loanId)
                .findFirst()
                .orElseThrow(() -> {
                    LOGGER.warn("Prestito non trovato: ID {}", loanId);
                    return new RecordNotFoundException("Prestito con ID " + loanId + " non trovato");
                });
    }
    
    @Override
    public List<Loan> getLoansByUser(String userEmail) throws DAOException {
        List<Loan> loans = loadAllLoans().stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .toList();
        LOGGER.debug("Recuperati {} prestiti per utente {}", loans.size(), userEmail);
        return loans;
    }
    
    @Override
    public List<Loan> getActiveLoansByUser(String userEmail) throws DAOException {
        List<Loan> loans = loadAllLoans().stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .filter(l -> l.getStatus() == LoanStatus.LOANED || l.getStatus() == LoanStatus.EXPIRED)
                .toList();
        LOGGER.debug("Recuperati {} prestiti attivi per utente {}", loans.size(), userEmail);
        return loans;
    }
    
    @Override
    public List<Loan> getReservedLoansByUser(String userEmail) throws DAOException {
        List<Loan> loans = loadAllLoans().stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .filter(l -> l.getStatus() == LoanStatus.RESERVED)
                .toList();
        LOGGER.debug("Recuperati {} prestiti riservati per utente {}", loans.size(), userEmail);
        return loans;
    }
    
    @Override
    public List<Loan> getLoansByBook(int bookId) throws DAOException {
        List<Loan> loans = loadAllLoans().stream()
                .filter(l -> l.getBookId() == bookId)
                .toList();
        LOGGER.debug("Recuperati {} prestiti per libro {}", loans.size(), bookId);
        return loans;
    }
    
    @Override
    public List<Loan> getLoansByStatus(String status) throws DAOException {
        LoanStatus loanStatus = LoanStatus.valueOf(status);
        List<Loan> loans = loadAllLoans().stream()
                .filter(l -> l.getStatus() == loanStatus)
                .toList();
        LOGGER.debug("Recuperati {} prestiti con stato {}", loans.size(), status);
        return loans;
    }
    
    @Override
    public List<Loan> getAllReservedLoans() throws DAOException {
        List<Loan> loans = getLoansByStatus("RESERVED");
        LOGGER.debug("Recuperati tutti i {} prestiti riservati", loans.size());
        return loans;
    }
    
    @Override
    public List<Loan> getAllActiveLoans() throws DAOException {
        List<Loan> loans = loadAllLoans().stream()
                .filter(l -> l.getStatus() == LoanStatus.LOANED || l.getStatus() == LoanStatus.EXPIRED)
                .toList();
        LOGGER.debug("Recuperati tutti i {} prestiti attivi", loans.size());
        return loans;
    }
    
    @Override
    public List<Loan> getAllReturnedLoans() throws DAOException {
        List<Loan> loans = getLoansByStatus("RETURNED");
        LOGGER.debug("Recuperati tutti i {} prestiti restituiti", loans.size());
        return loans;
    }
    
    @Override
    public void acceptLoan(int loanId) throws DAOException, RecordNotFoundException {
        updateLoan(loanId, loan -> {
            loan.setStatus(LoanStatus.LOANED);
            loan.setLoanedDate(LocalDate.now());
            loan.setReturningDate(LocalDate.now().plusDays(Constants.LOANING_DAYS));
        });
        LOGGER.info("Prestito accettato: ID {}", loanId);
    }
    
    @Override
    public void returnLoan(int loanId) throws DAOException, RecordNotFoundException {
        updateLoan(loanId, loan -> {
            loan.setStatus(LoanStatus.RETURNED);
        });
        LOGGER.info("Prestito restituito: ID {}", loanId);
    }
    
    @Override
    public List<Loan> searchLoansByUser(String searchText) throws DAOException {
        String lowerSearch = searchText.toLowerCase();
        List<Loan> loans = loadAllLoans().stream()
                .filter(l -> l.getUserEmail().toLowerCase().contains(lowerSearch))
                .toList();
        LOGGER.debug("Ricerca prestiti per utente '{}': trovati {} risultati", searchText, loans.size());
        return loans;
    }
    
    @Override
    public List<Loan> searchLoansByBook(String searchText) throws DAOException {
        // In CSV senza join con books, non possiamo cercare per titolo/autore
        LOGGER.warn("Ricerca prestiti per libro non supportata in modalità CSV");
        return new ArrayList<>();
    }
    
    @Override
    public int countActiveLoansByUser(String userEmail) throws DAOException {
        int count = (int) loadAllLoans().stream()
                .filter(l -> l.getUserEmail().equals(userEmail))
                .filter(l -> l.getStatus() == LoanStatus.LOANED || l.getStatus() == LoanStatus.EXPIRED)
                .count();
        LOGGER.debug("Contati {} prestiti attivi per utente {}", count, userEmail);
        return count;
    }
    
    @Override
    public boolean hasUserActiveLoans(String userEmail) throws DAOException {
        boolean hasLoans = countActiveLoansByUser(userEmail) > 0;
        LOGGER.debug("Utente {} ha prestiti attivi: {}", userEmail, hasLoans);
        return hasLoans;
    }
    
    @Override
    public List<Loan> getExpiredLoans() throws DAOException {
        LocalDate today = LocalDate.now();
        List<Loan> expiredLoans = loadAllLoans().stream()
                .filter(l -> l.getStatus() == LoanStatus.LOANED)
                .filter(l -> l.getReturningDate() != null && l.getReturningDate().isBefore(today))
                .peek(l -> l.setStatus(LoanStatus.EXPIRED))
                .toList();
        
        if (!expiredLoans.isEmpty()) {
            LOGGER.info("Trovati {} prestiti scaduti", expiredLoans.size());
            // Salva i cambiamenti di stato
            saveAllLoans(loadAllLoans());
        }
        
        return expiredLoans;
    }
    
    private List<Loan> loadAllLoans() throws DAOException {
        List<Loan> loans = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.debug("File prestiti non trovato, restituita lista vuota");
            return loans;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 1;
            int errors = 0;
            
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                try {
                    Loan loan = parseLoan(line);
                    if (loan != null) {
                        loans.add(loan);
                    } else {
                        errors++;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Errore nel parsing del prestito riga {}: {}", lineNumber, line, e);
                    errors++;
                }
                lineNumber++;
            }
            
            if (errors > 0) {
                LOGGER.warn("Caricamento prestiti completato con {} errori", errors);
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la lettura dei prestiti", e);
            throw new DAOException("Errore durante la lettura dei prestiti", e);
        }
        
        LOGGER.debug("Caricati {} prestiti", loans.size());
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
            LOGGER.error("Errore durante il salvataggio dei prestiti", e);
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
            LOGGER.warn("Errore nel parsing della riga prestito: {}", line, e);
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
        int nextId = loans.stream()
                .mapToInt(Loan::getId)
                .max()
                .orElse(0) + 1;
        LOGGER.debug("Nuovo ID prestito generato: {}", nextId);
        return nextId;
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
            LOGGER.warn("Prestito non trovato per aggiornamento: ID {}", loanId);
            throw new RecordNotFoundException("Prestito con ID " + loanId + " non trovato");
        }
        
        saveAllLoans(loans);
    }
    
    @FunctionalInterface
    private interface LoanUpdater {
        void update(Loan loan);
    }
}