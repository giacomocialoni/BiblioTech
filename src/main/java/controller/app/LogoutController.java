package controller.app;

import app.Session;

public class LogoutController {

    /**
     * Effettua il logout dell'utente dalla sessione.
     */
    public void logout() {
        if (!Session.getInstance().isLoggedIn()) {
            return;
        }
        Session.getInstance().logout();
    }
}