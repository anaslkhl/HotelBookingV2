package service;

import model.Room;
import model.enums.RoomStatus;
import model.enums.RoomType;
import exception.RoomNotFoundException;
import repository.jdbc.JdbcRoomRepository;

import java.math.BigDecimal;
import java.util.List;

public class RoomService {


    private final JdbcRoomRepository roomRepository;
    private int counter = 100;

    public RoomService(JdbcRoomRepository roomRepository){
        this.roomRepository = roomRepository;
    }


    public Room ajouterRoom(RoomType type, int capacity, BigDecimal pricePerNight) {

        String roomNumber = String.valueOf(counter++);

        Room room = new Room(roomNumber, pricePerNight, capacity, RoomStatus.AVAILABLE, type);

        roomRepository.save(room.getRoomNumber(),  room.getType() , room.getStatus(), room.getCapacity(), room.getPricePerNight());
        return room;
    }

    public List<Room> getAvailableRooms(){
        return  roomRepository.getAvailableRooms();
    }

    public List<Room> getAllRooms(){
        return roomRepository.findAll();
    }


    public void initializeRooms() {

        if (!getAllRooms().isEmpty()) {
            return;
        }

        ajouterRoom(RoomType.SINGLE, 1, new BigDecimal("300"));
        ajouterRoom(RoomType.DOUBLE, 2, new BigDecimal("500"));
        ajouterRoom(RoomType.SUITE, 4, new BigDecimal("1000"));
        ajouterRoom(RoomType.DOUBLE, 2, new BigDecimal("500"));
    }

    public Room getRoomByNumber(String roomNumber) {

        Room room = roomRepository.getByNumber(roomNumber);

        if (room == null) {
            throw new RoomNotFoundException("Room not found for number : " + roomNumber);
        }

        return room;
    }



}
