package controller.app.facade;

import app.Session;
import bean.LoanBean;
import controller.app.LoanController;
import utils.LoanResult;

import java.util.List;

public class UserLoanFacade {

    private final LoanController loanController;
    private final Session session;

    public UserLoanFacade() {
        this.loanController = new LoanController();
        this.session = Session.getInstance();
    }

    public LoanResult loanBook(int bookId) {

        if (!session.isLoggedIn()) {
            return LoanResult.NOT_LOGGED;
        }

        if (!session.isUser()) {
            return LoanResult.UNAUTHORIZED;
        }

        return loanController.loanBook(
                session.getLoggedUser().getEmail(),
                bookId
        );
    }

    public List<LoanBean> getUserActiveLoans() {
        if (!session.isUser()) {
            return List.of();
        }
        return loanController.getUserActiveLoans(
                session.getLoggedUser().getEmail()
        );
    }
    
    public List<LoanBean> getUserAllLoans() {
        if (!session.isUser()) {
            return List.of();
        }
        return loanController.getUserAllLoans(
                session.getLoggedUser().getEmail()
        );
    }
}