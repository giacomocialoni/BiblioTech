package controller.cli;

import java.util.Scanner;

public class InfoControllerCLI {

    private final Scanner scanner;

    public InfoControllerCLI(Scanner scanner) {
        this.scanner = scanner;
    }

    public void start() {
        System.out.println("\n=== Informazioni & Contatti BiblioTech ===\n");

        // Introduzione
        printSection("Come funziona la BiblioTech",
                "La BiblioTech è la versione digitale della biblioteca fisica. " +
                "Attraverso questa applicazione puoi consultare i libri disponibili, prenotarli per l’acquisto oppure richiederli in prestito.\n\n" +
                "La prenotazione non prevede spedizione: per ritirare o acquistare un libro è necessario recarsi in biblioteca presso: Via Biblioteca 123.");

        // Politiche
        printSection("Politiche di prestito e prenotazione",
                "I libri possono essere prenotati sia per l’acquisto sia per il prestito. Ogni utente può avere fino a tre prestiti attivi contemporaneamente.\n\n" +
                "Se un prestito supera la data di scadenza e risulta non restituito, non sarà possibile richiedere nuovi prestiti né prenotare libri per l’acquisto.\n\n" +
                "Tutte le operazioni di ritiro, restituzione e pagamento vengono effettuate esclusivamente presso la biblioteca.");

        // Uso dell'app
        printSection("Utilizzo dell’app",
                "Nella sezione Catalogo puoi scoprire l’intera collezione di libri disponibili in biblioteca e aprire la scheda di ciascun titolo per visualizzarne trama, dettagli e disponibilità.\n\n" +
                "Se sai già cosa stai cercando, la sezione Ricerca ti permette di filtrare i risultati per titolo, autore, categoria o anno, così da individuare rapidamente il libro desiderato.\n\n" +
                "La Bacheca raccoglie comunicazioni, annunci e aggiornamenti pubblicati dalla biblioteca, così da tenerti sempre informato sulle novità e sugli avvisi utili.\n\n" +
                "Per accedere alle prenotazioni e gestire le tue attività è necessario registrarsi o effettuare il login. Nella sezione Profilo puoi consultare le prenotazioni attive, i prestiti in corso e lo storico delle operazioni collegate al tuo account.");

        // Contatti
        printSection("Contatti",
                "Per informazioni o assistenza puoi contattare l'amministratore della biblioteca: Admin1 — tel. 333-123-4567 • admin@gmail.com");

        System.out.println("\n[B] Indietro");
        System.out.print("> ");
        scanner.nextLine();
    }

    private void printSection(String title, String content) {
        System.out.println("=== " + title + " ===");
        System.out.println(content + "\n");
    }
}