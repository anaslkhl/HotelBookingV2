package model;
import model.enums.RoomStatus;
import model.enums.RoomType;


import java.math.BigDecimal;

public class Room {


    private String roomNumber;
    private RoomType type;
    private RoomStatus status;
    private int capacity;
    private BigDecimal pricePerNight;


    public Room(String roomNumber, BigDecimal pricePerNight, int capacity, RoomStatus status, RoomType type) {
        this.roomNumber = roomNumber;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.status = status;
        this.type = type;
    }



    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}