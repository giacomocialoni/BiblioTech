package dao.csv;

import dao.PurchaseDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Purchase;
import utils.PurchaseStatus;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVPurchaseDAO implements PurchaseDAO {
    
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
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiunta dell'acquisto", e);
        }
    }
    
    @Override
    public void updatePurchaseStatus(int purchaseId, String status) throws DAOException, RecordNotFoundException {
        updatePurchase(purchaseId, purchase -> {
            purchase.setStatus(PurchaseStatus.valueOf(status));
            purchase.setStatusDate(LocalDate.now());
        });
    }
    
    @Override
    public void deletePurchase(int purchaseId) throws DAOException, RecordNotFoundException {
        List<Purchase> purchases = loadAllPurchases();
        boolean removed = purchases.removeIf(p -> p.getId() == purchaseId);
        
        if (!removed) {
            throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
        }
        
        saveAllPurchases(purchases);
    }
    
    @Override
    public Purchase getPurchaseById(int purchaseId) throws DAOException, RecordNotFoundException {
        return loadAllPurchases().stream()
                .filter(p -> p.getId() == purchaseId)
                .findFirst()
                .orElseThrow(() -> new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato"));
    }
    
    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        return loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .toList();
    }
    
    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        return loadAllPurchases().stream()
                .filter(p -> p.getBookId() == bookId)
                .toList();
    }
    
    @Override
    public List<Purchase> getPurchasesByStatus(String status) throws DAOException {
        PurchaseStatus purchaseStatus = PurchaseStatus.valueOf(status);
        return loadAllPurchases().stream()
                .filter(p -> p.getStatus() == purchaseStatus)
                .toList();
    }
    
    @Override
    public List<Purchase> getAllReservedPurchases() throws DAOException {
        return getPurchasesByStatus("RESERVED");
    }
    
    @Override
    public List<Purchase> getAllCompletedPurchases() throws DAOException {
        return getPurchasesByStatus("PURCHASED");
    }
    
    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        return loadAllPurchases();
    }
    
    @Override
    public void acceptPurchase(int purchaseId) throws DAOException, RecordNotFoundException {
        updatePurchase(purchaseId, purchase -> {
            purchase.setStatus(PurchaseStatus.PURCHASED);
            purchase.setStatusDate(LocalDate.now());
        });
    }
    
    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        String lowerSearch = searchText.toLowerCase();
        return loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().toLowerCase().contains(lowerSearch))
                .toList();
    }
    
    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        // In CSV senza join con books, non possiamo cercare per titolo/autore
        return new ArrayList<>();
    }
    
    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        return loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getBookId() == bookId)
                .anyMatch(p -> p.getStatus() == PurchaseStatus.PURCHASED);
    }
    
    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
        return loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getStatus() == PurchaseStatus.PURCHASED)
                .map(Purchase::getBookId)
                .toList();
    }
    
    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        return (int) loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getStatus() == PurchaseStatus.PURCHASED)
                .count();
    }
    
    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        // In CSV senza join con books, non possiamo calcolare il totale
        return 0.0;
    }
    
    private List<Purchase> loadAllPurchases() throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        
        if (!Files.exists(path)) {
            return purchases;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                try {
                    Purchase purchase = parsePurchase(line);
                    if (purchase != null) {
                        purchases.add(purchase);
                    }
                } catch (Exception e) {
                    System.err.println("Errore nel parsing dell'acquisto: " + line);
                    e.printStackTrace();
                }
            }
            
        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura degli acquisti", e);
        }
        
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
            System.err.println("Errore nel parsing della riga: " + line);
            e.printStackTrace();
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
        return purchases.stream()
                .mapToInt(Purchase::getId)
                .max()
                .orElse(0) + 1;
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
            throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
        }
        
        saveAllPurchases(purchases);
    }
    
    @FunctionalInterface
    private interface PurchaseUpdater {
        void update(Purchase purchase);
    }
}