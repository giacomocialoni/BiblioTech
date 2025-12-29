package controller.cli;

import java.util.Scanner;

import controller.app.LoginController;
import bean.AccountBean;

public class LoginControllerCLI {

    private final Scanner scanner;
    private final LoginController loginController;

    public LoginControllerCLI(Scanner scanner) {
        this.scanner = scanner;
        this.loginController = new LoginController();
    }

    public void start() {
        System.out.println("\n=== Login BiblioTech ===");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            AccountBean account = loginController.login(email, password);

            if (account == null) {
                System.out.println("Login fallito: email o password non corretti.");
                return;
            }

            System.out.println("Login effettuato con successo. Benvenuto, " + account.getFirstName() + "!");

            // In base al ruolo, reindirizza al menu corretto
            if ("ADMIN".equalsIgnoreCase(account.getRole())) {
                new MainAdminControllerCLI(scanner).start();
            } else {
                new MainUserControllerCLI(scanner).start();
            }

        } catch (Exception e) {
            System.out.println("Errore durante il login: " + e.getMessage());
        }
    }
}