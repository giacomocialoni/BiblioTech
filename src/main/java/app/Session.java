package app;

import model.Account;

public final class Session {

    private static final Session INSTANCE = new Session();
    private Account loggedAccount;

    private Session() {
        // Singleton intenzionale
    }

    public static Session getInstance() {
        return INSTANCE;
    }

    public void login(Account account) {
        this.loggedAccount = account;
    }

    public void logout() {
        this.loggedAccount = null;
    }

    public boolean isLoggedIn() {
        return loggedAccount != null;
    }

    public Account getLoggedUser() {
        return loggedAccount;
    }

    public boolean isGuest() {
        return !isLoggedIn();
    }

    public boolean isUser() {
        return isLoggedIn() && "logged_user".equals(loggedAccount.getRole());
    }

    public boolean isAdmin() {
        return isLoggedIn() && "admin".equals(loggedAccount.getRole());
    }

    public String getUserEmail() {
        return isLoggedIn() ? loggedAccount.getEmail() : null;
    }

    public String getUserRole() {
        return isLoggedIn() ? loggedAccount.getRole() : null;
    }
}