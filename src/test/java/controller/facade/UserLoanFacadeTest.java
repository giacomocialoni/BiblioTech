package controller.facade;

import static org.junit.jupiter.api.Assertions.*;

import app.Session;
import controller.app.facade.UserLoanFacade;
import dao.factory.DAOFactory;
import model.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.LoanResult;

public class UserLoanFacadeTest {

    private UserLoanFacade facade;
    private Session session;

    @BeforeEach
    void setUp() {
        // Inizializza la factory singleton
        DAOFactory.init("INMEMORY", null);

        facade = new UserLoanFacade();
        session = Session.getInstance();
        session.logout(); // stato iniziale consistente
    }

    @Test
    void testLoanBookUserNotLogged() {
        LoanResult result = facade.loanBook(1);
        assertEquals(LoanResult.NOT_LOGGED, result);
    }

    @Test
    void testLoanBookUnauthorizedUser() {
        Admin admin = new Admin("admin@test.com", "password", "Nome", "Cognome");
        session.login(admin);

        LoanResult result = facade.loanBook(1);
        assertEquals(LoanResult.UNAUTHORIZED, result);
    }
}