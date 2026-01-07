package controller.app;

import app.Session;
import bean.BookBean;
import controller.app.facade.UserLoanFacade;
import controller.app.facade.UserPurchaseFacade;
import dao.BookDAO;
import dao.WishlistDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.BuyResult;
import utils.LoanResult;

public class BookDetailController {

    private static final Logger logger =
            LoggerFactory.getLogger(BookDetailController.class);

    private final UserPurchaseFacade purchaseFacade;
    private final UserLoanFacade loanFacade;
    private final BookDAO bookDAO;
    private final WishlistDAO wishlistDAO;

    public BookDetailController() {
        DAOFactory factory = DAOFactory.getInstance();
        this.purchaseFacade = new UserPurchaseFacade();
        this.loanFacade = new UserLoanFacade();
        this.bookDAO = factory.getBookDAO();
        this.wishlistDAO = factory.getWishlistDAO();
    }

    // ================== READ ==================

    public BookBean getBookById(int bookId) {
        try {
            Book book = bookDAO.getBookById(bookId);
            return toBean(book);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro non trovato id={}", bookId);
            return null;
        } catch (DAOException e) {
            logger.error("Errore DAO nel recupero libro id={}", bookId, e);
            return null;
        }
    }

    // ================== BUY / LOAN ==================

    public BuyResult buyBook(int bookId, int quantity) {
        return purchaseFacade.buyBook(bookId, quantity);
    }

    public LoanResult loanBook(int bookId) {
        return loanFacade.loanBook(bookId);
    }

    public boolean hasPurchasedBook(int bookId) {
        return purchaseFacade.hasPurchasedBook(bookId);
    }

    // ================== WISHLIST ==================

    public boolean isInWishlist(int bookId) {
        Session session = Session.getInstance();
        if (!session.isLoggedIn()) return false;

        try {
            return wishlistDAO.isInWishlist(
                    session.getLoggedUser().getEmail(),
                    bookId
            );
        } catch (DAOException e) {
            logger.error("Errore DAO wishlist", e);
            return false;
        }
    }

    public boolean addToWishlist(int bookId) {
        Session session = Session.getInstance();
        if (!session.isLoggedIn()) return false;

        try {
            wishlistDAO.addToWishlist(
                    session.getLoggedUser().getEmail(),
                    bookId
            );
            return true;
        } catch (DAOException e) {
            logger.error("Errore DAO add wishlist", e);
            return false;
        }
    }

    public boolean removeFromWishlist(int bookId) {
        Session session = Session.getInstance();
        if (!session.isLoggedIn()) return false;

        try {
            wishlistDAO.removeFromWishlist(
                    session.getLoggedUser().getEmail(),
                    bookId
            );
            return true;
        } catch (DAOException e) {
            logger.error("Errore DAO remove wishlist", e);
            return false;
        }
    }

    // ================== USER INFO ==================

    public boolean isUserLoggedIn() {
        return Session.getInstance().isLoggedIn();
    }

    public boolean isUserGuest() {
        return Session.getInstance().isGuest();
    }

    public boolean isUserAdmin() {
        return Session.getInstance().isAdmin();
    }

    public boolean isUserNormal() {
        return Session.getInstance().isUser();
    }

    // ================== MAPPING ==================

    private BookBean toBean(Book book) {
        BookBean bean = new BookBean();
        try {
            bean.setId(book.getId());
            bean.setTitle(book.getTitle());
            bean.setAuthor(book.getAuthor());
            bean.setCategory(book.getCategory());
            bean.setImagePath(book.getImagePath());
            bean.setStock(book.getStock());
            bean.setPrice(book.getPrice());
            bean.setPublisher(book.getPublisher());
            bean.setYear(book.getYear());
            bean.setPages(book.getPages());
            bean.setIsbn(book.getIsbn());
            bean.setPlot(book.getPlot());
        } catch (Exception e) {
            logger.warn("Errore mapping BookBean id={}", book.getId(), e);
        }
        return bean;
    }
}