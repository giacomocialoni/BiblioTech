package controller.cli;

import java.util.Scanner;

import app.Session;
import bean.BookBean;
import controller.app.BookDetailController;
import utils.BuyResult;
import utils.LoanResult;

public class BookDetailControllerCLI {

    private final Scanner scanner;
    private final int bookId;
    private final BookDetailController controller;

    public BookDetailControllerCLI(Scanner scanner, int bookId) {
        this.scanner = scanner;
        this.bookId = bookId;
        this.controller = new BookDetailController();
    }

    public void start() {
        BookBean book = controller.getBookById(bookId);

        if (book == null) {
            System.out.println("Libro non trovato.");
            return;
        }

        boolean running = true;

        while (running) {
            printBook(book);
            printMenu();

            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "B":
                    running = false;
                    break;

                case "C":
                    handleBuy();
                    break;

                case "L":
                    handleLoan();
                    break;

                default:
                    System.out.println("Opzione non valida.");
            }
        }
    }

    // ================== ACTIONS ==================

    private void handleBuy() {
        if (!Session.getInstance().isLoggedIn()) {
            System.out.println("Devi essere loggato per acquistare un libro.");
            return;
        }

        System.out.print("Quantità da acquistare: ");
        int qty;

        try {
            qty = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Quantità non valida.");
            return;
        }

        BuyResult result = controller.buyBook(bookId, qty);
        System.out.println("Risultato acquisto: " + result);
    }

    private void handleLoan() {
        if (!Session.getInstance().isLoggedIn()) {
            System.out.println("Devi essere loggato per prendere un libro in prestito.");
            return;
        }

        LoanResult result = controller.loanBook(bookId);
        System.out.println("Risultato prestito: " + result);
    }

    // ================== PRINT ==================

    private void printMenu() {
        System.out.println("\n[C] Compra");
        System.out.println("[L] Prendi in prestito");
        System.out.println("[B] Torna indietro");
        System.out.print("> ");
    }

    private void printBook(BookBean b) {
        System.out.println("\n=== Dettaglio Libro ===");
        System.out.println("Titolo: " + b.getTitle());
        System.out.println("Autore: " + b.getAuthor());
        System.out.println("Categoria: " + b.getCategory());
        System.out.println("Anno: " + b.getYear());
        System.out.println("Editore: " + b.getPublisher());
        System.out.println("Pagine: " + b.getPages());
        System.out.println("ISBN: " + b.getIsbn());
        System.out.println("Prezzo: €" + b.getPrice());
        System.out.println("Disponibilità: " + b.getStock());
        System.out.println("\nTrama:\n" + b.getPlot());
    }
}