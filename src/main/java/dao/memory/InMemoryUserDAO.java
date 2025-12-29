package dao.memory;

import java.util.List;

import dao.UserDAO;
import exception.DAOException;
import model.User;

public class InMemoryUserDAO implements UserDAO {

	@Override
	public User getUser(String email) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> getAllUsers() throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> searchUsers(String searchTerm) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteUser(String email) throws DAOException {
		// TODO Auto-generated method stub

	}

}
