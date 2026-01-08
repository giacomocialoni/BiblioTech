package controller.app;

import bean.BookBean;
import bean.LoanBean;
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
import utils.PurchaseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ProfiloController {

    private static final Logger logger =
            LoggerFactory.getLogger(ProfiloController.class);

    private final BookDAO bookDAO;
    private final UserDAO userDAO;
    private final LoanDAO loanDAO;
    private final PurchaseDAO purchaseDAO;

    public ProfiloController() {
        this.bookDAO = DAOFactory.getInstance().getBookDAO();
        this.userDAO = DAOFactory.getInstance().getUserDAO();
        this.loanDAO = DAOFactory.getInstance().getLoanDAO();
        this.purchaseDAO = DAOFactory.getInstance().getPurchaseDAO();
    }

    public UserBean getUser(String email) {
        try {
            User user = userDAO.getUser(email);

            UserBean bean = new UserBean();
            bean.setEmail(user.getEmail());
            bean.setPassword(user.getPassword());
            bean.setFirstName(user.getFirstName());
            bean.setLastName(user.getLastName());
            return bean;

        } catch (DAOException e) {
            logger.error("Errore recupero utente", e);
            return null;
        }
    }

    public List<BookBean> getPurchasedBooks(String email) {
        try {
            List<Purchase> purchases = purchaseDAO.getPurchasesByUser(email);
            List<BookBean> purchasedBooks = new ArrayList<>();

            for (Purchase purchase : purchases) {
                if (purchase.getStatus() == PurchaseStatus.PURCHASED) {
                    BookBean bookBean = safeGetBookBean(purchase.getBookId());
                    if (bookBean != null) {
                        purchasedBooks.add(bookBean);
                    }
                }
            }

            return purchasedBooks;

        } catch (DAOException e) {
            logger.error("Errore recupero libri acquistati per utente: {}", email, e);
            return List.of();
        }
    }

    public List<LoanBean> getActiveLoans(String email) {
        try {
            List<Loan> loans = loanDAO.getActiveLoansByUser(email);
            List<LoanBean> loanBeans = new ArrayList<>();

            for (Loan loan : loans) {
                LoanBean loanBean = safeToLoanBean(loan);
                if (loanBean != null) {
                    loanBeans.add(loanBean);
                }
            }

            return loanBeans;

        } catch (DAOException e) {
            logger.error("Errore recupero prestiti attivi per utente: {}", email, e);
            return List.of();
        }
    }

    /* =====================
       HELPERS PRIVATI
       ===================== */

    private BookBean safeGetBookBean(int bookId) {
        try {
            Book book = bookDAO.getBookById(bookId);
            return book != null ? toBookBean(book) : null;
        } catch (RecordNotFoundException e) {
            logger.warn("Libro non trovato ID: {}", bookId);
            return null;
        } catch (DAOException e) {
            logger.warn("Errore recupero libro ID: {}", bookId, e);
            return null;
        }
    }

    private LoanBean safeToLoanBean(Loan loan) {
        try {
            LoanBean bean = new LoanBean();
            bean.setId(loan.getId());
            bean.setUserEmail(loan.getUserEmail());
            bean.setStatus(loan.getStatus());
            bean.setReservedDate(loan.getReservedDate());
            bean.setLoanedDate(loan.getLoanedDate());
            bean.setReturningDate(loan.getReturningDate());

            int bookId = loan.getBookId();
            if (bookId <= 0) {
                logger.warn("BookId non valido ({}) per prestito id={}", bookId, loan.getId());
                return null;
            }

            BookBean bookBean = safeGetBookBean(bookId);
            if (bookBean != null) {
                bean.setBook(bookBean);
            } else {
                return null;
            }

            return bean;
        } catch (Exception e) {
            logger.error("Errore conversione prestito id={}", loan != null ? loan.getId() : "null", e);
            return null;
        }
    }

    /* =====================
       MAPPING PRIVATO
       ===================== */

    private BookBean toBookBean(Book book) {
        try {
            BookBean bean = new BookBean();
            bean.setId(book.getId());
            bean.setTitle(book.getTitle());
            bean.setAuthor(book.getAuthor());
            bean.setCategory(book.getCategory());
            bean.setImagePath(book.getImagePath());
            bean.setStock(book.getStock());
            bean.setPrice(book.getPrice());
            bean.setYear(book.getYear());
            bean.setPublisher(book.getPublisher());
            bean.setPages(book.getPages());
            bean.setIsbn(book.getIsbn());
            bean.setPlot(book.getPlot());
            return bean;
        } catch (IncorrectDataException e) {
            logger.error("Errore conversione BookBean per libro ID: {}", book.getId(), e);
            return null;
        }
    }
}