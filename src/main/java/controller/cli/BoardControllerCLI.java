package controller.cli;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import bean.PostBean;
import controller.app.BachecaController;

public class BoardControllerCLI {

    private final Scanner scanner;
    private final BachecaController controller;

    public BoardControllerCLI(Scanner scanner) {
        this.scanner = scanner;
        this.controller = new BachecaController();
    }

    public void start() {
        List<PostBean> posts = controller.getAllPostsOrderedByDate();

        System.out.println("\n=== Bacheca ===");

        if (posts.isEmpty()) {
            System.out.println("Nessun post disponibile.");
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (int i = 0; i < posts.size(); i++) {
                PostBean p = posts.get(i);

                System.out.println("\n[" + (i + 1) + "] " + p.getTitle());
                System.out.println("Autore: " + p.getAuthorName() + " (" + p.getRole() + ")");
                System.out.println("Data: " + p.getPostDate().format(fmt));
                System.out.println(p.getContent());
            }
        }

        System.out.println("\n[B] Indietro");
        System.out.print("> ");
        scanner.nextLine();
    }
}