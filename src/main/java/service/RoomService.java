//package service;
//
//import model.Room;
//import model.enums.RoomStatus;
//import model.enums.RoomType;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//public class RoomService {
//
//
//
//    private int counter = 100;
//
//    public RoomService(InMemoryRoomRepository roomRepository){
//        this.roomRepository = roomRepository;
//    }
//
//
//    public Room ajouterRoom(RoomType type, int capacity, BigDecimal pricePerNight) {
//
//        String roomNumber = String.valueOf(counter++);
//
//        Room room = new Room(roomNumber, pricePerNight, capacity, RoomStatus.AVAILABLE, type);
//
//        roomRepository.save(room);
//        return room;
//    }
//
//    public List<Room> getAvailableRooms(){
//        return  roomRepository.getAvailableRooms();
//    }
//
//    public List<Room> getAllRooms(){
//        return roomRepository.findAll();
//    }
//
//
//    public void initializeRooms() {
//
//        ajouterRoom(RoomType.SINGLE, 1, new BigDecimal("300"));
//        ajouterRoom(RoomType.DOUBLE, 2, new BigDecimal("500"));
//        ajouterRoom(RoomType.SUITE, 4, new BigDecimal("1000"));
//        ajouterRoom(RoomType.DOUBLE, 2, new BigDecimal("500"));
//    }
//
//
//
//}
