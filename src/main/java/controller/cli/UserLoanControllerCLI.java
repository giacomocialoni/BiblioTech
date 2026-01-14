package controller.cli;

import java.util.List;
import java.util.Scanner;

import app.Session;
import bean.LoanBean;
import controller.app.facade.UserLoanFacade;
import utils.LoanStatus;

public class UserLoanControllerCLI {
    
    private final Scanner scanner;
    private final UserLoanFacade userLoanFacade;
    
    public UserLoanControllerCLI(Scanner scanner) {
        this.scanner = scanner;
        this.userLoanFacade = new UserLoanFacade();
    }
    
    public void start() {
        if (!Session.getInstance().isLoggedIn()) {
            System.out.println("Devi essere loggato.");
            return;
        }
        
        List<LoanBean> prestiti = userLoanFacade.getUserAllLoans();
        
        System.out.println("\n=== I TUOI PRESTITI ===");
        
        if (prestiti.isEmpty()) {
            System.out.println("Nessun prestito.");
        } else {
            for (LoanBean prestito : prestiti) {
                System.out.println("------------------------");
                System.out.println("ID: " + prestito.getId());
                
                if (prestito.getBook() != null) {
                    System.out.println("Libro: " + prestito.getBook().getTitle());
                    System.out.println("Autore: " + prestito.getBook().getAuthor());
                }
                
                System.out.println("Stato: " + getStato(prestito.getStatus()));
                System.out.println("Data prenotazione: " + prestito.getReservedDate());
                
                if (prestito.getLoanedDate() != null) {
                    System.out.println("Data inizio: " + prestito.getLoanedDate());
                }
                
                if (prestito.getReturningDate() != null) {
                    System.out.println("Data restituzione: " + prestito.getReturningDate());
                }
            }
        }
        
        System.out.println("\nPremi Invio per tornare indietro...");
        scanner.nextLine();
    }
    
    private String getStato(LoanStatus stato) {
        if (stato == null) return "Sconosciuto";
        
        switch (stato) {
            case RESERVED: return "In attesa";
            case LOANED: return "In prestito";
            case EXPIRED: return "Scaduto";
            case RETURNED: return "Restituito";
            default: return stato.name();
        }
    }
}