package repository;

import model.Room;
import model.enums.RoomStatus;
import model.enums.RoomType;

import java.math.BigDecimal;
import java.util.List;

public interface RoomRepository {


    void save(String roomNumber, RoomType type, RoomStatus status, int capacity, BigDecimal pricePerNight);

    boolean existByNumber(String number);

    Room getByNumber(String number);

    void delete(String number);

    List<Room> getAvailableRooms();

    List<Room> findAll();
}
