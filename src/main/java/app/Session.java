package app;

import model.Account;

/**
 * Singleton che rappresenta la sessione utente.
 * Supporta multilogin consecutivi tramite reset/init controllati.
 */
public final class Session {

    private static Session instance;
    private Account loggedAccount;
    private boolean isGuest;

    private Session(Account account, boolean isGuest) {
        this.loggedAccount = account;
        this.isGuest = isGuest;
    }

    // ====== INIT GUEST ======
    public static synchronized void initGuest() {
        if (instance != null) {
            throw new IllegalStateException("Session already initialized. Call reset() before initGuest() or initLogin()");
        }
        instance = new Session(null, true);
    }

    // ====== INIT LOGIN ======
    public static synchronized void initLogin(Account account) {
        if (instance != null) {
            throw new IllegalStateException("Session already initialized. Call reset() before initGuest() or initLogin()");
        }
        instance = new Session(account, false);
    }

    // ====== RESET ======
    public static synchronized void reset() {
        instance = null;
    }

    // ====== ACCESS ======
    public static Session getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Session not initialized. Call initGuest() or initLogin() first.");
        }
        return instance;
    }

    // ====== UTILITY ======
    public boolean isLoggedIn() {
        return !isGuest && loggedAccount != null;
    }

    public boolean isGuest() {
        return isGuest;
    }

    public boolean isUser() {
        return isLoggedIn() && "logged_user".equals(loggedAccount.getRole());
    }

    public boolean isAdmin() {
        return isLoggedIn() && "admin".equals(loggedAccount.getRole());
    }

    public Account getLoggedUser() {
        return loggedAccount;
    }

    public String getUserEmail() {
        return isLoggedIn() ? loggedAccount.getEmail() : null;
    }

    public String getUserRole() {
        if (isGuest) return "guest";
        return loggedAccount.getRole();
    }

    public void logout() {
        this.loggedAccount = null;
        this.isGuest = true; // dopo logout torni automaticamente guest
    }
}