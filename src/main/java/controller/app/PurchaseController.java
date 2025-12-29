package controller.app;

import app.Session;
import dao.BookDAO;
import dao.PurchaseDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Account;
import model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.BuyResult;

public class PurchaseController {

    private static final Logger logger =
            LoggerFactory.getLogger(PurchaseController.class);

    private final BookDAO bookDAO;
    private final PurchaseDAO purchaseDAO;

    public PurchaseController() {
        DAOFactory factory = DAOFactory.getActiveFactory();
        this.bookDAO = factory.getBookDAO();
        this.purchaseDAO = factory.getPurchaseDAO();
    }

    // ===================== PURCHASE =====================

    public BuyResult buyBook(int bookId, int quantity) {
        Session session = Session.getInstance();

        if (!session.isLoggedIn())
            return BuyResult.NOT_LOGGED;

        if (quantity <= 0)
            return BuyResult.ERROR;

        Account user = session.getLoggedUser();
        Book book;

        try {
            book = bookDAO.getBookById(bookId);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro non trovato id={}", bookId);
            return BuyResult.ERROR;
        } catch (DAOException e) {
            logger.error("Errore DAO nel recupero libro id={}", bookId, e);
            return BuyResult.ERROR;
        }

        if (book.getStock() < quantity)
            return BuyResult.INSUFFICIENT_STOCK;

        try {
            bookDAO.updateStock(bookId, -quantity);

            purchaseDAO.addPurchase(user.getEmail(), bookId);

            return BuyResult.SUCCESS;

        } catch (DAOException e) {
            logger.error(
                    "Errore DAO durante acquisto libro id={} user={}",
                    bookId, user.getEmail(), e
            );
            return BuyResult.ERROR;
        }
    }

    // ===================== CHECK =====================

    public boolean hasPurchasedBook(int bookId) {
        Session session = Session.getInstance();

        if (!session.isLoggedIn())
            return false;

        Account user = session.getLoggedUser();

        try {
            return purchaseDAO.hasUserPurchasedBook(user.getEmail(), bookId);
        } catch (DAOException e) {
            logger.error(
                    "Errore DAO controllo acquisto libro id={} user={}",
                    bookId, user.getEmail(), e
            );
            return false;
        }
    }
}