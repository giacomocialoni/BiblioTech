package app;

import model.Account;

/**
 * Singleton che rappresenta la sessione utente.
 * Supporta multilogin consecutivi tramite reset/init controllati.
 */
public final class Session {

    private static Session instance;
    private Account loggedAccount;

    private Session(Account account) {
        this.loggedAccount = account;
    }

    // ====== INIT SINGLETON ======
    /**
     * Inizializza la sessione con un utente.
     * Lancia eccezione se già inizializzata.
     */
    public static synchronized void init(Account account) {
        if (instance != null) {
            throw new IllegalStateException("Session already initialized. Call reset() before init() for a new login.");
        }
        instance = new Session(account);
    }

    // ====== RESET SINGLETON ======
    /**
     * Resetta completamente la sessione.
     * Permette un nuovo login consecutivo.
     */
    public static synchronized void reset() {
        instance = null;
    }

    // ====== ACCESS ======
    /**
     * Ritorna l'istanza della sessione.
     * Lancia eccezione se non inizializzata.
     */
    public static Session getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Session not initialized. Call init() first.");
        }
        return instance;
    }

    // ====== LOGOUT ======
    /**
     * Effettua il logout pulendo lo stato interno.
     * Non distrugge l'istanza; per login consecutivi usare reset() + init().
     */
    public void logout() {
        this.loggedAccount = null;
    }

    // ====== SESSION LOGIC ======
    public boolean isLoggedIn() {
        return loggedAccount != null;
    }

    public Account getLoggedUser() {
        return loggedAccount;
    }

    public boolean isGuest() {
        return loggedAccount == null;
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