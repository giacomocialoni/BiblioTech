package bean;

import exception.IncorrectDataException;

public class AccountBean {
    protected String email;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String role; // "admin" o "logged_user" o null

    // ================== GETTER ==================
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
    
    public boolean isUser() {
        return "logged_user".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        String roleStr = (role == null) ? "guest" : role;
        return firstName + " " + lastName + " (" + roleStr + ")";
    }

    // ================== SETTER CON VALIDAZIONE ==================
    public void setEmail(String email) throws IncorrectDataException {
        if (email == null || email.isBlank()) {
            throw new IncorrectDataException("Email non valida");
        }
        this.email = email;
    }

    public void setPassword(String password) throws IncorrectDataException {
        if (password == null || password.isBlank()) {
            throw new IncorrectDataException("Password non valida");
        }
        this.password = password;
    }

    public void setFirstName(String firstName) throws IncorrectDataException {
        if (firstName == null || firstName.isBlank()) {
            throw new IncorrectDataException("Nome non valido");
        }
        this.firstName = firstName;
    }

    public void setLastName(String lastName) throws IncorrectDataException {
        if (lastName == null || lastName.isBlank()) {
            throw new IncorrectDataException("Cognome non valido");
        }
        this.lastName = lastName;
    }

    public void setRole(String role) throws IncorrectDataException {
        // Permette null per guest, altrimenti solo valori specifici
        if (role != null && !role.isBlank() && !"admin".equalsIgnoreCase(role) && !"logged_user".equalsIgnoreCase(role)) {
            throw new IncorrectDataException("Ruolo non valido. Usa 'admin', 'logged_user' o null");
        }
        this.role = role;
    }
}