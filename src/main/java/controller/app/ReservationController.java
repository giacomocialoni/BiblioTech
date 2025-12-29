package controller.app;

import bean.BookBean;
import bean.LoanBean;
import bean.PurchaseBean;
import bean.UserBean;
import dao.BookDAO;
import dao.LoanDAO;
import dao.PurchaseDAO;
import dao.UserDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.IncorrectDataException;
import exception.RecordNotFoundException;
import model.Book;
import model.Loan;
import model.Purchase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final PurchaseDAO purchaseDAO;
    private final LoanDAO loanDAO;
    private final UserDAO userDAO;
    private final BookDAO bookDAO;

    public ReservationController() {
        DAOFactory factory = DAOFactory.getActiveFactory();
        this.purchaseDAO = factory.getPurchaseDAO();
        this.loanDAO = factory.getLoanDAO();
        this.userDAO = factory.getUserDAO();
        this.bookDAO = factory.getBookDAO();
    }

    // ===================== GET =====================

    public List<PurchaseBean> getAllReservedPurchases() {
        try {
            return purchaseDAO.getAllReservedPurchases().stream()
                    .map(this::toPurchaseBean)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore DAO recupero acquisti riservati", e);
            return List.of();
        }
    }

    public List<LoanBean> getAllReservedLoans() {
        try {
            return loanDAO.getAllReservedLoans().stream()
                    .map(this::toLoanBean)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore DAO recupero prestiti riservati", e);
            return List.of();
        }
    }

    public BookBean getBookBeanById(int id) {
        try {
            Book book = bookDAO.getBookById(id);
            if (book == null) return null;
            return mapBookToBean(book);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro non trovato id={}", id);
        } catch (DAOException e) {
            logger.warn("Errore DAO per libro non trovato id={}", id);
        }
        return null;
    }

    // ===================== SEARCH =====================

    public List<UserBean> searchUsers(String text) {
        try {
            return userDAO.searchUsers(text).stream()
                    .map(this::toUserBean)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore DAO ricerca utenti", e);
            return List.of();
        }
    }

    public List<PurchaseBean> searchPurchasesByUser(String text) {
        try {
            return purchaseDAO.searchPurchasesByUser(text).stream()
                    .map(this::toPurchaseBean)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore ricerca acquisti per utente", e);
            return List.of();
        }
    }

    public List<PurchaseBean> searchPurchasesByBook(String text) {
        try {
            return purchaseDAO.searchPurchasesByBook(text).stream()
                    .map(this::toPurchaseBean)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore ricerca acquisti per libro", e);
            return List.of();
        }
    }

    public List<LoanBean> searchLoansByUser(String text) {
        try {
            // CORREZIONE: ora si chiama searchLoansByUser
            return loanDAO.searchLoansByUser(text).stream()
                    .map(this::toLoanBean)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore DAO ricerca prestiti per utente '{}'", text, e);
            return List.of();
        }
    }

    public List<LoanBean> searchLoansByBook(String text) {
        try {
            return loanDAO.searchLoansByBook(text).stream()
                    .map(this::toLoanBean)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore ricerca prestiti per libro", e);
            return List.of();
        }
    }

    // ===================== ACTIONS =====================

    public boolean acceptPurchase(int purchaseId) {
        try {
            purchaseDAO.acceptPurchase(purchaseId);
            return true;
        } catch (RecordNotFoundException e) {
            logger.warn("Acquisto non trovato id={}", purchaseId);
            return false;
        } catch (DAOException e) {
            logger.error("Errore DAO accettazione acquisto id={}", purchaseId, e);
            return false;
        }
    }

    public boolean acceptLoan(int loanId) {
        try {
            loanDAO.acceptLoan(loanId);
            return true;
        } catch (RecordNotFoundException e) {
            logger.warn("Prestito non trovato id={}", loanId);
            return false;
        } catch (DAOException e) {
            logger.error("Errore DAO accettazione prestito id={}", loanId, e);
            return false;
        }
    }

    public void updateBookStock(int bookId, int quantityChange) {
        try {
            bookDAO.updateStock(bookId, quantityChange);
        } catch (DAOException e) {
            logger.warn("Errore DAO durante update stock id={}", bookId);
        }
    }

    // ===================== MAPPING =====================

    private UserBean toUserBean(User user) {
        UserBean bean = new UserBean();
        bean.setEmail(user.getEmail());
        bean.setFirstName(user.getFirstName());
        bean.setLastName(user.getLastName());
        return bean;
    }

    private BookBean mapBookToBean(Book book) {
        if (book == null) return null;
        BookBean bean = new BookBean();
        try {
            bean.setId(book.getId());
            bean.setTitle(book.getTitle());
            bean.setAuthor(book.getAuthor());
            bean.setPrice(book.getPrice());
            bean.setImagePath(book.getImagePath());
        } catch (IncorrectDataException e) {
            logger.warn("Errore mapping book", e);
            return null;
        }
        return bean;
    }

    private PurchaseBean toPurchaseBean(Purchase purchase) {
        if (purchase == null) return null;

        try {
            PurchaseBean bean = new PurchaseBean();
            bean.setId(purchase.getId());
            bean.setUserEmail(purchase.getUserEmail());
            bean.setBookId(purchase.getBookId());
            bean.setStatus(purchase.getStatus());
            bean.setStatusDate(purchase.getStatusDate());

            Book bookModel = bookDAO.getBookById(purchase.getBookId());
            bean.setBook(mapBookToBean(bookModel));

            return bean;
        } catch (IncorrectDataException | DAOException e) {
            logger.warn("Errore mapping acquisto id={}", purchase.getId(), e);
            return null;
        }
    }

    private LoanBean toLoanBean(Loan loan) {
        if (loan == null) return null;

        try {
            LoanBean bean = new LoanBean();
            bean.setId(loan.getId());
            bean.setUserEmail(loan.getUserEmail());
            bean.setStatus(loan.getStatus());
            bean.setReservedDate(loan.getReservedDate());
            bean.setLoanedDate(loan.getLoanedDate());
            bean.setReturningDate(loan.getReturningDate());

            Book bookModel = bookDAO.getBookById(loan.getBookId());
            bean.setBook(mapBookToBean(bookModel));

            return bean;
        } catch (DAOException e) {
            logger.warn("Errore mapping prestito id={}", loan.getId(), e);
            return null;
        }
    }
}