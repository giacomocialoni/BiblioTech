package app;

import model.Account;

public class Session {

    private static Session instance;
    private Account loggedUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null)
            instance = new Session();
        return instance;
    }

    public void login(Account account) {
        this.loggedUser = account;
    }

    public void logout() {
        this.loggedUser = null;
    }

    public boolean isLoggedIn() {
        boolean result = loggedUser != null;
        return result;
    }

    public Account getLoggedUser() {
        return loggedUser;
    }
    
    // Metodi per i ruoli specifici: admin, logged_user, guest
    public boolean isGuest() {
        return !isLoggedIn();
    }
    
    public boolean isUser() {
        if (!isLoggedIn()) {
            return false;
        }
        String role = loggedUser.getRole();
        boolean result = "logged_user".equals(role);
        return result;
    }
    
    public boolean isAdmin() {
        if (!isLoggedIn()) return false;
        String role = loggedUser.getRole();
        return "admin".equals(role);
    }
    
    public String getUserEmail() {
        return isLoggedIn() ? loggedUser.getEmail() : null;
    }
    
    public String getUserRole() {
        if (!isLoggedIn()) {
            return null;
        }
        String role = loggedUser.getRole();
        return role;
    }
}