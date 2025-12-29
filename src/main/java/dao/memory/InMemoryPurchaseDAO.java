package dao.memory;

import dao.PurchaseDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Purchase;
import utils.PurchaseStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryPurchaseDAO implements PurchaseDAO {

    private final List<Purchase> purchases = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void addPurchase(String userEmail, int bookId) throws DAOException {
        Purchase purchase = new Purchase(
                nextId++,
                userEmail,
                bookId,
                LocalDate.now(),
                PurchaseStatus.RESERVED
        );
        purchases.add(purchase);
    }

    @Override
    public void updatePurchaseStatus(int purchaseId, String status) throws DAOException, RecordNotFoundException {
        Purchase purchase = getPurchaseById(purchaseId);
        purchase.setStatus(PurchaseStatus.valueOf(status));
        purchase.setStatusDate(LocalDate.now());
    }

    @Override
    public void deletePurchase(int purchaseId) throws DAOException, RecordNotFoundException {
        Optional<Purchase> purchaseToRemove = purchases.stream()
                .filter(p -> p.getId() == purchaseId)
                .findFirst();
        
        if (purchaseToRemove.isPresent()) {
            purchases.remove(purchaseToRemove.get());
        } else {
            throw new RecordNotFoundException("Acquisto con ID " + purchaseId + " non trovato");
        }
    }

    @Override
    public Purchase getPurchaseById(int purchaseId) throws DAOException, RecordNotFoundException {
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
    public List<Purchase> getPurchasesByStatus(String status) throws DAOException {
        PurchaseStatus purchaseStatus = PurchaseStatus.valueOf(status);
        return purchases.stream()
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
        return new ArrayList<>(purchases);
    }

    @Override
    public void acceptPurchase(int purchaseId) throws DAOException, RecordNotFoundException {
        Purchase purchase = getPurchaseById(purchaseId);
        purchase.setStatus(PurchaseStatus.PURCHASED);
        purchase.setStatusDate(LocalDate.now());
    }

    @Override
    public List<Purchase> searchPurchasesByUser(String searchText) throws DAOException {
        String lowerSearch = searchText.toLowerCase();
        return purchases.stream()
                .filter(p -> p.getUserEmail().toLowerCase().contains(lowerSearch))
                .toList();
    }

    @Override
    public List<Purchase> searchPurchasesByBook(String searchText) throws DAOException {
        // In memoria non abbiamo informazioni sui libri, solo bookId
        return new ArrayList<>();
    }

    @Override
    public boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException {
        return purchases.stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getBookId() == bookId)
                .anyMatch(p -> p.getStatus() == PurchaseStatus.PURCHASED);
    }

    @Override
    public List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException {
        return purchases.stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getStatus() == PurchaseStatus.PURCHASED)
                .map(Purchase::getBookId)
                .toList();
    }

    @Override
    public int countPurchasesByUser(String userEmail) throws DAOException {
        return (int) purchases.stream()
                .filter(p -> p.getUserEmail().equals(userEmail))
                .filter(p -> p.getStatus() == PurchaseStatus.PURCHASED)
                .count();
    }

    @Override
    public double getTotalSpentByUser(String userEmail) throws DAOException {
        // In memoria non abbiamo i prezzi dei libri
        // Questa implementazione richiederebbe un BookDAO
        return 0.0;
    }
}