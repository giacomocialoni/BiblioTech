package dao.memory;

import dao.PurchaseDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Purchase;
import utils.PurchaseStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InMemoryPurchaseDAO implements PurchaseDAO {

    private static InMemoryPurchaseDAO instance = null;
    private final List<Purchase> purchases = new ArrayList<>();
    private int nextId = 1;

    public InMemoryPurchaseDAO() {}

    public static InMemoryPurchaseDAO getInstance() {
        if (instance == null) instance = new InMemoryPurchaseDAO();
        return instance;
    }

    @Override
    public void addPurchase(String userEmail, int bookId, int quantity) throws DAOException {
        if (quantity <= 0) throw new DAOException("Quantità non valida: " + quantity);

        Purchase purchase = new Purchase(
                nextId++,
                userEmail,
                bookId,
                quantity,
                LocalDate.now(),
                PurchaseStatus.RESERVED
        );
        purchases.add(purchase);
    }

    @Override
    public void updatePurchaseStatus(int purchaseId, PurchaseStatus status) throws DAOException {
        Purchase purchase = getPurchaseById(purchaseId);
        purchase.setStatus(status);
        purchase.setStatusDate(LocalDate.now());
    }

    @Override
    public void rejectPurchase(int purchaseId) throws DAOException {
        boolean removed = purchases.removeIf(p -> p.getId() == purchaseId);
        if (!removed) throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
    }

    @Override
    public Purchase getPurchaseById(int purchaseId) throws DAOException {
        return purchases.stream()
                .filter(p -> p.getId() == purchaseId)
                .findFirst()
                .orElseThrow(() -> new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato"));
    }

    @Override
    public List<Purchase> getPurchasesByUser(String userEmail) throws DAOException {
        return purchases.stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .toList();
    }

    @Override
    public List<Purchase> getPurchasesByBook(int bookId) throws DAOException {
        return purchases.stream()
                .filter(p -> p.getBookId() == bookId)
                .toList();
    }

    @Override
    public List<Purchase> getPurchasesByStatus(PurchaseStatus status) throws DAOException {
        return purchases.stream()
                .filter(p -> p.getStatus() == status)
                .toList();
    }

    @Override
    public List<Purchase> getAllPurchases() throws DAOException {
        return new ArrayList<>(purchases);
    }

    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        String lower = searchText.toLowerCase();
        return purchases.stream()
                .filter(p -> p.getUserEmail().toLowerCase().contains(lower))
                .toList();
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        return new ArrayList<>();
    }

    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        return purchases.stream()
                .anyMatch(p -> p.getUserEmail().equals(userEmail)
                            && p.getBookId() == bookId
                            && p.getStatus() == PurchaseStatus.PURCHASED);
    }

    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
        return purchases.stream()
                .filter(p -> p.getUserEmail().equals(userEmail) && p.getStatus() == PurchaseStatus.PURCHASED)
                .map(Purchase::getBookId)
                .toList();
    }

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        return (int) purchases.stream()
                .filter(p -> p.getUserEmail().equals(userEmail) && p.getStatus() == PurchaseStatus.PURCHASED)
                .count();
    }

    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        return 0.0;
    }
}