package dao.memory;

import dao.UserDAO;
import exception.DAOException;
import model.User;
import model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryUserDAO implements UserDAO {

    private static InMemoryUserDAO instance = null;
    private final List<Account> accounts;

    public InMemoryUserDAO() {
        // Ottieni la lista accounts dal DAO Account
        this.accounts = new ArrayList<>();
        accounts.add(new User("user@bibliotech.com", "userpass", "Bob", "User"));
        accounts.add(new User("mario.rossi@email.com", "password", "Mario", "Rossi"));
        accounts.add(new User("laura.bianchi@email.com", "password", "Laura", "Bianchi"));
    }

    public static InMemoryUserDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryUserDAO();
        }
        return instance;
    }

    @Override
    public User getUser(String email) throws DAOException {
        return accounts.stream()
                .filter(a -> a instanceof User)
                .map(a -> (User) a)
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> getAllUsers() throws DAOException {
        return accounts.stream()
                .filter(a -> a instanceof User)
                .map(a -> (User) a)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> searchUsers(String searchTerm) throws DAOException {
        String lowerTerm = searchTerm.toLowerCase();
        return accounts.stream()
                .filter(a -> a instanceof User)
                .map(a -> (User) a)
                .filter(u -> 
                    u.getEmail().toLowerCase().contains(lowerTerm) ||
                    u.getFirstName().toLowerCase().contains(lowerTerm) ||
                    u.getLastName().toLowerCase().contains(lowerTerm))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(String email) throws DAOException {
        accounts.removeIf(a -> a instanceof User && a.getEmail().equalsIgnoreCase(email));
    }
}