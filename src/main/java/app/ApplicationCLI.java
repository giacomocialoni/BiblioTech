package app;

import java.util.Scanner;

import controller.cli.MainGuestControllerCLI;
import dao.factory.DAOFactory;

public class ApplicationCLI {

    public static void start(DAOFactory factory) {
        // Imposta la factory attiva (fondamentale)
        DAOFactory.setActiveFactory(factory);

        Scanner scanner = new Scanner(System.in);

        MainGuestControllerCLI guestController = new MainGuestControllerCLI(scanner);

        guestController.start();

        scanner.close();
    }
}