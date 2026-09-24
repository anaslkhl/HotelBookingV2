package ui;

import service.AuthService;
import service.ReservationService;
import service.RoomService;

import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner;
    private final AuthService authService;

    private final AuthMenu authMenu;
    private final RoomMenu roomMenu;
    private final ReservationMenu reservationMenu;

    public MainMenu(AuthService authService, RoomService roomService, ReservationService reservationService) {

        this.scanner = new Scanner(System.in);
        this.authService = authService;

        this.authMenu = new AuthMenu(scanner, authService);
        this.roomMenu = new RoomMenu(scanner, roomService);
        this.reservationMenu = new ReservationMenu(scanner, authService, reservationService);
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("""
                    
                    ==============================
                       HOTEL BOOKING SYSTEM
                    ==============================
                    1. Authentication
                    2. Rooms
                    3. Reservations
                    0. Exit
                    ==============================
                    """);

            System.out.print("> Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> authMenu.start();

                case "2" -> roomMenu.start();

                case "3" -> reservationMenu.start();

                case "0" -> running = false;

                default -> System.out.println("Invalid option.");
            }
        }

        System.out.println("Bye!");
    }
}