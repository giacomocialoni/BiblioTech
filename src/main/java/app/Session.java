package app;

import model.Account;

public final class Session {

    private static final Session INSTANCE = new Session();
    private Account loggedUser;

    private Session() {
        // Singleton intenzionale
    }

    public static Session getInstance() {
        return INSTANCE;
    }

    public void login(Account account) {
        this.loggedUser = account;
    }

    public void logout() {
        this.loggedUser = null;
    }

    public boolean isLoggedIn() {
        return loggedUser != null;
    }

    public Account getLoggedUser() {
        return loggedUser;
    }

    public boolean isGuest() {
        return !isLoggedIn();
    }

    public boolean isUser() {
        return isLoggedIn() && "logged_user".equals(loggedUser.getRole());
    }

    public boolean isAdmin() {
        return isLoggedIn() && "admin".equals(loggedUser.getRole());
    }

    public String getUserEmail() {
        return isLoggedIn() ? loggedUser.getEmail() : null;
    }

    public String getUserRole() {
        return isLoggedIn() ? loggedUser.getRole() : null;
    }
}