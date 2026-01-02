package controller.facade;

import static org.junit.jupiter.api.Assertions.*;

import app.Session;
import controller.app.facade.AdminLoanFacade;
import dao.factory.DAOFactory;
import dao.factory.InMemoryDAOFactory;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdminLoanFacadeTest {

    private AdminLoanFacade facade;
    private Session session;

    @BeforeEach
    void setUp() {
    	DAOFactory.setActiveFactory(new InMemoryDAOFactory());

        facade = new AdminLoanFacade();
        session = Session.getInstance();
        session.logout();
    }

    @Test
    void testGetAllReservedLoansNotAdmin() {
        User user = new User("user@test.com", "password", "Nome", "Cognome");

        session.login(user);

        assertTrue(facade.getAllReservedLoans().isEmpty());
    }
}