package dao.factory;

import dao.AccountDAO;
import dao.BookDAO;
import dao.CategoryDAO;
import dao.LoanDAO;
import dao.PostDAO;
import dao.PurchaseDAO;
import dao.UserDAO;
import dao.WishlistDAO;
import dao.database.DBConnection;

public abstract class DAOFactory {
    protected BookDAO bookDAO;
    protected CategoryDAO categoryDAO;
    protected PostDAO postDAO;
    protected AccountDAO accountDAO;
    protected UserDAO userDAO;
    private LoanDAO loanDAO;
    private PurchaseDAO purchaseDAO;
    protected WishlistDAO wishlistDAO;

    private static DAOFactory activeFactory;
    
    // Metodi astratti
    protected abstract BookDAO createBookDAO();
    protected abstract CategoryDAO createCategoryDAO();
    protected abstract PostDAO createPostDAO();
    protected abstract AccountDAO createAccountDAO();
    protected abstract UserDAO createUserDAO();    
    protected abstract LoanDAO createLoanDAO();
    protected abstract PurchaseDAO createPurchaseDAO();
    protected abstract WishlistDAO createWishlistDAO();

    // Gestione singleton
    public static void setActiveFactory(DAOFactory factory) {
        activeFactory = factory;
    }

    public static DAOFactory getActiveFactory() {
        if (activeFactory == null) {
            throw new IllegalStateException("DAOFactory non inizializzata. Chiamare setActiveFactory() prima.");
        }
        return activeFactory;
    }

    // Getter DAO
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
            userDAO = createUserDAO();
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
    
    // Factory selector
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