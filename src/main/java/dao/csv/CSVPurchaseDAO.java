package dao.csv;

import dao.PurchaseDAO;
import exception.DAOException;
import model.Purchase;
import utils.PurchaseStatus;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVPurchaseDAO implements PurchaseDAO {

    private static final String FILE_PATH = "src/main/resources/data/purchases.csv";
    private static final String CSV_HEADER = "id,user_email,book_id,quantity,status,status_date";

    @Override
    public void addPurchase(String userEmail, int bookId, int quantity) throws DAOException {
        if (quantity <= 0) throw new DAOException("Quantità non valida: " + quantity);

        try {
            Path path = Paths.get(FILE_PATH);
            boolean fileExists = Files.exists(path);

            try (BufferedWriter writer = Files.newBufferedWriter(path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                if (!fileExists || Files.size(path) == 0) {
                    writer.write(CSV_HEADER);
                    writer.newLine();
                }

                int nextId = getNextId();
                String line = String.join(",",
                        String.valueOf(nextId),
                        userEmail,
                        String.valueOf(bookId),
                        String.valueOf(quantity),
                        "RESERVED",
                        LocalDate.now().toString()
                );

                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            throw new DAOException("Errore durante l'aggiunta dell'acquisto per utente " + userEmail, e);
        }
    }

    @Override
    public void updatePurchaseStatus(int purchaseId, PurchaseStatus status) throws DAOException {
        updatePurchase(purchaseId, p -> {
            p.setStatus(status);
            p.setStatusDate(LocalDate.now());
        });
    }

    @Override
    public void rejectPurchase(int purchaseId) throws DAOException {
        List<Purchase> purchases = loadAllPurchases();
        boolean removed = purchases.removeIf(p -> p.getId() == purchaseId);

        if (!removed) throw new DAOException("Acquisto non trovato: ID " + purchaseId);
        saveAllPurchases(purchases);
    }

    @Override
    public Purchase getPurchaseById(int purchaseId) throws DAOException {
        return loadAllPurchases().stream()
                .filter(p -> p.getId() == purchaseId)
                .findFirst()
                .orElseThrow(() -> new DAOException("Acquisto non trovato: ID " + purchaseId));
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
    public List<Purchase> getPurchasesByStatus(PurchaseStatus status) throws DAOException {
        return loadAllPurchases().stream()
                .filter(p -> p.getStatus() == status)
                .toList();
    }

    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        return new ArrayList<>(loadAllPurchases());
    }

    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        String lower = searchText.toLowerCase();
        return loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().toLowerCase().contains(lower))
                .toList();
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) {
        return new ArrayList<>();
    }

    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        return loadAllPurchases().stream()
                .anyMatch(p -> p.getUserEmail().equals(userEmail)
                            && p.getBookId() == bookId
                            && p.getStatus() == PurchaseStatus.PURCHASED);
    }

    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
        return loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail) && p.getStatus() == PurchaseStatus.PURCHASED)
                .map(Purchase::getBookId)
                .toList();
    }

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        return (int) loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().equals(userEmail) && p.getStatus() == PurchaseStatus.PURCHASED)
                .count();
    }

    @Override
    public double getTotalSpentByUser(String userEmail) {
        return 0.0;
    }

    // ====== PRIVATE METHODS ======

    private List<Purchase> loadAllPurchases() throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) return purchases;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) 
                throw new DAOException("File CSV acquisti non valido: header mancante o non corretto");

            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                Purchase p = parseLineSafe(line);
                if (p != null) purchases.add(p);
            }

        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura degli acquisti dal file", e);
        }

        return purchases;
    }

    private Purchase parseLineSafe(String line) {
        try { return parsePurchase(line); } catch (Exception e) { return null; }
    }

    private Purchase parsePurchase(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length < 6) return null;

        int id = Integer.parseInt(fields[0]);
        String userEmail = fields[1];
        int bookId = Integer.parseInt(fields[2]);
        int quantity = Integer.parseInt(fields[3]);
        PurchaseStatus status = PurchaseStatus.valueOf(fields[4]);
        LocalDate date = fields[5].isEmpty() ? null : LocalDate.parse(fields[5]);

        return new Purchase(id, userEmail, bookId, quantity, date, status);
    }

    private void saveAllPurchases(List<Purchase> purchases) throws DAOException {
        Path path = Paths.get(FILE_PATH);
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(CSV_HEADER);
                writer.newLine();
                for (Purchase p : purchases) {
                    writer.write(formatPurchase(p));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new DAOException("Errore durante il salvataggio degli acquisti nel file", e);
        }
    }

    private String formatPurchase(Purchase p) {
        return String.join(",",
                String.valueOf(p.getId()),
                p.getUserEmail(),
                String.valueOf(p.getBookId()),
                String.valueOf(p.getQuantity()),
                p.getStatus().name(),
                p.getStatusDate() != null ? p.getStatusDate().toString() : ""
        );
    }

    private int getNextId() throws DAOException {
        return loadAllPurchases().stream()
                .mapToInt(Purchase::getId)
                .max()
                .orElse(0) + 1;
    }

    private void updatePurchase(int purchaseId, PurchaseUpdater updater) throws DAOException {
        List<Purchase> purchases = loadAllPurchases();
        boolean found = false;

        for (Purchase p : purchases) {
            if (p.getId() == purchaseId) {
                updater.update(p);
                found = true;
                break;
            }
        }

        if (!found) throw new DAOException("Acquisto non trovato: ID " + purchaseId);
        saveAllPurchases(purchases);
    }

    @FunctionalInterface
    private interface PurchaseUpdater { void update(Purchase p); }
}