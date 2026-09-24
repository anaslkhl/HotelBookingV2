package ui;

import exception.*;
import model.Client;
import service.AuthService;

import java.util.Scanner;
import java.util.UUID;

public class AuthMenu {

    private final Scanner scanner;
    private final AuthService authService;

    public AuthMenu(Scanner scanner, AuthService authService) {
        this.scanner = scanner;
        this.authService = authService;
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("""
                    
                    ================ AUTH MENU ================
                    1. Register new client
                    2. Login
                    3. Find client by ID
                    4. Update profile
                    5. Change password
                    6. Show current user
                    7. Logout
                    8. Initialize sample clients
                    0. Back
                    ==========================================
                    """);

            System.out.print("> Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> register();
                case "2" -> login();
                case "3" -> findById();
                case "4" -> updateProfile();
                case "5" -> changePassword();
                case "6" -> showCurrentUser();
                case "7" -> logout();
                case "8" -> initializeClients();
                case "0" -> running = false;

                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void register() {

        try {
            System.out.print("Full name : ");
            String fullName = scanner.nextLine();

            System.out.print("Email : ");
            String email = scanner.nextLine();

            System.out.print("Phone : ");
            String phone = scanner.nextLine();

            System.out.print("Password : ");
            String password = scanner.nextLine();

            Client client = authService.ajouterClient(
                    new Client(fullName, email, phone, password)
            );

            System.out.println("Client id : " + client.getId());

        } catch (InvalidClientInfoException | ClientAlreadyExistException | InvalidPasswordException e) {
            System.err.println(e.getMessage());
        }
    }

    private void login() {

        try {
            System.out.print("Email : ");
            String email = scanner.nextLine();

            System.out.print("Password : ");
            String password = scanner.nextLine();

            Client client = authService.Login(email, password);

            authService.setCurrentUser(client);

        } catch (ClientNotFoundException | PasswordIncorrectException e) {
            System.err.println(e.getMessage());
        }
    }

    private void findById() {

        try {
            System.out.print("Client ID (UUID) : ");
            UUID id = UUID.fromString(scanner.nextLine());

            Client client = authService.chercherClient(id);

            System.out.println("Name    : " + client.getFullName());
            System.out.println("Email   : " + client.getEmail());
            System.out.println("Phone   : " + client.getPhone());

        } catch (ClientNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format.");
        }
    }

    private void updateProfile() {

        try {
            Client current = authService.getCurrentUser();

            if (current == null) {
                System.out.println("You must login first.");
                return;
            }

            System.out.print("New full name : ");
            String fullName = scanner.nextLine();

            System.out.print("New email : ");
            String email = scanner.nextLine();

            System.out.print("New phone : ");
            String phone = scanner.nextLine();

            Client client = authService.updateProfile(current.getId(), fullName, email, phone);

            System.out.println("Profile updated : " + client.getFullName());

        } catch (ClientNotFoundException | InvalidClientInfoException | ClientAlreadyExistException e) {
            System.err.println(e.getMessage());
        }
    }

    private void changePassword() {

        try {
            Client current = authService.getCurrentUser();

            if (current == null) {
                System.out.println("You must login first.");
                return;
            }

            System.out.print("Current password : ");
            String currentPassword = scanner.nextLine();

            System.out.print("New password : ");
            String newPassword = scanner.nextLine();

            authService.changePassword(current.getId(), currentPassword, newPassword);

        } catch (ClientNotFoundException | InvalidPasswordException e) {
            System.err.println(e.getMessage());
        }
    }

    private void showCurrentUser() {

        Client current = authService.getCurrentUser();

        if (current == null) {
            System.out.println("No user is logged in.");
            return;
        }

        System.out.println("Name  : " + current.getFullName());
        System.out.println("Email : " + current.getEmail());
        System.out.println("Phone : " + current.getPhone());
        System.out.println("ID    : " + current.getId());
    }

    private void logout() {

        authService.logout();
        System.out.println("Logged out.");
    }

    private void initializeClients() {

        try {
            authService.initializeClients();
        } catch (ClientAlreadyExistException e) {
            System.err.println(e.getMessage());
        }
    }
}