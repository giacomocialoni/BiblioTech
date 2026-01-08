package controller.app;

import bean.AccountBean;
import dao.AccountDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.IncorrectDataException;
import exception.RecordNotFoundException;
import model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.Session;

public class LoginController {

    private final AccountDAO accountDAO;
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    public LoginController() {
        this.accountDAO = DAOFactory.getInstance().getAccountDAO();
    }

    public AccountBean login(String email, String password) throws DAOException {
        
        if (email == null || email.isBlank() || password == null || password.isBlank())
            throw new IllegalArgumentException("Email e password obbligatorie");

        Account account;
        try {
            account = accountDAO.login(email, password);
        } catch (RecordNotFoundException e) {
            logger.warn("Login fallito per {}", email, e);
            return null;
        } catch (DAOException e) {
            logger.warn("Errore nel DAO nel login per {}", email, e);
            return null;
        }

        if (account == null) {
            return null;
        }

        Session.reset();
        Session.initLogin(account);

        return accountToBean(account);
    }

    public boolean isLoggedIn() {
        return Session.getInstance().isLoggedIn();
    }

    public boolean isAdmin() {
        Account account = Session.getInstance().getLoggedUser();
        return account != null && account.isAdmin();
    }

    public AccountBean getLoggedUserBean() {
        Account account = Session.getInstance().getLoggedUser();
        if (account == null) return null;
        return accountToBean(account);
    }
    
    public static AccountBean accountToBean(Account account) {
        try {
            AccountBean bean = new AccountBean();
            bean.setEmail(account.getEmail());
            bean.setFirstName(account.getFirstName());
            bean.setLastName(account.getLastName());
            bean.setRole(account.getRole());
            return bean;
        } catch (IncorrectDataException e) {
            LoggerFactory.getLogger(LoginController.class)
                         .warn("Errore dati account: {}", account.getEmail(), e);
            return null;
        }
    }
}