package controller.cli;

import java.util.List;
import java.util.Scanner;

import bean.BookBean;
import controller.app.CatalogoController;

public class CatalogControllerCLI {

    private final Scanner scanner;
    private final CatalogoController catalogoController;

    public CatalogControllerCLI(Scanner scanner) {
        this.scanner = scanner;
        this.catalogoController = new CatalogoController();
    }

    public void start() {
        while (true) {
            System.out.println("\n=== Catalogo Libri ===");

            List<BookBean> books = catalogoController.getAllBooks();

            if (books.isEmpty()) {
                System.out.println("Nessun libro disponibile.");
                return;
            }

            for (BookBean b : books) {
                System.out.printf(
                        "[%d] %s - %s (%s) | stock: %d%n",
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getCategory(),
                        b.getStock()
                );
            }

            System.out.println("\nInserisci ID libro | [B] Indietro");
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("B")) {
                return;
            }

            try {
                int bookId = Integer.parseInt(input);
                new BookDetailControllerCLI(scanner, bookId).start();
            } catch (NumberFormatException e) {
                System.out.println("Input non valido.");
            }
        }
    }
}