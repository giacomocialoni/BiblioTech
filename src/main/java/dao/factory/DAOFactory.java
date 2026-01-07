package dao.factory;

import dao.*;
import dao.database.DBConnection;

public abstract class DAOFactory {

    private static DAOFactory instance;

    public BookDAO bookDAO;
    protected CategoryDAO categoryDAO;
    protected PostDAO postDAO;
    protected AccountDAO accountDAO;
    protected UserDAO userDAO;
    protected LoanDAO loanDAO;
    protected PurchaseDAO purchaseDAO;
    protected WishlistDAO wishlistDAO;

    protected DAOFactory() {}

    // ====== FACTORY INIT ======
    public static synchronized void init(String mode, DBConnection dbConnection) {
        if (instance != null) {
            throw new IllegalStateException("DAOFactory già inizializzata");
        }

        switch (mode) {
            case "CSV":
                instance = new CSVDAOFactory();
                break;
            case "DB":
                if (dbConnection == null) {
                    throw new IllegalArgumentException("DBConnection required for DB mode");
                }
                instance = new DatabaseDAOFactory(dbConnection);
                break;
            default:
                throw new IllegalArgumentException("Invalid mode: " + mode);
        }
    }

    // ====== SINGLETON ACCESS ======
    public static DAOFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DAOFactory non inizializzata");
        }
        return instance;
    }

    // ====== DAO CREATORS ======
    protected abstract BookDAO createBookDAO();
    protected abstract CategoryDAO createCategoryDAO();
    protected abstract PostDAO createPostDAO();
    protected abstract AccountDAO createAccountDAO();
    protected abstract UserDAO createUserDAO();
    protected abstract LoanDAO createLoanDAO();
    protected abstract PurchaseDAO createPurchaseDAO();
    protected abstract WishlistDAO createWishlistDAO();

    // ====== DAO GETTERS ======
    public BookDAO getBookDAO() {
        if (bookDAO == null) bookDAO = createBookDAO();
        return bookDAO;
    }

    public CategoryDAO getCategoryDAO() {
        if (categoryDAO == null) categoryDAO = createCategoryDAO();
        return categoryDAO;
    }

    public PostDAO getPostDAO() {
        if (postDAO == null) postDAO = createPostDAO();
        return postDAO;
    }

    public AccountDAO getAccountDAO() {
        if (accountDAO == null) accountDAO = createAccountDAO();
        return accountDAO;
    }

    public UserDAO getUserDAO() {
        if (userDAO == null) userDAO = createUserDAO();
        return userDAO;
    }

    public LoanDAO getLoanDAO() {
        if (loanDAO == null) loanDAO = createLoanDAO();
        return loanDAO;
    }

    public PurchaseDAO getPurchaseDAO() {
        if (purchaseDAO == null) purchaseDAO = createPurchaseDAO();
        return purchaseDAO;
    }

    public WishlistDAO getWishlistDAO() {
        if (wishlistDAO == null) wishlistDAO = createWishlistDAO();
        return wishlistDAO;
    }
    
    public void setCustomBookDAO(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }
}