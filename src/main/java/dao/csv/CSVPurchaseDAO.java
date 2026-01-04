package dao.csv;

import dao.PurchaseDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Purchase;
import utils.PurchaseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVPurchaseDAO implements PurchaseDAO {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVPurchaseDAO.class);
    private static final String FILE_PATH = "src/main/resources/data/purchases.csv";
    
    @Override
    public void addPurchase(String userEmail, int bookId) throws DAOException {
        try {
            Path path = Paths.get(FILE_PATH);
            boolean fileExists = Files.exists(path);
            
            try (BufferedWriter writer = Files.newBufferedWriter(path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                
                if (!fileExists || Files.size(path) == 0) {
                    writer.write("id,user_email,book_id,status,status_date");
                    writer.newLine();
                }
                
                int nextId = getNextId();
                String line = String.join(",",
                    String.valueOf(nextId),
                    userEmail,
                    String.valueOf(bookId),
                    "RESERVED",
                    LocalDate.now().toString()
                );
                
                writer.write(line);
                writer.newLine();
                
                LOGGER.info("Acquisto aggiunto: ID {} per utente {} libro {}", nextId, userEmail, bookId);
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante l'aggiunta dell'acquisto per utente {} libro {}", userEmail, bookId, e);
            throw new DAOException("Errore durante l'aggiunta dell'acquisto", e);
        }
    }
    
    @Override
    public void updatePurchaseStatus(int purchaseId, String status) throws DAOException, RecordNotFoundException {
        updatePurchase(purchaseId, purchase -> {
            purchase.setStatus(PurchaseStatus.valueOf(status));
            purchase.setStatusDate(LocalDate.now());
        });
        LOGGER.info("Stato acquisto aggiornato: ID {} -> {}", purchaseId, status);
    }
    
    @Override
    public void rejectPurchase(int purchaseId) throws DAOException, RecordNotFoundException {
        List<Purchase> purchases = loadAllPurchases();
        boolean removed = purchases.removeIf(p -> p.getId() == purchaseId);
        
        if (!removed) {
            LOGGER.warn("Tentativo di eliminazione acquisto non trovato: ID {}", purchaseId);
            throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
        }
        
        saveAllPurchases(purchases);
        LOGGER.info("Acquisto rifiutato/eliminato: ID {}", purchaseId);
    }
    
    @Override
    public Purchase getPurchaseById(int purchaseId) throws DAOException, RecordNotFoundException {
        return loadAllPurchases().stream()
                .filter(p -> p.getId() == purchaseId)
                .findFirst()
                .orElseThrow(() -> {
                    LOGGER.warn("Acquisto non trovato: ID {}", purchaseId);
                    return new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
                });
    }
    
    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        List<Purchase> purchases = loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .toList();
        LOGGER.debug("Recuperati {} acquisti per utente {}", purchases.size(), userEmail);
        return purchases;
    }
    
    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        List<Purchase> purchases = loadAllPurchases().stream()
                .filter(p -> p.getBookId() == bookId)
                .toList();
        LOGGER.debug("Recuperati {} acquisti per libro {}", purchases.size(), bookId);
        return purchases;
    }
    
    @Override
    public List<Purchase> getPurchasesByStatus(String status) throws DAOException {
        PurchaseStatus purchaseStatus = PurchaseStatus.valueOf(status);
        List<Purchase> purchases = loadAllPurchases().stream()
                .filter(p -> p.getStatus() == purchaseStatus)
                .toList();
        LOGGER.debug("Recuperati {} acquisti con stato {}", purchases.size(), status);
        return purchases;
    }
    
    @Override
    public List<Purchase> getAllReservedPurchases() throws DAOException {
        List<Purchase> purchases = getPurchasesByStatus("RESERVED");
        LOGGER.debug("Recuperati tutti i {} acquisti riservati", purchases.size());
        return purchases;
    }
    
    @Override
    public List<Purchase> getAllCompletedPurchases() throws DAOException {
        List<Purchase> purchases = getPurchasesByStatus("PURCHASED");
        LOGGER.debug("Recuperati tutti i {} acquisti completati", purchases.size());
        return purchases;
    }
    
    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        List<Purchase> purchases = loadAllPurchases();
        LOGGER.debug("Recuperati tutti i {} acquisti", purchases.size());
        return purchases;
    }
    
    @Override
    public void acceptPurchase(int purchaseId) throws DAOException, RecordNotFoundException {
        updatePurchase(purchaseId, purchase -> {
            purchase.setStatus(PurchaseStatus.PURCHASED);
            purchase.setStatusDate(LocalDate.now());
        });
        LOGGER.info("Acquisto accettato: ID {}", purchaseId);
    }
    
    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        String lowerSearch = searchText.toLowerCase();
        List<Purchase> purchases = loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().toLowerCase().contains(lowerSearch))
                .toList();
        LOGGER.debug("Ricerca acquisti per utente '{}': trovati {} risultati", searchText, purchases.size());
        return purchases;
    }
    
    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        // In CSV senza join con books, non possiamo cercare per titolo/autore
        LOGGER.warn("Ricerca acquisti per libro non supportata in modalità CSV");
        return new ArrayList<>();
    }
    
    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        boolean purchased = loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getBookId() == bookId)
                .anyMatch(p -> p.getStatus() == PurchaseStatus.PURCHASED);
        
        LOGGER.debug("Utente {} ha acquistato libro {}: {}", userEmail, bookId, purchased);
        return purchased;
    }
    
    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
        List<Integer> bookIds = loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getStatus() == PurchaseStatus.PURCHASED)
                .map(Purchase::getBookId)
                .toList();
        
        LOGGER.debug("Utente {} ha acquistato {} libri", userEmail, bookIds.size());
        return bookIds;
    }
    
    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        int count = (int) loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getStatus() == PurchaseStatus.PURCHASED)
                .count();
        
        LOGGER.debug("Utente {} ha completato {} acquisti", userEmail, count);
        return count;
    }
    
    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        // In CSV senza join con books, non possiamo calcolare il totale
        LOGGER.warn("Calcolo spesa totale non supportato in modalità CSV per utente {}", userEmail);
        return 0.0;
    }
    
    private List<Purchase> loadAllPurchases() throws DAOException {
    	List<Purchase> purchases = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            LOGGER.debug("File acquisti non trovato, restituita lista vuota");
            return purchases;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine(); // Leggi e memorizza
            if (header == null) {
                LOGGER.warn("File acquisti vuoto");
                return purchases;
            }
            LOGGER.debug("Header file acquisti: {}", header);
            
            String line;
            int lineNumber = 1;
            int errors = 0;
            
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                try {
                    Purchase purchase = parsePurchase(line);
                    if (purchase != null) {
                        purchases.add(purchase);
                    } else {
                        errors++;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Errore nel parsing dell'acquisto riga {}: {}", lineNumber, line, e);
                    errors++;
                }
                lineNumber++;
            }
            
            if (errors > 0) {
                LOGGER.warn("Caricamento acquisti completato con {} errori", errors);
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante la lettura degli acquisti", e);
            throw new DAOException("Errore durante la lettura degli acquisti", e);
        }
        
        LOGGER.debug("Caricati {} acquisti", purchases.size());
        return purchases;
    }
    
    private void saveAllPurchases(List<Purchase> purchases) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        
        try {
            Files.createDirectories(path.getParent());
            
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("id,user_email,book_id,status,status_date");
                writer.newLine();
                
                for (Purchase purchase : purchases) {
                    writer.write(formatPurchase(purchase));
                    writer.newLine();
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Errore durante il salvataggio degli acquisti", e);
            throw new DAOException("Errore durante il salvataggio degli acquisti", e);
        }
    }
    
    private Purchase parsePurchase(String line) {
        String[] fields = line.split(",", -1);
        
        try {
            int id = Integer.parseInt(fields[0]);
            String userEmail = fields[1];
            int bookId = Integer.parseInt(fields[2]);
            PurchaseStatus status = PurchaseStatus.valueOf(fields[3]);
            LocalDate statusDate = fields[4].isEmpty() ? null : LocalDate.parse(fields[4]);
            
            return new Purchase(id, userEmail, bookId, statusDate, status);
            
        } catch (Exception e) {
            LOGGER.warn("Errore nel parsing della riga acquisto: {}", line, e);
            return null;
        }
    }
    
    private String formatPurchase(Purchase purchase) {
        return String.join(",",
            String.valueOf(purchase.getId()),
            purchase.getUserEmail(),
            String.valueOf(purchase.getBookId()),
            purchase.getStatus().name(),
            purchase.getStatusDate() != null ? purchase.getStatusDate().toString() : ""
        );
    }
    
    private int getNextId() throws DAOException {
        List<Purchase> purchases = loadAllPurchases();
        int nextId = purchases.stream()
                .mapToInt(Purchase::getId)
                .max()
                .orElse(0) + 1;
        LOGGER.debug("Nuovo ID acquisto generato: {}", nextId);
        return nextId;
    }
    
    private void updatePurchase(int purchaseId, PurchaseUpdater updater) throws DAOException, RecordNotFoundException {
        List<Purchase> purchases = loadAllPurchases();
        boolean found = false;
        
        for (Purchase purchase : purchases) {
            if (purchase.getId() == purchaseId) {
                updater.update(purchase);
                found = true;
                break;
            }
        }
        
        if (!found) {
            LOGGER.warn("Acquisto non trovato per aggiornamento: ID {}", purchaseId);
            throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
        }
        
        saveAllPurchases(purchases);
    }
    
    @FunctionalInterface
    private interface PurchaseUpdater {
        void update(Purchase purchase);
    }
}