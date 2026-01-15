package controller.cli;

import java.util.Scanner;
import controller.app.LogoutController;

public class MainAdminControllerCLI {

    private final Scanner scanner;
    private final LogoutController logoutController;

    public MainAdminControllerCLI(Scanner scanner) {
        this.scanner = scanner;
        this.logoutController = new LogoutController();
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A":
                	new ReservationControllerCLI(scanner).start();
                    break;
                case "B":
                    System.out.println("Gestione Libri (da implementare).");
                    break;
                case "C":
                	System.out.println("Creaziione Libri (da implementare).");
                	break;
                case "U":
                    System.out.println("Gestione Utenti (da implementare).");
                    break;
                case "P":
                    System.out.println("Crea Post (da implementare).");
                    break;
                case "R":
                    System.out.println("Restituzione Libri (da implementare).");
                    break;
                case "L":
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
        System.out.println("\n=== BiblioTech CLI (Admin) ===");
        System.out.println("[A] Reservations");
        System.out.println("[B] Manage Books");
        System.out.println("[C] Create Book");
        System.out.println("[U] Manage Users");
        System.out.println("[P] Create Post");
        System.out.println("[R] Return Books");
        System.out.println("[L] Logout");
        System.out.println("[Q] Quit");
        System.out.print("Scelta: ");
    }
}