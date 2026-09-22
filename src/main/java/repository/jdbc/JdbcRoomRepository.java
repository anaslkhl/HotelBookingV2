package repository.jdbc;

import db.DatabaseConnection;
import model.Room;
import model.enums.RoomStatus;
import model.enums.RoomType;
import repository.RoomRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcRoomRepository implements RoomRepository {


    private Connection getConnection(){
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void save(String roomNumber, RoomType type, RoomStatus status, int capacity, BigDecimal pricePerNight){

        String sql = """
                    INSERT INTO rooms (room_number, room_type, status, capacity, price_per_night)
                    VALUES(?, ?, ?, ?, ?)
                    
                """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, roomNumber);
            stmt.setString(2, type.name());
            stmt.setString(3, status.name());
            stmt.setInt(4, capacity);
            stmt.setBigDecimal(5, pricePerNight);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save room !" + e.getMessage());
        }

    }

    @Override
    public Room getByNumber(String number){

        String sql = "SELECT room_number, room_type, status, capacity, price_per_night FROM rooms WHERE room_number = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)){

            stmt.setString(1, number);
            try (ResultSet res = stmt.executeQuery()){
                return res.next() ? mapRow(res) : null;
            }

        }catch (SQLException e){
            throw new RuntimeException("Failed to find room by room number !" + e.getMessage());
        }

    }


    @Override
    public boolean existByNumber(String number){

        String sql = """
                     SELECT EXISTS(SELECT 1 FROM rooms WHERE room_number = ?)
                     """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)){

            stmt.setString(1, number);
            try (ResultSet res = stmt.executeQuery()){
                res.next();
                return res.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find this room by room number !" + e.getMessage());
        }
    }


    @Override
    public void delete(String number){

        String sql = """
                DELETE FROM rooms WHERE room_number = ?
                """;

        try(PreparedStatement stmt = getConnection().prepareStatement(sql)){

            stmt.setString(1, number);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete room by room number !" + e.getMessage());
        }
    }


    @Override
    public List<Room> getAvailableRooms(){

        String sql = "SELECT * FROM rooms WHERE status = ?";

        try(PreparedStatement stmt = getConnection().prepareStatement(sql)){

            stmt.setString(1, RoomStatus.AVAILABLE.name());
            try(ResultSet res = stmt.executeQuery()){

                List<Room> rooms = new ArrayList<>();
                while (res.next()){

                    rooms.add(mapRow(res));
                }
                return rooms;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all available rooms ! " + e.getMessage());
        }

    }

    @Override
    public List<Room> findAll(){

        String sql = "SELECT * FROM rooms";
        try(PreparedStatement stmt = getConnection().prepareStatement(sql)){

            try(ResultSet res = stmt.executeQuery()) {
                List<Room> rooms = new ArrayList<>();
                while (res.next()) {

                    rooms.add(mapRow(res));
                }
                return rooms;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all rooms !" + e.getMessage());
        }
    }


    private Room mapRow(ResultSet rs) throws SQLException {

        return new Room(
                rs.getString("room_number"),
                rs.getBigDecimal("price_per_night"),
                rs.getInt("capacity"),
                RoomStatus.valueOf(rs.getString("status")),
                RoomType.valueOf(rs.getString("room_type"))
        );

    }
}