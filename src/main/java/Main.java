import db.DatabaseInitializer;
import exception.*;
import model.Client;
import repository.jdbc.JdbcClientRepository;
import service.AuthService;

import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        DatabaseInitializer.initialize();

        AuthService authService = new AuthService(new JdbcClientRepository());

        boolean running = true;

        while (running) {

            System.out.println("\n================ AUTH MENU ================");
            System.out.println("1. Register new client (ajouterClient)");
            System.out.println("2. Login");
            System.out.println("3. Find client by ID (chercherClient)");
            System.out.println("4. Update profile");
            System.out.println("5. Change password");
            System.out.println("6. Show current user");
            System.out.println("7. Logout");
            System.out.println("8. Initialize sample clients");
            System.out.println("0. Exit");
            System.out.println("==========================================");

            System.out.print("> Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> register(authService);
                case "2" -> login(authService);
                case "3" -> findById(authService);
                case "4" -> updateProfile(authService);
                case "5" -> changePassword(authService);
                case "6" -> showCurrentUser(authService);
                case "7" -> logout(authService);
                case "8" -> initializeClients(authService);
                case "0" -> running = false;
                default -> System.out.println("Invalid option, try again.");
            }
        }

        System.out.println("Bye !");
    }

    private static void register(AuthService authService) {

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

        } catch (InvalidClientInfoException | ClientAlreadyExistException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void login(AuthService authService) {

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

    private static void findById(AuthService authService) {

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

    private static void updateProfile(AuthService authService) {

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

    private static void changePassword(AuthService authService) {

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

    private static void showCurrentUser(AuthService authService) {

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

    private static void logout(AuthService authService) {

        authService.logout();
        System.out.println("Logged out.");
    }

    private static void initializeClients(AuthService authService) {

        try {
            authService.initializeClients();
        } catch (ClientAlreadyExistException e) {
            System.err.println(e.getMessage());
        }
    }
}