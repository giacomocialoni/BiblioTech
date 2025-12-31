package controller.app.facade;

import app.Session;
import controller.app.PurchaseController;
import utils.BuyResult;

public class UserPurchaseFacade {

    private final PurchaseController purchaseController;
    private final Session session;

    public UserPurchaseFacade() {
        this.purchaseController = new PurchaseController();
        this.session = Session.getInstance();
    }

    public BuyResult buyBook(int bookId, int quantity) {
        // Utente deve essere loggato
        if (!session.isLoggedIn()) {
            return BuyResult.NOT_LOGGED;
        }

        // Solo utenti normali possono comprare
        if (!session.isUser()) {
            if (session.isAdmin()) return BuyResult.UNAUTHORIZED;
            return BuyResult.NOT_LOGGED;
        }

        // Passa l'email automaticamente al controller
        return purchaseController.buyBook(bookId, quantity, session.getUserEmail());
    }

    public boolean hasPurchasedBook(int bookId) {
        if (!session.isLoggedIn()) return false;
        return purchaseController.hasPurchasedBook(session.getUserEmail(), bookId);
    }

    public boolean canPurchase() {
        return session.isUser();
    }

    public String getUserEmail() {
        return session.getUserEmail();
    }

    public String getUserRole() {
        return session.getUserRole();
    }
}