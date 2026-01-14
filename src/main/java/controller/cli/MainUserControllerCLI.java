package controller.cli;

import java.util.Scanner;
import controller.app.LogoutController;

public class MainUserControllerCLI {

    private final Scanner scanner;
    private final LogoutController logoutController;

    public MainUserControllerCLI(Scanner scanner) {
        this.scanner = scanner;
        this.logoutController = new LogoutController();
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "C":
                    new CatalogControllerCLI(scanner).start();
                    break;
                case "S":
                    new SearchControllerCLI(scanner).start();
                    break;
                case "B":
                    new BoardControllerCLI(scanner).start();
                    break;
                case "I":
                    new InfoControllerCLI(scanner).start();
                    break;
                case "P":
                    new ProfileControllerCLI(scanner).start();
                    break;
                case "L":
                    new UserLoanControllerCLI(scanner).start();
                    break;
                case "U":
                    System.out.println("Purchases da implementare.");
                    break;
                case "O":
                    logoutController.logout();
                    System.out.println("Logout effettuato.");
                    running = false;
                    break;
                case "Q":
                    System.out.println("Uscita dalla demo CLI.");
                    running = false;
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== BiblioTech CLI (User) ===");
        System.out.println("[C] Catalog");
        System.out.println("[S] Search");
        System.out.println("[B] Board");
        System.out.println("[I] Info");
        System.out.println("[P] Profile");
        System.out.println("[L] Loans (Prestiti)");
        System.out.println("[U] Purchases (Acquisti) - da implementare");
        System.out.println("[O] Logout");
        System.out.println("[Q] Quit");
        System.out.print("Scelta: ");
    }
}