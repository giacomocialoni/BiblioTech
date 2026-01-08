package dao.csv;

import dao.PurchaseDAO;
import exception.DAOException;
import model.Purchase;
import utils.PurchaseStatus;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CSVPurchaseDAO implements PurchaseDAO {

    private static final String FILE_PATH = "src/main/resources/data/purchases.csv";
    private static final String CSV_HEADER = "id,user_email,book_id,status,status_date";

    @Override
    public void addPurchase(String userEmail, int bookId) throws DAOException {
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
        updatePurchase(purchaseId, purchase -> {
            purchase.setStatus(status);
            purchase.setStatusDate(LocalDate.now());
        });
    }

    @Override
    public void rejectPurchase(int purchaseId) throws DAOException {
        List<Purchase> purchases = loadAllPurchases();
        boolean removed = purchases.removeIf(p -> p.getId() == purchaseId);

        if (!removed) {
            throw new DAOException("Acquisto non trovato: ID " + purchaseId);
        }

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
        String lowerSearch = searchText.toLowerCase();
        return loadAllPurchases().stream()
                .filter(p -> p.getUserEmail().toLowerCase().contains(lowerSearch))
                .toList();
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) {
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
    public double getTotalSpentByUser(String userEmail) {
        return 0.0;
    }

    // ====== PRIVATE METHODS ======

    private List<Purchase> loadAllPurchases() throws DAOException {
        List<Purchase> purchases = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);

        if (!Files.exists(path)) {
            return purchases;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null || !header.trim().equals(CSV_HEADER)) {
                throw new DAOException("File CSV acquisti non valido: header mancante o non corretto");
            }

            String line;
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                Purchase purchase = parseLineSafe(line);
                if (purchase != null) {
                    purchases.add(purchase);
                }
            }

        } catch (IOException e) {
            throw new DAOException("Errore durante la lettura degli acquisti dal file", e);
        }

        return purchases;
    }

    // Estrazione del try/catch annidato per Sonar
    private Purchase parseLineSafe(String line) {
        try {
            return parsePurchase(line);
        } catch (DateTimeParseException e) {
            // skip invalid line
            return null;
        }
    }

    private void saveAllPurchases(List<Purchase> purchases) throws DAOException {
        Path path = Paths.get(FILE_PATH);

        try {
            Files.createDirectories(path.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(CSV_HEADER);
                writer.newLine();

                for (Purchase purchase : purchases) {
                    writer.write(formatPurchase(purchase));
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            throw new DAOException("Errore durante il salvataggio degli acquisti nel file", e);
        }
    }

    private Purchase parsePurchase(String line) {
        List<String> fields = parseCSVLine(line);

        if (fields.size() < 5) {
            return null;
        }

        try {
            int id = Integer.parseInt(fields.get(0));
            String userEmail = fields.get(1);
            int bookId = Integer.parseInt(fields.get(2));
            PurchaseStatus status = PurchaseStatus.valueOf(fields.get(3));
            LocalDate statusDate = fields.get(4).isEmpty() ? null : LocalDate.parse(fields.get(4));

            return new Purchase(id, userEmail, bookId, statusDate, status);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i += 2;
                    continue;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
            i++;
        }

        fields.add(currentField.toString());
        return fields;
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

    private void updatePurchase(int purchaseId, PurchaseUpdater updater) throws DAOException {
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
            throw new DAOException("Acquisto non trovato: ID " + purchaseId);
        }

        saveAllPurchases(purchases);
    }

    @FunctionalInterface
    private interface PurchaseUpdater {
        void update(Purchase purchase);
    }
}