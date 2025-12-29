package dao;

import model.Purchase;
import exception.DAOException;
import exception.RecordNotFoundException;

import java.util.List;

public interface PurchaseDAO {
    // Operazioni CRUD
    void addPurchase(String userEmail, int bookId) throws DAOException;
    void updatePurchaseStatus(int purchaseId, String status) throws DAOException, RecordNotFoundException;
    void deletePurchase(int purchaseId) throws DAOException, RecordNotFoundException;
    
    // Recupero acquisti
    Purchase getPurchaseById(int purchaseId) throws DAOException, RecordNotFoundException;
    List<Purchase> getPurchasesByUser(String userEmail) throws DAOException;
    List<Purchase> getPurchasesByBook(int bookId) throws DAOException;
    
    // Recupero per stato
    List<Purchase> getPurchasesByStatus(String status) throws DAOException;
    List<Purchase> getAllReservedPurchases() throws DAOException;
    List<Purchase> getAllCompletedPurchases() throws DAOException;
    List<Purchase> getAllPurchases() throws DAOException;
    
    // Operazioni business
    void acceptPurchase(int purchaseId) throws DAOException, RecordNotFoundException;
    
    // Ricerca
    List<Purchase> searchPurchasesByUser(String searchText) throws DAOException;
    List<Purchase> searchPurchasesByBook(String searchText) throws DAOException;
    
    // Verifiche
    boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException;
    List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException;
    
    // Statistiche
    int countPurchasesByUser(String userEmail) throws DAOException;
    double getTotalSpentByUser(String userEmail) throws DAOException;
}