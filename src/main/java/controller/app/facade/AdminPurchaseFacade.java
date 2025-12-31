package controller.app.facade;

import app.Session;
import bean.PurchaseBean;
import controller.app.PurchaseController;

import java.util.List;

public class AdminPurchaseFacade {

    private final PurchaseController purchaseController;
    private final Session session;

    public AdminPurchaseFacade() {
        this.purchaseController = new PurchaseController();
        this.session = Session.getInstance();
    }

    // =========================
    // ADMIN OPERATIONS
    // =========================

    public List<PurchaseBean> getAllReservedPurchases() {
        if (!session.isAdmin()) return List.of();
        return purchaseController.getAllReservedPurchases();
    }

    public List<PurchaseBean> searchPurchasesByUser(String userText) {
        if (!session.isAdmin()) return List.of();
        return purchaseController.searchPurchasesByUser(userText);
    }

    public List<PurchaseBean> searchPurchasesByBook(String bookText) {
        if (!session.isAdmin()) return List.of();
        return purchaseController.searchPurchasesByBook(bookText);
    }

    public boolean acceptPurchase(int purchaseId) {
        if (!session.isAdmin()) return false;
        return purchaseController.acceptPurchase(purchaseId);
    }

    public boolean rejectPurchase(int purchaseId) {
        if (!session.isAdmin()) return false;
        return purchaseController.rejectPurchase(purchaseId);
    }

    public boolean isAdmin() {
        return session.isAdmin();
    }
}