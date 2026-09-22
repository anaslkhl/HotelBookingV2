package repository;

import model.Reservation;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository {

    void save(Reservation reservation);

    boolean existByUuid(UUID uuid);

    boolean existByReservationCode(String reservationCode);

    Reservation getByUuid(UUID uuid);

    void delete(UUID id);

    List<Reservation> findByUserId(UUID userId);

    List<Reservation> findByRoomNumber(String roomNumber);

    List<Reservation> getAllReservations();

    List<Reservation> getConfirmedReservations();
}
