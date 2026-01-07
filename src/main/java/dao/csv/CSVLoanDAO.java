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
    private static final String CSV_HEADER =
            "id,user_email,book_id,status,reserved_date,loaned_date,returning_date";

    @Override
    public void addLoan(String userEmail, int bookId) throws DAOException {
        Path path = Paths.get(FILE_PATH);

        try {
            Files.createDirectories(path.getParent());
            boolean writeHeader = !Files.exists(path) || Files.size(path) == 0;

            try (BufferedWriter writer = Files.newBufferedWriter(
                    path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                if (writeHeader) {
                    writer.write(CSV_HEADER);
                    writer.newLine();
                }

                int nextId = getNextId();
                writer.write(formatLoan(new Loan(
                        nextId,
                        userEmail,
                        bookId,
                        LocalDate.now(),
                        null,
                        null,
                        LoanStatus.RESERVED
                )));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiunta del prestito CSV", e);
        }
    }

    @Override
    public void updateLoanStatus(int loanId, String status) throws DAOException, RecordNotFoundException {
        LoanStatus newStatus = LoanStatus.valueOf(status);
        updateLoanInternal(loanId, loan -> loan.setStatus(newStatus));

        if (newStatus == LoanStatus.LOANED) {
            updateLoanInternal(loanId, loan -> {
                loan.setLoanedDate(LocalDate.now());
                loan.setReturningDate(LocalDate.now().plusDays(Constants.LOANING_DAYS));
            });
        }
    }

    @Override
    public void acceptLoan(int loanId) throws DAOException, RecordNotFoundException {
        updateLoanStatus(loanId, LoanStatus.LOANED.name());
    }

    @Override
    public void returnLoan(int loanId) throws DAOException, RecordNotFoundException {
        updateLoanInternal(loanId, loan -> loan.setStatus(LoanStatus.RETURNED));
    }

    @Override
    public void deleteLoan(int loanId) throws DAOException, RecordNotFoundException {
        List<Loan> loans = loadAllLoans();

        boolean removed = loans.removeIf(l -> l.getId() == loanId);
        if (!removed) {
            throw new RecordNotFoundException("Prestito non trovato: ID " + loanId);
        }

        saveAllLoans(loans);
    }

    @Override
    public Loan getLoanById(int loanId) throws DAOException, RecordNotFoundException {
        return loadAllLoans().stream()
                .filter(l -> l.getId() == loanId)
                .findFirst()
                .orElseThrow(() -> new RecordNotFoundException("Prestito non trovato: ID " + loanId));
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
                .filter(this::isActive)
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
        return getLoansByStatus(LoanStatus.RESERVED.name());
    }

    @Override
    public List<Loan> getAllActiveLoans() throws DAOException {
        return loadAllLoans().stream()
                .filter(this::isActive)
                .toList();
    }

    @Override
    public List<Loan> getAllReturnedLoans() throws DAOException {
        return getLoansByStatus(LoanStatus.RETURNED.name());
    }

    @Override
    public List<Loan> getExpiredLoans() throws DAOException {
        List<Loan> loans = loadAllLoans();
        List<Loan> expired = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (Loan loan : loans) {
            if (loan.getStatus() == LoanStatus.LOANED
                    && loan.getReturningDate() != null
                    && loan.getReturningDate().isBefore(today)) {

                loan.setStatus(LoanStatus.EXPIRED);
                expired.add(loan);
            }
        }

        if (!expired.isEmpty()) {
            saveAllLoans(loans);
        }

        return expired;
    }

    @Override
    public List<Loan> searchLoansByUser(String searchText) throws DAOException {
        String pattern = safeLower(searchText);

        return loadAllLoans().stream()
                .filter(l -> safeLower(l.getUserEmail()).contains(pattern))
                .toList();
    }

    @Override
    public List<Loan> searchLoansByBook(String searchText) {
        return new ArrayList<>();
    }

    @Override
    public int countActiveLoansByUser(String userEmail) throws DAOException {
        return (int) getActiveLoansByUser(userEmail).size();
    }

    @Override
    public boolean hasUserActiveLoans(String userEmail) throws DAOException {
        return countActiveLoansByUser(userEmail) > 0;
    }

    private List<Loan> loadAllLoans() throws DAOException {
        List<Loan> loans = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);

        if (!Files.exists(path)) {
            return loans;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
        	String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV utenti non valido: header mancante o non corretto");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Loan loan = parseLoanLine(line);
                    if (loan != null) {
                        loans.add(loan);
                    }
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore lettura CSV prestiti", e);
        }
        
        return loans;
    }

    private void saveAllLoans(List<Loan> loans) throws DAOException {
        Path path = Paths.get(FILE_PATH);

        try {
            Files.createDirectories(path.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(CSV_HEADER);
                writer.newLine();

                for (Loan loan : loans) {
                    writer.write(formatLoan(loan));
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            throw new DAOException("Errore scrittura CSV prestiti", e);
        }
    }

    private Loan parseLoanLine(String line) {
        try {
            String[] f = line.split(",", -1);

            return new Loan(
                    Integer.parseInt(f[0]),
                    f[1],
                    Integer.parseInt(f[2]),
                    parseDate(f[4]),
                    parseDate(f[5]),
                    parseDate(f[6]),
                    LoanStatus.valueOf(f[3])
            );

        } catch (Exception e) {
            return null;
        }
    }

    private String formatLoan(Loan loan) {
        return String.join(",",
                String.valueOf(loan.getId()),
                loan.getUserEmail(),
                String.valueOf(loan.getBookId()),
                loan.getStatus().name(),
                formatDate(loan.getReservedDate()),
                formatDate(loan.getLoanedDate()),
                formatDate(loan.getReturningDate())
        );
    }

    private int getNextId() throws DAOException {
        return loadAllLoans().stream()
                .mapToInt(Loan::getId)
                .max()
                .orElse(0) + 1;
    }

    private void updateLoanInternal(int loanId, LoanUpdater updater) throws DAOException, RecordNotFoundException {
        List<Loan> loans = loadAllLoans();

        for (Loan loan : loans) {
            if (loan.getId() == loanId) {
                updater.update(loan);
                saveAllLoans(loans);
                return;
            }
        }

        throw new RecordNotFoundException("Prestito non trovato: ID " + loanId);
    }

    private boolean isActive(Loan loan) {
        return loan.getStatus() == LoanStatus.LOANED
                || loan.getStatus() == LoanStatus.EXPIRED;
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isEmpty() ? null : LocalDate.parse(value);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    @FunctionalInterface
    private interface LoanUpdater {
        void update(Loan loan);
    }
}