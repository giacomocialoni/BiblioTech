package controller.cli;

import java.util.List;
import java.util.Scanner;

import app.Session;
import bean.LoanBean;
import controller.app.facade.AdminLoanFacade;

public class ReservationControllerCLI {
    
    private final Scanner scanner;
    private final AdminLoanFacade adminLoanFacade;
    
    public ReservationControllerCLI(Scanner scanner) {
        this.scanner = scanner;
        this.adminLoanFacade = new AdminLoanFacade();
    }
    
    public void start() {
        if (!Session.getInstance().isAdmin()) {
            System.out.println("Accesso negato. Solo admin.");
            return;
        }
        
        boolean running = true;
        
        while (running) {
            // Mostra direttamente i prestiti in attesa invece del menu
            mostraPrestitiInAttesaConMenu();
        }
    }
    
    private void mostraPrestitiInAttesaConMenu() {
        List<LoanBean> prestiti = adminLoanFacade.getAllReservedLoans();
        
        System.out.println("\n=== GESTIONE PRESTITI IN ATTESA (ADMIN) ===");
        
        if (prestiti.isEmpty()) {
            System.out.println("\nNessun prestito in attesa.");
            System.out.println("\n[C] Cerca prestiti per utente");
            System.out.println("[B] Torna indietro");
            System.out.print("Scelta: ");
            
            String choice = scanner.nextLine().trim().toUpperCase();
            
            switch (choice) {
                case "C":
                    cercaPrestiti();
                    break;
                case "B":
                    System.exit(0); // Esce dal ciclo while in start()
                    break;
                default:
                    System.out.println("Scelta non valida.");
            }
            return;
        }
        
        // Mostra la lista dei prestiti
        System.out.println("\n--- PRESTITI IN ATTESA DI CONFERMA ---");
        visualizzaPrestiti(prestiti);
        
        // Menu opzioni
        System.out.println("\n--- OPZIONI ---");
        System.out.println("[ID] Gestisci prestito (inserisci ID)");
        System.out.println("[C] Cerca prestiti per utente");
        System.out.println("[R] Ricarica lista");
        System.out.println("[B] Torna indietro");
        System.out.print("Scelta: ");
        
        String input = scanner.nextLine().trim().toUpperCase();
        
        if (input.equals("B")) {
            System.exit(0); // Esce dal ciclo while in start()
        } else if (input.equals("C")) {
            cercaPrestiti();
        } else if (input.equals("R")) {
            // Ricarica automaticamente tornando all'inizio del ciclo
            return;
        } else {
            try {
                int loanId = Integer.parseInt(input);
                gestisciPrestito(loanId);
            } catch (NumberFormatException e) {
                System.out.println("Input non valido.");
            }
        }
    }
    
    private void cercaPrestiti() {
        System.out.print("\nCerca per email utente: ");
        String email = scanner.nextLine().trim();
        
        if (email.isEmpty()) {
            System.out.println("Email vuota.");
            return;
        }
        
        List<LoanBean> risultati = adminLoanFacade.searchLoansByUser(email);
        
        if (risultati.isEmpty()) {
            System.out.println("Nessun risultato.");
            return;
        }
        
        System.out.println("\n--- RISULTATI PER " + email + " ---");
        visualizzaPrestiti(risultati);
        
        // Menu dopo la ricerca
        System.out.println("\n--- OPZIONI ---");
        System.out.println("[ID] Gestisci prestito (inserisci ID)");
        System.out.println("[B] Torna indietro");
        System.out.print("Scelta: ");
        
        String input = scanner.nextLine().trim().toUpperCase();
        
        if (!input.equals("B")) {
            try {
                int loanId = Integer.parseInt(input);
                gestisciPrestito(loanId);
            } catch (NumberFormatException e) {
                System.out.println("Input non valido.");
            }
        }
    }
    
    private void visualizzaPrestiti(List<LoanBean> prestiti) {
        System.out.println("ID | Utente | Titolo");
        System.out.println("-----------------------");
        
        for (LoanBean prestito : prestiti) {
            String titolo = prestito.getBook() != null ? 
                prestito.getBook().getTitle() : "Sconosciuto";
            
            System.out.printf("%-3d | %-20s | %-30s%n",
                prestito.getId(),
                truncate(prestito.getUserEmail(), 20),
                truncate(titolo, 30)
            );
        }
    }
    
    private void gestisciPrestito(int loanId) {
        System.out.println("\n--- GESTIONE PRESTITO ID " + loanId + " ---");
        System.out.println("[A] Accetta");
        System.out.println("[R] Rifiuta");
        System.out.println("[X] Annulla");
        System.out.print("Scelta: ");
        
        String scelta = scanner.nextLine().trim().toUpperCase();
        
        switch (scelta) {
            case "A":
                boolean accettato = adminLoanFacade.acceptLoan(loanId);
                if (accettato) {
                    System.out.println("Prestito accettato.");
                } else {
                    System.out.println("Errore.");
                }
                break;
                
            case "R":
                boolean rifiutato = adminLoanFacade.rejectLoan(loanId);
                if (rifiutato) {
                    System.out.println("Prestito rifiutato.");
                } else {
                    System.out.println("Errore.");
                }
                break;
                
            case "X":
                System.out.println("Annullato.");
                break;
                
            default:
                System.out.println("Scelta non valida.");
        }
        
        // Pausa prima di tornare alla lista
        System.out.print("\nPremi INVIO per continuare...");
        scanner.nextLine();
    }
    
    private String truncate(String str, int lunghezza) {
        if (str == null) return "";
        if (str.length() <= lunghezza) return str;
        return str.substring(0, lunghezza - 3) + "...";
    }
}