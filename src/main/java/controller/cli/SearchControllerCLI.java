package controller.cli;

import java.util.List;
import java.util.Scanner;

import bean.BookBean;
import controller.app.CercaController;

public class SearchControllerCLI {

    private final Scanner scanner;
    private final CercaController controller;

    public SearchControllerCLI(Scanner scanner) {
        this.scanner = scanner;
        this.controller = new CercaController();
    }

    public void start() {
        System.out.println("\n=== Ricerca Libro (per titolo) ===");
        System.out.print("Inserisci titolo (o parte): ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Ricerca annullata.");
            return;
        }

        List<BookBean> results =
                controller.searchBooks(query, "TITLE", null, null, null, true);

        if (results.isEmpty()) {
            System.out.println("Nessun libro trovato.");
            return;
        }

        while (true) {
            printResults(results);

            System.out.println("\nSeleziona numero libro oppure [B] Indietro");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("B")) {
                return;
            }

            try {
                int index = Integer.parseInt(choice) - 1;

                if (index < 0 || index >= results.size()) {
                    System.out.println("Indice non valido.");
                    continue;
                }

                int bookId = results.get(index).getId();
                new BookDetailControllerCLI(scanner, bookId).start();

            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
            }
        }
    }

    private void printResults(List<BookBean> books) {
        System.out.println("\n=== Risultati ===");
        for (int i = 0; i < books.size(); i++) {
            BookBean b = books.get(i);
            System.out.printf("[%d] %s — %s (disp: %d)%n",
                    i + 1,
                    b.getTitle(),
                    b.getAuthor(),
                    b.getStock());
        }
    }
}