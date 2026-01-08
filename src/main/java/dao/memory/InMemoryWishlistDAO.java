package dao.memory;

import dao.WishlistDAO;
import exception.DAOException;
import model.User;
import model.Wishlist;

import java.util.ArrayList;
import java.util.List;

public class InMemoryWishlistDAO implements WishlistDAO {

    private static InMemoryWishlistDAO instance = null;
    private List<Wishlist> wishlistItems = new ArrayList<>();

    public InMemoryWishlistDAO() {
    	//costruttore vuoto
    }

    public static InMemoryWishlistDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryWishlistDAO();
        }
        return instance;
    }

    @Override
    public void addToWishlist(String userEmail, int bookId) throws DAOException {
        // Verifica se già esiste
        for (Wishlist item : wishlistItems) {
            if (item.getUserEmail().equals(userEmail) && item.getBookId() == bookId) {
                return; // Già presente, non fare nulla
            }
        }
        wishlistItems.add(new Wishlist(userEmail, bookId));
    }

    @Override
    public void removeFromWishlist(String userEmail, int bookId) throws DAOException {
        wishlistItems.removeIf(item -> 
            item.getUserEmail().equals(userEmail) && item.getBookId() == bookId
        );
    }

    @Override
    public boolean isInWishlist(String userEmail, int bookId) throws DAOException {
        for (Wishlist item : wishlistItems) {
            if (item.getUserEmail().equals(userEmail) && item.getBookId() == bookId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Wishlist> getWishlistByUser(String userEmail) throws DAOException {
        List<Wishlist> userWishlist = new ArrayList<>();
        for (Wishlist item : wishlistItems) {
            if (item.getUserEmail().equals(userEmail)) {
                userWishlist.add(item);
            }
        }
        return userWishlist;
    }

    @Override
    public List<User> getUsersWithBookInWishlist(int bookId) throws DAOException {
        List<User> users = new ArrayList<>();
        for (Wishlist item : wishlistItems) {
            if (item.getBookId() == bookId) {
                // Crea un utente con solo l'email (altri campi vuoti)
                users.add(new User(item.getUserEmail(), "", "", ""));
            }
        }
        return users;
    }
}