package controller.cli;

import java.util.Scanner;
import app.Session;
import model.Account;
import model.Admin;

public class ProfileControllerCLI {
    
    private final Scanner scanner;
    
    public ProfileControllerCLI(Scanner scanner) {
        this.scanner = scanner;
    }
    
    public void start() {
        if (!Session.getInstance().isLoggedIn()) {
            System.out.println("Devi essere loggato.");
            return;
        }
        
        Account account = Session.getInstance().getLoggedUser();
        
        System.out.println("\n=== PROFILO ===");
        System.out.println("Nome: " + account.getFirstName());
        System.out.println("Cognome: " + account.getLastName());
        System.out.println("Email: " + account.getEmail());
        System.out.println("Password: " + account.getPassword());
        System.out.println("Ruolo: " + (account instanceof Admin ? "Admin" : "Utente"));
        
        System.out.println("\nPremi Invio per tornare indietro...");
        scanner.nextLine();
    }
}