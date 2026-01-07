package dao;

import model.User;
import model.Wishlist;
import exception.DAOException;

import java.util.List;

public interface WishlistDAO {

    void addToWishlist(String userEmail, int bookId) throws DAOException;
    void removeFromWishlist(String userEmail, int bookId) throws DAOException;
    boolean isInWishlist(String userEmail, int bookId) throws DAOException;
    List<Wishlist> getWishlistByUser(String userEmail) throws DAOException;
    List<User> getUsersWithBookInWishlist(int bookId) throws DAOException;
}