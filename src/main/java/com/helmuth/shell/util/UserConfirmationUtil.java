package com.helmuth.shell.util;

import java.util.Scanner;

public class UserConfirmationUtil {
    private static final Scanner scanner = new Scanner(System.in);
    
    /**
     * Asks the user for confirmation with a custom message.
     * @param message The message to display to the user
     * @return true if user confirms, false otherwise
     */
    public static boolean confirm(String message) {
        System.out.println(message + " (yes/no)");
        String response = scanner.nextLine().toLowerCase().trim();
        return response.equals("yes") || response.equals("y");
    }

    /**
     * Asks the user for confirmation with a default message.
     * @param action The action to be confirmed
     * @return true if user confirms, false otherwise
     */
    public static boolean confirmAction(String action) {
        return confirm("Are you sure you want to " + action + "?");
    }
}
