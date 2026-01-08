package controller.app;

import app.Session;
import bean.AccountBean;
import dao.AccountDAO;
import dao.factory.DAOFactory;
import exception.DAOException;
import exception.IncorrectDataException;
import model.Account;
import model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exception.EmailAlreadyRegisteredException;

public class SignInController {
 private static final Logger logger = LoggerFactory.getLogger(SignInController.class);
 private final AccountDAO accountDAO;

 public SignInController() {
     this.accountDAO = DAOFactory.getInstance().getAccountDAO();
 }

 public AccountBean signIn(String email, String password, String firstName, String lastName) 
         throws DAOException, EmailAlreadyRegisteredException {  
     
     // Validazione input
     if (email == null || email.isBlank() || password == null || password.isBlank() ||
         firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
         throw new IllegalArgumentException("Tutti i campi sono obbligatori.");
     }

     boolean success;
     try {
         success = accountDAO.register(email, password, firstName, lastName);
         
     } catch (EmailAlreadyRegisteredException e) {
         logger.warn("Tentativo di registrazione con email già esistente: {}", email);
         logRegistrationAttempt(email, "duplicate_email");
         
         throw e;
         
     } catch (DAOException e) {
         logRegistrationAttempt(email, "database_error");
         throw new DAOException("Errore temporaneo nel sistema. Riprova tra qualche minuto.", e);
     }

     if (!success) {
         logger.error("Registrazione fallita senza eccezione per {}", email);
         throw new DAOException("Registrazione fallita per motivi sconosciuti");
     }

     logger.info("Nuovo utente registrato: {} {} ({})", firstName, lastName, email);
     logRegistrationAttempt(email, "success");

     // Login automatico dopo registrazione
     Account account = new User(email, password, firstName, lastName);
     Session.reset();
     Session.initLogin(account);

     try {
         AccountBean bean = new AccountBean();
         bean.setEmail(account.getEmail());
         bean.setFirstName(account.getFirstName());
         bean.setLastName(account.getLastName());
         bean.setRole(account.getRole());
         return bean;
     } catch (IncorrectDataException e) {
         throw new DAOException("Errore nella creazione dell'account", e);
     }
 }
 
 private void logRegistrationAttempt(String email, String outcome) {
     logger.info("Tentativo registrazione - Email: {}, Esito: {}, IP: {}, Ora: {}", 
                email, outcome, "unknown", new java.util.Date());
 }
}