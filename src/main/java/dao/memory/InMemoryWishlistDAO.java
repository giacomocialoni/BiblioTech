package dao.memory;

import java.util.List;

import dao.WishlistDAO;
import exception.DAOException;
import exception.RecordNotFoundException;
import model.User;
import model.Wishlist;

public class InMemoryWishlistDAO implements WishlistDAO {

	@Override
	public void addToWishlist(String userEmail, int bookId) throws DAOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeFromWishlist(String userEmail, int bookId) throws DAOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInWishlist(String userEmail, int bookId) throws DAOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Wishlist> getWishlistByUser(String userEmail) throws DAOException, RecordNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> getUsersWithBookInWishlist(int bookId) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

}
