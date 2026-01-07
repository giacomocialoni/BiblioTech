package app;

import java.util.Scanner;

import controller.cli.MainGuestControllerCLI;

public class ApplicationCLI {
	
    public static void start() {
        Scanner scanner = new Scanner(System.in);
        MainGuestControllerCLI guestController = new MainGuestControllerCLI(scanner);
        guestController.start();
        scanner.close();
    }
}