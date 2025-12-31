package model;

public class Admin extends Account {
    public Admin(String email, String password, String firstName, String lastName) {
        super(email, password, firstName, lastName, "admin"); // Ruolo fisso
    }
}