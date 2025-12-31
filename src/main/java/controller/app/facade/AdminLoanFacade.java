package controller.app.facade;

import app.Session;
import bean.LoanBean;
import controller.app.LoanController;

import java.util.List;

public class AdminLoanFacade {

    private final LoanController loanController;
    private final Session session;

    public AdminLoanFacade() {
        this.loanController = new LoanController();
        this.session = Session.getInstance();
    }

    public List<LoanBean> getAllReservedLoans() {
        if (!session.isAdmin()) return List.of();
        return loanController.getAllReservedLoans();
    }

    public List<LoanBean> searchLoansByUser(String text) {
        if (!session.isAdmin()) return List.of();
        return loanController.searchLoansByUser(text);
    }

    public List<LoanBean> searchLoansByBook(String text) {
        if (!session.isAdmin()) return List.of();
        return loanController.searchLoansByBook(text);
    }

    public boolean acceptLoan(int loanId) {
        if (!session.isAdmin()) return false;
        return loanController.acceptLoan(loanId);
    }

    public boolean rejectLoan(int loanId) {
        if (!session.isAdmin()) return false;
        return loanController.rejectLoan(loanId);
    }
}