package repository.jdbc;

import db.DatabaseConnection;
import model.Reservation;
import model.enums.ReservationStatus;
import repository.ReservationRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcReservationRepository implements ReservationRepository {

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void save(Reservation reservation) {

        String sql = """
                INSERT INTO reservations (
                    id, reservation_code, user_id, room_number,
                    check_in, check_out, number_of_guests,
                    nights, total_price, status, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    reservation_code = EXCLUDED.reservation_code,
                    user_id = EXCLUDED.user_id,
                    room_number = EXCLUDED.room_number,
                    check_in = EXCLUDED.check_in,
                    check_out = EXCLUDED.check_out,
                    number_of_guests = EXCLUDED.number_of_guests,
                    nights = EXCLUDED.nights,
                    total_price = EXCLUDED.total_price,
                    status = EXCLUDED.status,
                    created_at = EXCLUDED.created_at
                """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, reservation.getId());
            stmt.setString(2, reservation.getReservationCode());
            stmt.setObject(3, reservation.getUserId());
            stmt.setString(4, reservation.getRoomNumber());
            stmt.setObject(5, reservation.getCheckIn());
            stmt.setObject(6, reservation.getCheckOut());
            stmt.setInt(7, reservation.getNumberOfGuests());
            stmt.setLong(8, reservation.getNumberOfNights());
            stmt.setBigDecimal(9, reservation.getTotalPrice());
            stmt.setString(10, reservation.getStatus().name());
            stmt.setObject(11, reservation.getCreatedAt());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save reservation : " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existByUuid(UUID uuid) {

        String sql = "SELECT 1 FROM reservations WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check reservation by id : " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existByReservationCode(String reservationCode) {

        String sql = "SELECT 1 FROM reservations WHERE reservation_code = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, reservationCode);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check reservation code : " + e.getMessage(), e);
        }
    }

    @Override
    public Reservation getByUuid(UUID uuid) {

        String sql = "SELECT * FROM reservations WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservation by id : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID id) {

        String sql = "DELETE FROM reservations WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete reservation : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> findByUserId(UUID userId) {

        String sql = "SELECT * FROM reservations WHERE user_id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservations by user id : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> findByRoomNumber(String roomNumber) {

        String sql = "SELECT * FROM reservations WHERE room_number = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, roomNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservations by room number : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> getAllReservations() {

        String sql = "SELECT * FROM reservations";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return mapRows(rs);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all reservations : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> getConfirmedReservations() {

        String sql = "SELECT * FROM reservations WHERE status = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, ReservationStatus.CONFIRMED.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get confirmed reservations : " + e.getMessage(), e);
        }
    }

    private List<Reservation> mapRows(ResultSet rs) throws SQLException {

        List<Reservation> reservations = new ArrayList<>();

        while (rs.next()) {
            reservations.add(mapRow(rs));
        }

        return reservations;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {

        Reservation reservation = new Reservation(
                (UUID) rs.getObject("user_id"),
                rs.getString("room_number"),
                rs.getObject("check_in", LocalDate.class),
                rs.getObject("check_out", LocalDate.class),
                rs.getInt("number_of_guests"),
                rs.getLong("nights"),
                rs.getBigDecimal("total_price")
        );

        reservation.setId((UUID) rs.getObject("id"));
        reservation.setReservationCode(rs.getString("reservation_code"));
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        reservation.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));

        return reservation;
    }
}