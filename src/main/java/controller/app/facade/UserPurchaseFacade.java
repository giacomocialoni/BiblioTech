package controller.app.facade;

import app.Session;
import controller.app.PurchaseController;
import utils.BuyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPurchaseFacade {

    private final PurchaseController purchaseController;
    private final Session session;
    private static final Logger logger = LoggerFactory.getLogger(UserPurchaseFacade.class);

    public UserPurchaseFacade() {
        this.purchaseController = new PurchaseController();
        this.session = Session.getInstance();
    }

    public BuyResult buyBook(int bookId, int quantity) {
        logger.debug("UserPurchaseFacade.buyBook() - BookID: {}, Quantity: {}, User: {}", 
                    bookId, quantity, session.getUserEmail());
        
        // Utente deve essere loggato
        if (!session.isLoggedIn()) {
            logger.warn("Utente non loggato tenta acquisto");
            return BuyResult.NOT_LOGGED;
        }

        // Solo utenti normali possono comprare (admin NO)
        if (!session.isUser()) {
            logger.warn("Admin tenta acquisto - Email: {}", session.getUserEmail());
            return BuyResult.UNAUTHORIZED;
        }

        // Verifica che la quantità sia valida
        if (quantity <= 0) {
            logger.error("Quantità non valida: {}", quantity);
            return BuyResult.ERROR;
        }

        // Passa l'email automaticamente al controller
        BuyResult result = purchaseController.buyBook(bookId, quantity, session.getUserEmail());
        return result;
    }

    public boolean hasPurchasedBook(int bookId) {
        if (!session.isLoggedIn()) {
            return false;
        }
        boolean hasPurchased = purchaseController.hasPurchasedBook(session.getUserEmail(), bookId);
        logger.debug("Verifica acquisto - Utente: {}, Libro: {}, Risultato: {}", 
                    session.getUserEmail(), bookId, hasPurchased);
        return hasPurchased;
    }

    public boolean canPurchase() {
        boolean canPurchase = session.isUser();
        logger.debug("Utente può acquistare? {}", canPurchase);
        return canPurchase;
    }

    public String getUserEmail() {
        return session.getUserEmail();
    }

    public String getUserRole() {
        return session.getUserRole();
    }
}