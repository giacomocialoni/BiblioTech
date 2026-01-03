package dao;

import model.Account;
import exception.DAOException;
import exception.RecordNotFoundException;
import exception.EmailAlreadyRegisteredException;

public interface AccountDAO {
    Account login(String email, String password) throws DAOException, RecordNotFoundException;
    boolean register(String email, String password, String firstName, String lastName) 
            throws DAOException, EmailAlreadyRegisteredException;
}