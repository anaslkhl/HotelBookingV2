import db.DatabaseConnection;
import model.Client;
import model.Reservation;
import model.enums.ReservationStatus;
import model.enums.RoomStatus;
import model.enums.RoomType;
import repository.jdbc.JdbcClientRepository;
import repository.jdbc.JdbcReservationRepository;
import repository.jdbc.JdbcRoomRepository;

import java.math.BigDecimal;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class JdbcReservationRepositoryTest {

    private static final JdbcReservationRepository repo = new JdbcReservationRepository();
    private static final JdbcClientRepository clientRepo = new JdbcClientRepository();
    private static final JdbcRoomRepository roomRepo = new JdbcRoomRepository();

    private static final UUID TEST_CLIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static void cleanup() throws Exception {
        Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
        stmt.execute("DELETE FROM reservations WHERE user_id = '" + TEST_CLIENT_ID + "'");
        stmt.execute("DELETE FROM clients WHERE id = '" + TEST_CLIENT_ID + "'");
        stmt.execute("DELETE FROM rooms WHERE room_number = 'TEST-RES'");
    }

    private static void seed() {
        Client client = new Client("Test Client", "test.res@example.com", "0612345678", "secret7");
        client.setUuid(TEST_CLIENT_ID);
        clientRepo.save(client);
        roomRepo.save("TEST-RES", RoomType.DOUBLE, RoomStatus.AVAILABLE, 2, new BigDecimal("125.00"));
    }

    private static Reservation newReservation() {
        Reservation reservation = new Reservation(
                TEST_CLIENT_ID,
                "TEST-RES",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 4),
                2,
                3,
                new BigDecimal("375.00")
        );
        reservation.setId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        return reservation;
    }

    public static void main(String[] args) throws Exception {
        cleanup();

        int[] tally = {0, 0};
        tally[run("save() creates reservation", testSave())]++;
        tally[run("existByUuid() finds saved reservation", testExistByUuid())]++;
        tally[run("existByReservationCode() finds code", testExistByCode())]++;
        tally[run("getByUuid() reads back fields", testGetByUuid())]++;
        tally[run("findByUserId() filters", testFindByUserId())]++;
        tally[run("findByRoomNumber() filters", testFindByRoomNumber())]++;
        tally[run("getAllReservations() lists", testGetAll())]++;
        tally[run("getConfirmedReservations() filters", testGetConfirmed())]++;
        tally[run("delete() removes reservation", testDelete())]++;
        tally[run("save() upserts existing reservation", testSaveUpdatesExisting())]++;
        tally[run("getByUuid() returns null for unknown id", testGetByUuidUnknown())]++;
        tally[run("findByUserId() returns only that user's rows", testFindByUserIdIsolation())]++;
        tally[run("findByRoomNumber() returns only that room's rows", testFindByRoomNumberIsolation())]++;
        tally[run("getConfirmedReservations() excludes cancelled", testGetConfirmedExcludesCancelled())]++;

        System.out.println("=====================");
        System.out.println("PASSED: " + tally[1] + "  FAILED: " + tally[0]);

        cleanup();
    }

    private static int run(String name, boolean ok) {
        System.out.println((ok ? "[PASS] " : "[FAIL] ") + name);
        return ok ? 1 : 0;
    }

    private static boolean testSave() {
        try {
            seed();
            repo.save(newReservation());
            boolean saved = repo.existByUuid(newReservation().getId());
            cleanup();
            return saved;
        } catch (Exception e) {
            System.out.println("   save threw: " + e);
            return false;
        }
    }

    private static boolean testExistByUuid() {
        try {
            seed();
            Reservation reservation = newReservation();
            repo.save(reservation);
            boolean yes = repo.existByUuid(reservation.getId());
            boolean no = repo.existByUuid(UUID.randomUUID());
            System.out.println("   exist(id)=" + yes + " exist(random)=" + no);
            cleanup();
            return yes && !no;
        } catch (Exception e) {
            System.out.println("   existByUuid threw: " + e);
            return false;
        }
    }

    private static boolean testExistByCode() {
        try {
            seed();
            Reservation reservation = newReservation();
            repo.save(reservation);
            boolean yes = repo.existByReservationCode(reservation.getReservationCode());
            boolean no = repo.existByReservationCode("RES-DOES-NOT-EXIST");
            System.out.println("   code=" + reservation.getReservationCode() + " yes=" + yes + " no=" + no);
            cleanup();
            return yes && !no;
        } catch (Exception e) {
            System.out.println("   existByCode threw: " + e);
            return false;
        }
    }

    private static boolean testGetByUuid() {
        try {
            seed();
            Reservation reservation = newReservation();
            repo.save(reservation);
            Reservation got = repo.getByUuid(reservation.getId());
            boolean ok = got != null
                    && reservation.getId().equals(got.getId())
                    && reservation.getReservationCode().equals(got.getReservationCode())
                    && reservation.getUserId().equals(got.getUserId())
                    && "TEST-RES".equals(got.getRoomNumber())
                    && reservation.getCheckIn().equals(got.getCheckIn())
                    && reservation.getCheckOut().equals(got.getCheckOut())
                    && got.getNumberOfGuests() == 2
                    && got.getNumberOfNights() == 3
                    && got.getStatus() == ReservationStatus.CONFIRMED
                    && got.getTotalPrice().compareTo(new BigDecimal("375.00")) == 0;
            if (got == null) System.out.println("   getByUuid returned null");
            else System.out.println("   got=" + got.getId() + " / " + got.getRoomNumber()
                    + " / " + got.getStatus() + " / price=" + got.getTotalPrice());
            cleanup();
            return ok;
        } catch (Exception e) {
            System.out.println("   getByUuid threw: " + e);
            return false;
        }
    }

    private static boolean testFindByUserId() {
        try {
            seed();
            repo.save(newReservation());
            List<Reservation> list = repo.findByUserId(TEST_CLIENT_ID);
            boolean found = list.stream().anyMatch(r -> "TEST-RES".equals(r.getRoomNumber()));
            System.out.println("   reservations for client: " + list.size());
            cleanup();
            return found;
        } catch (Exception e) {
            System.out.println("   findByUserId threw: " + e);
            return false;
        }
    }

    private static boolean testFindByRoomNumber() {
        try {
            seed();
            repo.save(newReservation());
            List<Reservation> list = repo.findByRoomNumber("TEST-RES");
            boolean found = list.stream().anyMatch(r -> r.getUserId().equals(TEST_CLIENT_ID));
            System.out.println("   reservations for room: " + list.size());
            cleanup();
            return found;
        } catch (Exception e) {
            System.out.println("   findByRoomNumber threw: " + e);
            return false;
        }
    }

    private static boolean testGetAll() {
        try {
            seed();
            repo.save(newReservation());
            List<Reservation> all = repo.getAllReservations();
            boolean found = all.stream().anyMatch(r -> TEST_CLIENT_ID.equals(r.getUserId()));
            System.out.println("   total reservations in DB: " + all.size());
            cleanup();
            return found;
        } catch (Exception e) {
            System.out.println("   getAllReservations threw: " + e);
            return false;
        }
    }

    private static boolean testGetConfirmed() {
        try {
            seed();
            Reservation reservation = newReservation();
            repo.save(reservation);
            List<Reservation> confirmed = repo.getConfirmedReservations();
            boolean found = confirmed.stream().anyMatch(r -> r.getId().equals(reservation.getId()));
            cleanup();
            return found;
        } catch (Exception e) {
            System.out.println("   getConfirmedReservations threw: " + e);
            return false;
        }
    }

    private static boolean testDelete() {
        try {
            seed();
            Reservation reservation = newReservation();
            repo.save(reservation);
            repo.delete(reservation.getId());
            boolean gone = !repo.existByUuid(reservation.getId());
            cleanup();
            return gone;
        } catch (Exception e) {
            System.out.println("   delete threw: " + e);
            return false;
        }
    }

    private static boolean testSaveUpdatesExisting() {
        try {
            seed();
            Reservation reservation = newReservation();
            repo.save(reservation);

            reservation.setCheckIn(LocalDate.of(2026, 12, 1));
            reservation.setCheckOut(LocalDate.of(2026, 12, 5));
            reservation.setNumberOfNights(4);
            reservation.setTotalPrice(new BigDecimal("500.00"));
            reservation.setStatus(ReservationStatus.CANCELLED);
            repo.save(reservation);

            Reservation reloaded = repo.getByUuid(reservation.getId());
            boolean ok = reloaded != null
                    && LocalDate.of(2026, 12, 1).equals(reloaded.getCheckIn())
                    && LocalDate.of(2026, 12, 5).equals(reloaded.getCheckOut())
                    && reloaded.getNumberOfNights() == 4
                    && reloaded.getStatus() == ReservationStatus.CANCELLED
                    && reloaded.getTotalPrice().compareTo(new BigDecimal("500.00")) == 0;
            if (reloaded == null) System.out.println("   reloaded null");
            else System.out.println("   updated=" + reloaded.getCheckIn() + " -> " + reloaded.getCheckOut()
                    + " / " + reloaded.getStatus() + " / nights=" + reloaded.getNumberOfNights());
            cleanup();
            return ok;
        } catch (Exception e) {
            System.out.println("   save-upsert threw: " + e);
            return false;
        }
    }

    private static boolean testGetByUuidUnknown() {
        try {
            seed();
            Reservation got = repo.getByUuid(UUID.fromString("00000000-0000-0000-0000-0000000000dd"));
            System.out.println("   unknown id returns " + got);
            cleanup();
            return got == null;
        } catch (Exception e) {
            System.out.println("   getByUuid-unknown threw: " + e);
            return false;
        }
    }

    private static boolean testFindByUserIdIsolation() {
        try {
            seed();
            repo.save(newReservation());
            List<Reservation> list = repo.findByUserId(TEST_CLIENT_ID);
            boolean onlyOwn = list.stream().allMatch(r -> TEST_CLIENT_ID.equals(r.getUserId()));
            boolean found = list.stream().anyMatch(r -> "TEST-RES".equals(r.getRoomNumber()));
            System.out.println("   rows for client: " + list.size() + " allOwn=" + onlyOwn);
            cleanup();
            return onlyOwn && found;
        } catch (Exception e) {
            System.out.println("   findByUserId-isolation threw: " + e);
            return false;
        }
    }

    private static boolean testFindByRoomNumberIsolation() {
        try {
            seed();
            repo.save(newReservation());
            List<Reservation> list = repo.findByRoomNumber("TEST-RES");
            boolean onlyRoom = list.stream().allMatch(r -> "TEST-RES".equals(r.getRoomNumber()));
            boolean found = list.stream().anyMatch(r -> TEST_CLIENT_ID.equals(r.getUserId()));
            System.out.println("   rows for room: " + list.size() + " allRoom=" + onlyRoom);
            cleanup();
            return onlyRoom && found;
        } catch (Exception e) {
            System.out.println("   findByRoomNumber-isolation threw: " + e);
            return false;
        }
    }

    private static boolean testGetConfirmedExcludesCancelled() {
        try {
            seed();
            Reservation reservation = newReservation();
            reservation.setStatus(ReservationStatus.CANCELLED);
            repo.save(reservation);

            List<Reservation> confirmed = repo.getConfirmedReservations();
            boolean excluded = confirmed.stream().noneMatch(r -> r.getId().equals(reservation.getId()));
            System.out.println("   cancelled row present in confirmed list: " + !excluded);
            cleanup();
            return excluded;
        } catch (Exception e) {
            System.out.println("   getConfirmed-excludes threw: " + e);
            return false;
        }
    }
}