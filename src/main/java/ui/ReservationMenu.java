package ui;

import exception.*;
import model.Client;
import model.Reservation;
import service.AuthService;
import service.ReservationService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ReservationMenu {

    private final Scanner scanner;
    private final AuthService authService;
    private final ReservationService reservationService;

    public ReservationMenu(
            Scanner scanner,
            AuthService authService,
            ReservationService reservationService
    ) {
        this.scanner = scanner;
        this.authService = authService;
        this.reservationService = reservationService;
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("""
                    
                    ============ RESERVATION MENU ============
                    1. Create reservation
                    2. My reservations
                    3. View reservation
                    4. Update reservation
                    5. Cancel reservation
                    0. Back
                    =========================================
                    """);

            System.out.print("> Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> createReservation();
                case "2" -> showMyReservations();
                case "3" -> showReservation();
                case "4" -> updateReservation();
                case "5" -> cancelReservation();
                case "0" -> running = false;

                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void createReservation() {

        Client current = authService.getCurrentUser();

        if (current == null) {
            System.out.println("You must login first.");
            return;
        }

        try {
            System.out.print("Room number : ");
            String roomNumber = scanner.nextLine();

            System.out.print("Check-in (YYYY-MM-DD) : ");
            LocalDate checkIn = LocalDate.parse(scanner.nextLine());

            System.out.print("Check-out (YYYY-MM-DD) : ");
            LocalDate checkOut = LocalDate.parse(scanner.nextLine());

            System.out.print("Number of guests : ");
            int numberOfGuests = Integer.parseInt(scanner.nextLine());

            Reservation reservation = reservationService.ajouterReservation(
                    current.getId(), roomNumber, checkIn, checkOut, numberOfGuests
            );

            if (reservation != null) {
                System.out.println("Reservation ID : " + reservation.getId());
                System.out.println("Total price     : " + reservation.getTotalPrice());
                System.out.println("Status          : " + reservation.getStatus());
            }

        } catch (DateTimeParseException e) {
            System.err.println("Invalid date. Use YYYY-MM-DD.");
        } catch (NumberFormatException e) {
            System.err.println("Invalid number.");
        } catch (ClientNotFoundException | RoomNotFoundException
                 | RoomCapacityException | RoomNotAvailableException
                 | InvalidReservationInfoException | InvalidClientInfoException e) {
            System.err.println(e.getMessage());
        }
    }

    private void showMyReservations() {

        Client current = authService.getCurrentUser();

        if (current == null) {
            System.out.println("You must login first.");
            return;
        }

        try {
            List<Reservation> reservations = reservationService.findByUserId(current.getId());
            reservationService.afficherReservation(reservations);
        } catch (ReservationNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private void showReservation() {

        try {
            System.out.print("Reservation ID (or first digits) : ");
            String shortId = scanner.nextLine();

            Reservation reservation = reservationService.getReservationShortId(shortId);

            if (reservation == null) {
                System.out.println("No reservation found for : " + shortId);
                return;
            }

            System.out.println("---------------------------------------------");
            System.out.println("Reservation ID : " + reservation.getId());
            System.out.println("Code           : " + reservation.getReservationCode());
            System.out.println("Room           : " + reservation.getRoomNumber());
            System.out.println("Check-in       : " + reservation.getCheckIn());
            System.out.println("Check-out      : " + reservation.getCheckOut());
            System.out.println("Guests         : " + reservation.getNumberOfGuests());
            System.out.println("Nights         : " + reservation.getNumberOfNights());
            System.out.println("Total price    : " + reservation.getTotalPrice());
            System.out.println("Status         : " + reservation.getStatus());
            System.out.println("---------------------------------------------");

        } catch (ReservationNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private void updateReservation() {

        Client current = authService.getCurrentUser();

        if (current == null) {
            System.out.println("You must login first.");
            return;
        }

        try {
            System.out.print("Reservation ID : ");
            UUID id = UUID.fromString(scanner.nextLine());

            System.out.print("New room number : ");
            String roomNumber = scanner.nextLine();

            System.out.print("New check-in (YYYY-MM-DD) : ");
            LocalDate checkIn = LocalDate.parse(scanner.nextLine());

            System.out.print("New check-out (YYYY-MM-DD) : ");
            LocalDate checkOut = LocalDate.parse(scanner.nextLine());

            Reservation reservation = reservationService.reservationUpdate(
                    id, current.getId(), checkIn, checkOut, roomNumber
            );

            if (reservation != null) {
                System.out.println("Reservation ID : " + reservation.getId());
                System.out.println("Total price     : " + reservation.getTotalPrice());
            }

        } catch (DateTimeParseException e) {
            System.err.println("Invalid date. Use YYYY-MM-DD.");
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid reservation UUID.");
        } catch (ReservationNotFoundException | RoomNotFoundException
                 | RoomCapacityException | RoomNotAvailableException e) {
            System.err.println(e.getMessage());
        }
    }

    private void cancelReservation() {

        Client current = authService.getCurrentUser();

        if (current == null) {
            System.out.println("You must login first.");
            return;
        }

        try {
            System.out.print("Reservation ID : ");
            UUID id = UUID.fromString(scanner.nextLine());

            Reservation reservation = reservationService.reservationCancelation(id, current.getId());

            if (reservation != null) {
                System.out.println("Reservation ID : " + reservation.getId()
                        + " -> " + reservation.getStatus());
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid reservation UUID.");
        } catch (ReservationNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}