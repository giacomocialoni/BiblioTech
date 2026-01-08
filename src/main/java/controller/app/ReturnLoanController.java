package controller.app;

import bean.BookBean;
import bean.LoanBean;
import dao.LoanDAO;
import dao.BookDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.IncorrectDataException;
import exception.RecordNotFoundException;
import model.Loan;
import model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.LoanStatus;

import java.util.List;
import java.util.Objects;

public class ReturnLoanController {

    private static final Logger logger = LoggerFactory.getLogger(ReturnLoanController.class);

    private final LoanDAO loanDAO;
    private final BookDAO bookDAO;

    public ReturnLoanController() {
        DAOFactory factory = DAOFactory.getInstance();
        this.loanDAO = factory.getLoanDAO();
        this.bookDAO = factory.getBookDAO();
    }

    // ===================== GET =====================
    public List<LoanBean> getAllLoanedLoans() {
        try {
            return loanDAO.getAllActiveLoans().stream()
                    .map(this::toLoanBean)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore DAO recupero prestiti LOANED", e);
            return List.of();
        }
    }

    public List<LoanBean> searchLoanedLoansByUser(String searchText) {
        try {
            return loanDAO.searchLoansByUser(searchText).stream()
                    .map(this::toLoanBean)
                    .filter(Objects::nonNull)
                    .filter(loan -> loan.getStatus() == LoanStatus.LOANED)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore DAO ricerca prestiti LOANED per utente", e);
            return List.of();
        }
    }
    
    public List<LoanBean> searchLoanedLoansByBook(String searchText) {
        try {
            return loanDAO.searchLoansByBook(searchText).stream()
                    .map(this::toLoanBean)
                    .filter(Objects::nonNull)
                    .filter(loan -> loan.getStatus() == LoanStatus.LOANED)
                    .toList();
        } catch (DAOException e) {
            logger.error("Errore DAO ricerca prestiti LOANED per libro", e);
            return List.of();
        }
    }

    // ===================== ACTION =====================
    public boolean returnLoan(int loanId) {
        try {
            loanDAO.returnLoan(loanId);
            return true;
        } catch (RecordNotFoundException e) {
            logger.warn("Prestito non trovato id={}", loanId);
            return false;
        } catch (DAOException e) {
            logger.error("Errore DAO restituzione prestito id={}", loanId, e);
            return false;
        }
    }

    // ===================== MAPPING =====================
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

            BookBean bookBean = safeGetBookBeanForLoan(loan.getBookId(), loan.getId());
            bean.setBook(bookBean);

            return bean;
        } catch (Exception e) {
            logger.warn("Errore mapping prestito id={}", loan.getId(), e);
            return null;
        }
    }

    private BookBean safeGetBookBeanForLoan(int bookId, int loanId) {
        try {
            Book book = bookDAO.getBookById(bookId);
            if (book != null) {
                return mapBookToBean(book);
            }
        } catch (DAOException e) {
            logger.warn("Impossibile recuperare il libro ID {} per prestito id={}", bookId, loanId, e);
        }

        // fallback: bean con solo ID libro
        BookBean fallback = new BookBean();
        try {
            fallback.setId(bookId);
        } catch (IncorrectDataException e) {
            logger.warn("Impossibile impostare ID del BookBean fallback per libro ID {} prestito id={}", bookId, loanId, e);
        }
        return fallback;
    }

    private BookBean mapBookToBean(Book book) {
        if (book == null) return null;
        BookBean bean = new BookBean();
        try {
            bean.setId(book.getId());
            bean.setTitle(book.getTitle());
            bean.setAuthor(book.getAuthor());
            bean.setStock(book.getStock());
            bean.setCategory(book.getCategory());
            bean.setPrice(book.getPrice());
            bean.setImagePath(book.getImagePath());
        } catch (Exception e) {
            logger.warn("Errore mapping book id={}", book.getId(), e);
            return null;
        }
        return bean;
    }
}