package controller.cli;

import java.util.Scanner;

public class MainGuestControllerCLI {

    private final Scanner scanner;

    public MainGuestControllerCLI(Scanner scanner) {
        this.scanner = scanner;
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
                case "L":
                    new LoginControllerCLI(scanner).start();
                    break;
                case "Q":
                    running = false;
                    System.out.println("Uscita dalla demo CLI.");
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== BiblioTech CLI (Guest) ===");
        System.out.println("[C] Catalog");
        System.out.println("[S] Search");
        System.out.println("[B] Board");
        System.out.println("[I] Info");
        System.out.println("[L] Login");
        System.out.println("[Q] Quit");
        System.out.print("Scelta: ");
    }
}