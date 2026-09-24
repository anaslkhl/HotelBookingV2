import db.DatabaseInitializer;
import exception.*;
import model.Client;
import repository.jdbc.JdbcClientRepository;
import repository.jdbc.JdbcRoomRepository;
import repository.jdbc.JdbcReservationRepository;
import service.AuthService;
import service.ReservationService;
import service.RoomService;
import ui.MainMenu;

import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        DatabaseInitializer.initialize();

        JdbcClientRepository clientRepository =
                new JdbcClientRepository();

        JdbcRoomRepository roomRepository =
                new JdbcRoomRepository();

        JdbcReservationRepository reservationRepository =
                new JdbcReservationRepository();

        AuthService authService =
                new AuthService(clientRepository);

        RoomService roomService =
                new RoomService(roomRepository);

        ReservationService reservationService =
                new ReservationService(
                        reservationRepository,
                        clientRepository,
                        roomRepository
                );

        roomService.initializeRooms();

        MainMenu mainMenu = new MainMenu(
                authService,
                roomService,
                reservationService
        );

        mainMenu.start();
    }


}