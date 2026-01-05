package controller.app;

import dao.BookDAO;
import dao.LoanDAO;
import dao.PurchaseDAO;
import dao.UserDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import model.Book;
import model.Loan;
import model.Purchase;
import model.User;
import bean.UserBean;
import view.dto.UserDisplayDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ManageUsersController {

    private static final Logger logger = LoggerFactory.getLogger(ManageUsersController.class);
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    
    private final BookDAO bookDAO;
    private final UserDAO userDAO;
    private final LoanDAO loanDAO;
    private final PurchaseDAO purchaseDAO;

    public ManageUsersController() {
        this.bookDAO = DAOFactory.getActiveFactory().getBookDAO();
        this.userDAO = DAOFactory.getActiveFactory().getUserDAO();
        this.loanDAO = DAOFactory.getActiveFactory().getLoanDAO();
        this.purchaseDAO = DAOFactory.getActiveFactory().getPurchaseDAO();
    }
    
    public List<UserDisplayDTO> getAllUsersForDisplay() {
        try {
            List<User> users = userDAO.getAllUsers();
            return users.stream()
                    .map(this::createUserDisplayDTO)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore nel recupero utenti per visualizzazione", e);
            return Collections.emptyList();
        }
    }
    
    public List<UserDisplayDTO> searchUsersForDisplay(String searchText) {
        try {
            List<User> filteredUsers = userDAO.getAllUsers();
            
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lower = searchText.toLowerCase();
                filteredUsers = filteredUsers.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(lower) ||
                                 u.getFirstName().toLowerCase().contains(lower) ||
                                 u.getLastName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
            }
            
            return filteredUsers.stream()
                    .map(this::createUserDisplayDTO)
                    .collect(Collectors.toList());
        } catch (DAOException e) {
            logger.error("Errore nella ricerca utenti", e);
            return Collections.emptyList();
        }
    }
    
    private UserDisplayDTO createUserDisplayDTO(User user) {
        UserBean userBean = mapUserToBean(user);
        String userEmail = user.getEmail();
        
        try {
            return new UserDisplayDTO(
                userBean,
                getLastPurchaseInfo(userEmail),
                getLastLoanInfo(userEmail),
                getStatsInfo(userEmail)
            );
        } catch (DAOException e) {
            logger.warn("Errore nel recupero attività per utente: {}", userEmail, e);
            return new UserDisplayDTO(
                userBean,
                "Ultimo acquisto: Dati non disponibili",
                "Ultimo prestito: Dati non disponibili",
                "Statistiche non disponibili"
            );
        }
    }
    
    // ===== METODI PER INFORMAZIONI UTENTE =====
    
    private String getLastPurchaseInfo(String userEmail) throws DAOException {
        List<Purchase> purchases = purchaseDAO.getPurchasesByUser(userEmail);
        
        if (purchases.isEmpty()) {
            return "Ultimo acquisto: Nessun acquisto";
        }
        
        Purchase lastPurchase = purchases.stream()
                .filter(p -> p.getStatusDate() != null)
                .max(Comparator.comparing(Purchase::getStatusDate))
                .orElse(purchases.get(0));
        
        Book book = bookDAO.getBookById(lastPurchase.getBookId());
        String bookTitle = book != null ? book.getTitle() : "Libro sconosciuto";
        
        String dateText = lastPurchase.getStatusDate() != null ?
                lastPurchase.getStatusDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)) :
                "Data non disponibile";
        
        return "Ultimo acquisto: " + bookTitle + " (" + dateText + ")";
    }
    
    private String getLastLoanInfo(String userEmail) throws DAOException {
        List<Loan> loans = loanDAO.getLoansByUser(userEmail);
        
        if (loans.isEmpty()) {
            return "Ultimo prestito: Nessun prestito";
        }
        
        Loan lastLoan = loans.stream()
                .filter(l -> l.getLoanedDate() != null)
                .max(Comparator.comparing(Loan::getLoanedDate))
                .orElse(loans.stream()
                        .filter(l -> l.getReservedDate() != null)
                        .max(Comparator.comparing(Loan::getReservedDate))
                        .orElse(loans.get(0)));
        
        Book book = bookDAO.getBookById(lastLoan.getBookId());
        String bookTitle = book != null ? book.getTitle() : "Libro sconosciuto";
        
        String dateText = lastLoan.getLoanedDate() != null ?
                lastLoan.getLoanedDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)) :
                (lastLoan.getReservedDate() != null ?
                 lastLoan.getReservedDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)) + " (prenotato)" :
                 "Data non disponibile");
        
        return "Ultimo prestito: " + bookTitle + " (" + dateText + ")";
    }
    
    private String getStatsInfo(String userEmail) throws DAOException {
        List<Purchase> purchases = purchaseDAO.getPurchasesByUser(userEmail);
        List<Loan> loans = loanDAO.getLoansByUser(userEmail);
        
        long completedPurchases = purchases.stream()
                .filter(p -> p.getStatusDate() != null)
                .count();
        
        long completedLoans = loans.stream()
                .filter(l -> l.getLoanedDate() != null && l.getReturningDate() != null)
                .count();
        
        long activeLoans = loans.stream()
                .filter(l -> l.getLoanedDate() != null && l.getReturningDate() == null)
                .count();
        
        long pendingReservations = loans.stream()
                .filter(l -> l.getLoanedDate() == null && l.getReservedDate() != null)
                .count();
        
        return String.format(
            "Statistiche: %d acquisti, %d prestiti completati, %d prestiti attivi, %d prenotazioni in sospeso",
            completedPurchases, completedLoans, activeLoans, pendingReservations
        );
    }

    // ===== METODI PER ALTRE FUNZIONALITÀ =====
    
    public boolean deleteUser(String email) {
        try {
            userDAO.deleteUser(email);
            return true;
        } catch (DAOException e) {
            logger.error("Errore nella cancellazione utente: {}", email, e);
            return false;
        }
    }
    
    public UserBean getUserByEmail(String email) {
        try {
            User user = userDAO.getUser(email);
            return mapUserToBean(user);
        } catch (DAOException e) {
            logger.warn("Utente non trovato: {}", email, e);
            return null;
        }
    }
    
    public int getTotalUsersCount() {
        try {
            return userDAO.getAllUsers().size();
        } catch (DAOException e) {
            logger.error("Errore nel conteggio utenti", e);
            return 0;
        }
    }

    // ===== MAPPING =====
    
    private UserBean mapUserToBean(User user) {
        UserBean bean = new UserBean();
        bean.setEmail(user.getEmail());
        bean.setFirstName(user.getFirstName());
        bean.setLastName(user.getLastName());
        bean.setPassword(user.getPassword());
        return bean;
    }
    
    // ===== METODI PUBBLICI PER COMPATIBILITÀ (se necessario) =====
    
    public List<UserBean> getLoggedUsers() {
        try {
            return mapUsersToBeans(userDAO.getAllUsers());
        } catch (DAOException e) {
            logger.error("Errore DAO recupero utenti loggati", e);
            return List.of();
        }
    }
    
    private List<UserBean> mapUsersToBeans(List<User> users) {
        return users.stream()
                .map(this::mapUserToBean)
                .collect(Collectors.toList());
    }
}