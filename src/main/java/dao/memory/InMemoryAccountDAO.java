package dao.memory;

import dao.AccountDAO;
import exception.DAOException;
import exception.EmailAlreadyRegisteredException;
import exception.RecordNotFoundException;
import model.Account;
import model.Admin;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAccountDAO implements AccountDAO {

    private static InMemoryAccountDAO instance = null;
    private final List<Account> accounts = new ArrayList<>();

    public InMemoryAccountDAO() {
        // Crea admin e user di default
        accounts.add(new Admin("admin@bibliotech.com", "adminpass", "Alice", "Admin"));
        accounts.add(new User("user@bibliotech.com", "userpass", "Bob", "User"));
    }

    public static InMemoryAccountDAO getInstance() {
        if (instance == null) {
            instance = new InMemoryAccountDAO();
        }
        return instance;
    }

    @Override
    public Account login(String email, String password) throws DAOException {
        return accounts.stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(email) && a.getPassword().equals(password))
                .findFirst()
                .orElseThrow(() -> new RecordNotFoundException("Email o password non corretti"));
    }

    @Override
    public boolean register(String email, String password, String firstName, String lastName)
            throws DAOException, EmailAlreadyRegisteredException {
        throw new UnsupportedOperationException("Registrazione non disponibile nella demo CLI");
    }
}