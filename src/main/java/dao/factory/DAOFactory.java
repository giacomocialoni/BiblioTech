package dao.factory;

import dao.*;
import dao.database.DBConnection;

public abstract class DAOFactory {

    protected BookDAO bookDAO;
    protected CategoryDAO categoryDAO;
    protected PostDAO postDAO;
    protected AccountDAO accountDAO;
    protected UserDAO userDAO;
    protected LoanDAO loanDAO;
    protected PurchaseDAO purchaseDAO;
    protected WishlistDAO wishlistDAO;

    // --- Metodi astratti per creare i DAO ---
    protected abstract BookDAO createBookDAO();
    protected abstract CategoryDAO createCategoryDAO();
    protected abstract PostDAO createPostDAO();
    protected abstract AccountDAO createAccountDAO();
    protected abstract UserDAO createUserDAO();    
    protected abstract LoanDAO createLoanDAO();
    protected abstract PurchaseDAO createPurchaseDAO();
    protected abstract WishlistDAO createWishlistDAO();

    // --- Getter DAO con lazy initialization ---
    public BookDAO getBookDAO() {
        if (bookDAO == null)
            bookDAO = createBookDAO();
        return bookDAO;
    }

    public CategoryDAO getCategoryDAO() {
        if (categoryDAO == null)
            categoryDAO = createCategoryDAO();
        return categoryDAO;
    }

    public PostDAO getPostDAO() {
        if (postDAO == null)
            postDAO = createPostDAO();
        return postDAO;
    }

    public AccountDAO getAccountDAO() {
        if (accountDAO == null)
            accountDAO = createAccountDAO();
        return accountDAO;
    }

    public UserDAO getUserDAO() {
        if (userDAO == null)
            return createUserDAO();
        return userDAO;
    }

    public LoanDAO getLoanDAO() {
        if (loanDAO == null)
            loanDAO = createLoanDAO();
        return loanDAO;
    }

    public PurchaseDAO getPurchaseDAO() {
        if (purchaseDAO == null)
            purchaseDAO = createPurchaseDAO();
        return purchaseDAO;
    }

    public WishlistDAO getWishlistDAO() {
        if (wishlistDAO == null)
            wishlistDAO = createWishlistDAO();
        return wishlistDAO;
    }

    // --- Factory selector ---
    public static DAOFactory getFactory(String mode, DBConnection dbConnection) {
        switch (mode) {
            case "CSV":
                return new CSVDAOFactory();
            case "DB":
                if (dbConnection == null)
                    throw new IllegalArgumentException("DBConnection required for DB mode");
                return new DatabaseDAOFactory(dbConnection);
            default:
                throw new IllegalArgumentException("Invalid mode: " + mode);
        }
    }
}