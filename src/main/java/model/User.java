package model;

public class User extends Account {
    public User(String email, String password, String firstName, String lastName) {
        super(email, password, firstName, lastName, "logged_user"); // Ruolo fisso
    }
}