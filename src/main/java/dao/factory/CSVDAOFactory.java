package dao.factory;

import dao.*;
import dao.csv.*;

public class CSVDAOFactory extends DAOFactory {

    @Override
    protected BookDAO createBookDAO() {
        return new CSVBookDAO();
    }

    @Override
    protected CategoryDAO createCategoryDAO() {
        return new CSVCategoryDAO();
    }

    @Override
    protected PostDAO createPostDAO() {
        return new CSVPostDAO();
    }

    @Override
    protected AccountDAO createAccountDAO() {
        return new CSVAccountDAO();
    }

    @Override
    protected UserDAO createUserDAO() {
        return new CSVUserDAO();
    }

    @Override
    protected LoanDAO createLoanDAO() {
        return new CSVLoanDAO();
    }

    @Override
    protected PurchaseDAO createPurchaseDAO() {
        return new CSVPurchaseDAO();
    }

    @Override
    protected WishlistDAO createWishlistDAO() {
        return new CSVWishlistDAO();
    }
}