package app;

import model.Account;

public final class Session {

    private Account loggedUser;

    private Session() {
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("Use getInstance()");
        }
    }

    private static class Holder {
        private static final Session INSTANCE = new Session();
    }

    public static Session getInstance() {
        return Holder.INSTANCE;
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