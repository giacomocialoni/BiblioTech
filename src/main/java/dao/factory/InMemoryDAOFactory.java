package dao.factory;

import dao.*;
import dao.memory.*;

public class InMemoryDAOFactory extends DAOFactory {

    @Override
    protected BookDAO createBookDAO() {
        return new InMemoryBookDAO();
    }

    @Override
    protected CategoryDAO createCategoryDAO() {
        return new InMemoryCategoryDAO();
    }

    @Override
    protected PostDAO createPostDAO() {
        return new InMemoryPostDAO();
    }

    @Override
    protected AccountDAO createAccountDAO() {
        return new InMemoryAccountDAO();
    }

    @Override
    protected UserDAO createUserDAO() {
        return new InMemoryUserDAO();
    }

    @Override
    protected LoanDAO createLoanDAO() {
        return new InMemoryLoanDAO();
    }

    @Override
    protected PurchaseDAO createPurchaseDAO() {
        return new InMemoryPurchaseDAO();
    }

    @Override
    protected WishlistDAO createWishlistDAO() {
        return new InMemoryWishlistDAO();
    }
}