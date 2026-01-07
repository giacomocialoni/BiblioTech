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
import java.util.stream.Collectors;

public class PurchaseController {

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
        if (quantity <= 0) return BuyResult.ERROR;

        Book book;
        try {
            book = bookDAO.getBookById(bookId);
        } catch (RecordNotFoundException e) {
            return BuyResult.ERROR;
        } catch (DAOException e) {
            return BuyResult.ERROR;
        }

        if (book.getStock() < quantity) return BuyResult.INSUFFICIENT_STOCK;

        try {
            bookDAO.updateStock(bookId, -quantity);
            purchaseDAO.addPurchase(userEmail, bookId);
            return BuyResult.SUCCESS;
        } catch (DAOException e) {
            return BuyResult.ERROR;
        }
    }

    public boolean hasPurchasedBook(String userEmail, int bookId) {
        try {
            return purchaseDAO.hasUserPurchasedBook(userEmail, bookId);
        } catch (DAOException e) {
            return false;
        }
    }

    // =========================
    // ADMIN OPERATIONS
    // =========================

    public List<PurchaseBean> getAllReservedPurchases() {
        try {
            return purchaseDAO.getPurchasesByStatus(PurchaseStatus.RESERVED)
                    .stream()
                    .map(this::toPurchaseBean)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            return List.of();
        }
    }

    public List<PurchaseBean> searchPurchasesByUser(String userText) {
        try {
            return purchaseDAO.searchPurchasesByUser(userText)
                    .stream()
                    .map(this::toPurchaseBean)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            return List.of();
        }
    }

    public List<PurchaseBean> searchPurchasesByBook(String bookText) {
        try {
            return purchaseDAO.searchPurchasesByBook(bookText)
                    .stream()
                    .map(this::toPurchaseBean)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            return List.of();
        }
    }

    public boolean acceptPurchase(int purchaseId) {
        try {
            purchaseDAO.updatePurchaseStatus(purchaseId, PurchaseStatus.PURCHASED);
            return true;
        } catch (DAOException e) {
            return false;
        }
    }

    public boolean rejectPurchase(int purchaseId) {
        try {
            purchaseDAO.rejectPurchase(purchaseId);
            return true;
        } catch (DAOException e) {
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

            PurchaseStatus status = purchase.getStatus();
            bean.setStatus(status != null ? status : PurchaseStatus.RESERVED);

            LocalDate statusDate = purchase.getStatusDate();
            bean.setStatusDate(statusDate != null ? statusDate : LocalDate.now());

            try {
                Book book = bookDAO.getBookById(purchase.getBookId());
                if (book != null) {
                    BookBean bookBean = new BookBean();
                    bookBean.setId(book.getId());
                    bookBean.setTitle(book.getTitle());
                    bookBean.setAuthor(book.getAuthor());
                    bookBean.setCategory(book.getCategory());
                    bookBean.setPrice(book.getPrice());
                    bookBean.setStock(book.getStock());
                    bookBean.setImagePath(book.getImagePath() != null ? book.getImagePath() : "default.jpg");
                    bean.setBook(bookBean);
                }
            } catch (DAOException e) {
                // fallback: bean con solo ID libro
            }

        } catch (IncorrectDataException e) {
            // log o ignorare
        }

        return bean;
    }
}