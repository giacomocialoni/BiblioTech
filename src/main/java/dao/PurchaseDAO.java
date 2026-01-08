package dao;

import model.Purchase;
import utils.PurchaseStatus;
import exception.DAOException;

import java.util.List;

public interface PurchaseDAO {

    /* ================= CRUD ================= */

    void addPurchase(String userEmail, int bookId, int quantity) throws DAOException;
    void updatePurchaseStatus(int purchaseId, PurchaseStatus status)
            throws DAOException;
    void rejectPurchase(int purchaseId)
            throws DAOException;

    /* ============== RECUPERO ============== */

    Purchase getPurchaseById(int purchaseId)
            throws DAOException;
    List<Purchase> getPurchasesByUser(String userEmail) throws DAOException;
    List<Purchase> getPurchasesByBook(int bookId) throws DAOException;
    List<Purchase> getPurchasesByStatus(PurchaseStatus status) throws DAOException;
    List<Purchase> getAllPurchases() throws DAOException;

    /* ============== RICERCA ============== */

    List<Purchase> searchPurchasesByUser(String searchText) throws DAOException;
    List<Purchase> searchPurchasesByBook(String searchText) throws DAOException;

    /* ============== VERIFICHE ============== */

    boolean hasUserPurchasedBook(String userEmail, int bookId) throws DAOException;
    List<Integer> getPurchasedBookIdsByUser(String userEmail) throws DAOException;

    /* ============== STATISTICHE ============== */

    int countPurchasesByUser(String userEmail) throws DAOException;
    double getTotalSpentByUser(String userEmail) throws DAOException;
}