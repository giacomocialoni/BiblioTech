package controller.app;

import bean.BookBean;
import bean.LoanBean;
import dao.BookDAO;
import dao.LoanDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.IncorrectDataException;
import exception.RecordNotFoundException;
import model.Book;
import model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;
import utils.LoanResult;

import java.util.List;
import java.util.stream.Collectors;

public class LoanController {

    private static final Logger logger =
            LoggerFactory.getLogger(LoanController.class);

    private final BookDAO bookDAO;
    private final LoanDAO loanDAO;

    public LoanController() {
        DAOFactory factory = DAOFactory.getActiveFactory();
        this.bookDAO = factory.getBookDAO();
        this.loanDAO = factory.getLoanDAO();
    }

    /* =========================
       USER OPERATIONS
       ========================= */

    public LoanResult loanBook(String userEmail, int bookId) {

        Book book;
        try {
            book = bookDAO.getBookById(bookId);
        } catch (RecordNotFoundException e) {
            logger.warn("Libro non trovato id={}", bookId);
            return LoanResult.ERROR;
        } catch (DAOException e) {
            logger.error("Errore DAO recupero libro id={}", bookId, e);
            return LoanResult.ERROR;
        }

        try {
			if (hasExpiredLoans(userEmail)) {
			    return LoanResult.EXPIRED_LOAN_EXISTS;
			}
		} catch (DAOException e) {
            logger.error("Errore impossibile recuperare hasExpiredLoans", e);
		}

        try {
			if (getActiveLoansCount(userEmail) >= Constants.MAX_ACTIVE_LOANS) {
			    return LoanResult.MAX_LOANS_REACHED;
			}
		} catch (DAOException e) {
            logger.error("Errore impossibile recuperare getActiveLoansCount", e);
		}

        if (book.getStock() <= 0) {
            return LoanResult.INSUFFICIENT_STOCK;
        }

        try {
            book.setStock(book.getStock() - 1);
            bookDAO.updateBook(book);
            loanDAO.addLoan(userEmail, bookId);
            return LoanResult.SUCCESS;

        } catch (DAOException e) {
            logger.error("Errore DAO durante prestito libro", e);
            return LoanResult.ERROR;
        }
    }

    public List<LoanBean> getUserActiveLoans(String userEmail) {
        try {
            return loanDAO.getActiveLoansByUser(userEmail)
                    .stream()
                    .map(this::toLoanBean)
                    .toList(); 
        } catch (DAOException e) {
            logger.error("Errore recupero prestiti attivi", e);
            return List.of(); 
        }
    }

    public List<LoanBean> getUserAllLoans(String userEmail) {
        try {
            return loanDAO.getLoansByUser(userEmail)
                    .stream()
                    .map(this::toLoanBean)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore recupero prestiti", e);
            return List.of();
        }
    }

    /* =========================
       ADMIN OPERATIONS
       ========================= */

    public List<LoanBean> getAllReservedLoans() {
        try {
            return loanDAO.getAllReservedLoans()
                    .stream()
                    .map(this::toLoanBean)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore recupero prestiti riservati", e);
            return List.of();
        }
    }

    public List<LoanBean> searchLoansByUser(String userText) {
        try {
            return loanDAO.searchLoansByUser(userText)
                    .stream()
                    .map(this::toLoanBean)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore ricerca prestiti per utente", e);
            return List.of();
        }
    }

    public List<LoanBean> searchLoansByBook(String bookText) {
        try {
            return loanDAO.searchLoansByBook(bookText)
                    .stream()
                    .map(this::toLoanBean)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore ricerca prestiti per libro", e);
            return List.of();
        }
    }

    public boolean acceptLoan(int loanId) {
        try {
            loanDAO.acceptLoan(loanId);
            return true;
        } catch (DAOException e) {
            logger.error("Errore accettazione prestito", e);
            return false;
        }
    }

    public boolean rejectLoan(int loanId) {
        try {
            loanDAO.deleteLoan(loanId);
            return true;
        } catch (DAOException e) {
            logger.error("Errore rifiuto prestito", e);
            return false;
        }
    }

    /* =========================
       INTERNAL LOGIC
       ========================= */

    private boolean hasExpiredLoans(String userEmail) throws DAOException {
        try {
            return loanDAO.getActiveLoansByUser(userEmail)
                    .stream()
                    .anyMatch(Loan::isExpired);
        } catch (RecordNotFoundException e) {
            return false;
        }
    }

    private int getActiveLoansCount(String userEmail) throws DAOException {
        try {
            return loanDAO.getActiveLoansByUser(userEmail).size();
        } catch (RecordNotFoundException e) {
            return 0;
        }
    }

    /* =========================
       MAPPING
       ========================= */

    private LoanBean toLoanBean(Loan loan) {
        LoanBean bean = new LoanBean();
        bean.setId(loan.getId());
        bean.setUserEmail(loan.getUserEmail());
        bean.setStatus(loan.getStatus());
        bean.setReservedDate(loan.getReservedDate());
        bean.setLoanedDate(loan.getLoanedDate());
        bean.setReturningDate(loan.getReturningDate());

        try {
            Book book = bookDAO.getBookById(loan.getBookId());
            bean.setBook(toBookBean(book));
        } catch (DAOException e) {
            logger.warn("Libro non trovato per loan id={}", loan.getId());
        }

        return bean;
    }

    private BookBean toBookBean(Book book) {
        BookBean bean = new BookBean();
        try {
            bean.setId(book.getId());
            bean.setTitle(book.getTitle());
            bean.setAuthor(book.getAuthor());
            bean.setStock(book.getStock());
            bean.setCategory(book.getCategory());
            bean.setImagePath(book.getImagePath() != null ? book.getImagePath() : "default.jpg");
        } catch (IncorrectDataException e) {
            logger.warn("Errore mapping BookBean", e);
        }
        return bean;
    }
}