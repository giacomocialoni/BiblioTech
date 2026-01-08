package app;

import java.util.Scanner;
import controller.cli.MainGuestControllerCLI;

public final class ApplicationCLI {

    private ApplicationCLI() {
        // Utility class
    }

    public static void start() {
        Scanner scanner = new Scanner(System.in);
        MainGuestControllerCLI guestController = new MainGuestControllerCLI(scanner);
        guestController.start();
        scanner.close();
    }
}