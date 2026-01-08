package controller.app;

import bean.BookBean;
import bean.PurchaseBean;
import dao.BookDAO;
import dao.PurchaseDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.RecordNotFoundException;
import exception.IncorrectDataException;
import model.Book;
import model.Purchase;
import utils.BuyResult;
import utils.PurchaseStatus;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseController {

    private static final Logger logger =
            LoggerFactory.getLogger(PurchaseController.class);
    
    private final BookDAO bookDAO;
    private final PurchaseDAO purchaseDAO;

    public PurchaseController() {
        DAOFactory factory = DAOFactory.getInstance();
        this.bookDAO = factory.getBookDAO();
        this.purchaseDAO = factory.getPurchaseDAO();
    }

    // =========================
    // USER OPERATIONS
    // =========================

    public BuyResult buyBook(int bookId, int quantity, String userEmail) {
        
        if (quantity <= 0) {
            logger.error("Quantità non valida: {}", quantity);
            return BuyResult.ERROR;
        }

        try {

            Book book = bookDAO.getBookById(bookId);

            if (book.getStock() < quantity) {
                logger.warn("Stock insufficiente: {} < {}", book.getStock(), quantity);
                return BuyResult.INSUFFICIENT_STOCK;
            }

            // Il DAO si occupa di diminuire lo stock e creare la prenotazione in transazione
            purchaseDAO.addPurchase(userEmail, bookId, quantity);
            
            return BuyResult.SUCCESS;

        } catch (RecordNotFoundException e) {
            logger.error("Libro non trovato ID: {}", bookId, e);
            return BuyResult.ERROR;
        } catch (DAOException e) {
            logger.error("Errore durante l'acquisto", e);
            return BuyResult.ERROR;
        }
    }

    public boolean hasPurchasedBook(String userEmail, int bookId) {
        try {
            return purchaseDAO.hasUserPurchasedBook(userEmail, bookId);
        } catch (DAOException e) {
            logger.error("Errore verifica acquisto", e);
            return false;
        }
    }

    // =========================
    // ADMIN OPERATIONS
    // =========================

    public List<PurchaseBean> getAllReservedPurchases() {
        try {
            List<PurchaseBean> purchases = purchaseDAO.getPurchasesByStatus(PurchaseStatus.RESERVED)
                    .stream()
                    .map(this::toPurchaseBean)
                    .toList();
            return purchases;
        } catch (DAOException e) {
            logger.error("Errore recupero acquisti riservati", e);
            return List.of();
        }
    }

    public List<PurchaseBean> searchPurchasesByUser(String userText) {
        try {
            return purchaseDAO.searchPurchasesByUser(userText)
                    .stream()
                    .map(this::toPurchaseBean)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore ricerca acquisti per utente", e);
            return List.of();
        }
    }

    public List<PurchaseBean> searchPurchasesByBook(String bookText) {
        try {
            return purchaseDAO.searchPurchasesByBook(bookText)
                    .stream()
                    .map(this::toPurchaseBean)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore ricerca acquisti per libro", e);
            return List.of();
        }
    }

    public boolean acceptPurchase(int purchaseId) {
        try {
            
            // Verifica che l'acquisto sia in stato RESERVED
            Purchase purchase = purchaseDAO.getPurchaseById(purchaseId);
            if (purchase.getStatus() != PurchaseStatus.RESERVED) {
                logger.error("Acquisto ID {} non è in stato RESERVED", purchaseId);
                return false;
            }
            
            // Cambia solo lo stato (lo stock è già stato diminuito alla prenotazione)
            purchaseDAO.updatePurchaseStatus(purchaseId, PurchaseStatus.PURCHASED);
            return true;
            
        } catch (DAOException e) {
            logger.error("Errore accettazione acquisto ID: {}", purchaseId, e);
            return false;
        }
    }

    public boolean rejectPurchase(int purchaseId) {
        try {
            
            // Verifica che l'acquisto sia in stato RESERVED
            Purchase purchase = purchaseDAO.getPurchaseById(purchaseId);
            if (purchase.getStatus() != PurchaseStatus.RESERVED) {
                logger.error("Acquisto ID {} non è in stato RESERVED", purchaseId);
                return false;
            }
            
            // Il DAO si occupa di ripristinare lo stock ed eliminare la prenotazione
            purchaseDAO.rejectPurchase(purchaseId);
            return true;
            
        } catch (DAOException e) {
            logger.error("Errore rifiuto acquisto ID: {}", purchaseId, e);
            return false;
        }
    }

    // =========================
    // MAPPING
    // =========================

    private PurchaseBean toPurchaseBean(Purchase purchase) {
        PurchaseBean bean = new PurchaseBean();
        try {
            bean.setId(purchase.getId());
            bean.setUserEmail(purchase.getUserEmail());
            bean.setBookId(purchase.getBookId());
            bean.setQuantity(purchase.getQuantity()); // Aggiunta quantità

            PurchaseStatus status = purchase.getStatus();
            bean.setStatus(status != null ? status : PurchaseStatus.RESERVED);

            LocalDate statusDate = purchase.getStatusDate();
            bean.setStatusDate(statusDate != null ? statusDate : LocalDate.now());

            BookBean bookBean = safeGetBookBeanForPurchase(purchase.getBookId());
            bean.setBook(bookBean);

            logger.debug("Mappato acquisto ID: {}, Utente: {}, Libro: {}, Quantità: {}", 
                        purchase.getId(), purchase.getUserEmail(), purchase.getBookId(), purchase.getQuantity());

        } catch (IncorrectDataException e) {
            logger.warn("Errore conversione PurchaseBean per acquisto ID: {}", purchase.getId(), e);
        }

        return bean;
    }

    private BookBean safeGetBookBeanForPurchase(int bookId) {
        try {
            Book book = bookDAO.getBookById(bookId);
            if (book != null) {
                BookBean bookBean = new BookBean();
                bookBean.setId(book.getId());
                bookBean.setTitle(book.getTitle());
                bookBean.setAuthor(book.getAuthor());
                bookBean.setCategory(book.getCategory());
                bookBean.setPrice(book.getPrice());
                bookBean.setStock(book.getStock());
                bookBean.setImagePath(book.getImagePath() != null ? book.getImagePath() : "default.jpg");
                return bookBean;
            }
        } catch (IncorrectDataException | DAOException e) {
            logger.warn("Impossibile recuperare libro ID {} per acquisto", bookId, e);
        }

        // fallback: bean con solo ID libro
        BookBean fallback = new BookBean();
        try {
            fallback.setId(bookId);
            logger.warn("Utilizzato BookBean fallback per libro ID {}", bookId);
        } catch (IncorrectDataException e) {
            logger.warn("Impossibile impostare ID del BookBean fallback per libro ID {}", bookId, e);
        }
        return fallback;
    }
}