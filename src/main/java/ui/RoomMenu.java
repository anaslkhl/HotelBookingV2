package ui;

import exception.RoomNotFoundException;
import model.Room;
import service.RoomService;

import java.util.List;
import java.util.Scanner;

public class RoomMenu {

    private final Scanner scanner;
    private final RoomService roomService;

    public RoomMenu(
            Scanner scanner,
            RoomService roomService
    ) {
        this.scanner = scanner;
        this.roomService = roomService;
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("""
                    
                    ================ ROOM MENU ================
                    1. List all rooms
                    2. Find room
                    3. Search available rooms
                    0. Back
                    ===========================================
                    """);

            System.out.print("> Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> listRooms();
                case "2" -> findRoom();
                case "3" -> searchAvailableRooms();
                case "0" -> running = false;

                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void listRooms() {
        List<Room> rooms = roomService.getAllRooms();

        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }

        System.out.println("\n==================== ROOMS ====================");

        for (Room room : rooms) {
            System.out.println("---------------------------------------------");
            System.out.println("Room number : " + room.getRoomNumber());
            System.out.println("Type        : " + room.getType());
            System.out.println("Status      : " + room.getStatus());
            System.out.println("Capacity    : " + room.getCapacity());
            System.out.println("Price/night : " + room.getPricePerNight());
        }

        System.out.println("===============================================\n");
    }

    private void findRoom() {
        try {
            System.out.print("Room number : ");
            String roomNumber = scanner.nextLine();

            Room room = roomService.getRoomByNumber(roomNumber);

            System.out.println("Room number : " + room.getRoomNumber());
            System.out.println("Type        : " + room.getType());
            System.out.println("Status      : " + room.getStatus());
            System.out.println("Capacity    : " + room.getCapacity());
            System.out.println("Price/night : " + room.getPricePerNight());

        } catch (RoomNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private void searchAvailableRooms() {
        List<Room> rooms = roomService.getAvailableRooms();

        if (rooms.isEmpty()) {
            System.out.println("No available rooms found.");
            return;
        }

        System.out.println("\n============== AVAILABLE ROOMS ==============");

        for (Room room : rooms) {
            System.out.println("---------------------------------------------");
            System.out.println("Room number : " + room.getRoomNumber());
            System.out.println("Type        : " + room.getType());
            System.out.println("Capacity    : " + room.getCapacity());
            System.out.println("Price/night : " + room.getPricePerNight());
        }

        System.out.println("=============================================\n");
    }
}