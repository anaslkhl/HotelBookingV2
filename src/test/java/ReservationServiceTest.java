import db.DatabaseConnection;
import exception.ClientNotFoundException;
import exception.InvalidReservationInfoException;
import exception.ReservationNotFoundException;
import exception.RoomCapacityException;
import exception.RoomNotAvailableException;
import exception.RoomNotFoundException;
import model.Client;
import model.Reservation;
import model.Room;
import model.enums.ReservationStatus;
import model.enums.RoomStatus;
import model.enums.RoomType;
import repository.jdbc.JdbcClientRepository;
import repository.jdbc.JdbcReservationRepository;
import repository.jdbc.JdbcRoomRepository;
import service.ReservationService;

import java.math.BigDecimal;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ReservationServiceTest {

    private static final JdbcReservationRepository reservationRepo = new JdbcReservationRepository();
    private static final JdbcClientRepository clientRepo = new JdbcClientRepository();
    private static final JdbcRoomRepository roomRepo = new JdbcRoomRepository();
    private static final ReservationService service =
            new ReservationService(reservationRepo, clientRepo, roomRepo);

    private static final UUID CLIENT_A = UUID.fromString("00000000-0000-0000-0000-0000000000a0");
    private static final UUID CLIENT_B = UUID.fromString("00000000-0000-0000-0000-0000000000b0");

    private static final LocalDate D1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate D3 = LocalDate.of(2026, 11, 4);
    private static final LocalDate D4 = LocalDate.of(2026, 11, 5);
    private static final LocalDate D6 = LocalDate.of(2026, 11, 8);

    private static void cleanup() throws Exception {
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement()) {
            stmt.execute("DELETE FROM reservations WHERE user_id IN ('" + CLIENT_A + "', '" + CLIENT_B
                    + "') OR room_number LIKE 'SRV-%'");
            stmt.execute("DELETE FROM clients WHERE id IN ('" + CLIENT_A + "', '" + CLIENT_B + "')");
            stmt.execute("DELETE FROM rooms WHERE room_number LIKE 'SRV-%'");
        }
    }

    private static void seed() throws Exception {
        Client a = new Client("Service Client A", "svc.a@example.com", "0611111111", "secret7");
        a.setUuid(CLIENT_A);
        clientRepo.save(a);

        Client b = new Client("Service Client B", "svc.b@example.com", "0622222222", "secret8");
        b.setUuid(CLIENT_B);
        clientRepo.save(b);

        roomRepo.save("SRV-A", RoomType.DOUBLE, RoomStatus.AVAILABLE, 4, new BigDecimal("100.00"));
        roomRepo.save("SRV-B", RoomType.SINGLE, RoomStatus.AVAILABLE, 2, new BigDecimal("80.00"));
        roomRepo.save("SRV-SMALL", RoomType.SINGLE, RoomStatus.AVAILABLE, 1, new BigDecimal("50.00"));
    }

    private static Reservation saveReservation(UUID userId, String roomNumber, LocalDate checkIn,
                                               LocalDate checkOut, int guests, ReservationStatus status) {
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        Reservation reservation = new Reservation(userId, roomNumber, checkIn, checkOut,
                guests, nights, new BigDecimal("0.00"));
        reservation.setStatus(status);
        reservationRepo.save(reservation);
        return reservation;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface Check {
        void run() throws Exception;
    }

    private static boolean expectThrows(Class<? extends Throwable> type, Check action) {
        try {
            action.run();
            return false;
        } catch (Throwable t) {
            if (!type.isInstance(t)) {
                System.out.println("   expected " + type.getSimpleName() + " but got " + t);
                return false;
            }
            return true;
        }
    }

    private static boolean exec(Check test) {
        try {
            cleanup();
            seed();
            test.run();
            return true;
        } catch (Throwable t) {
            System.out.println("   " + t);
            return false;
        } finally {
            try {
                cleanup();
            } catch (Exception e) {
                System.out.println("   cleanup failed: " + e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        cleanup();

        int[] tally = {0, 0};

        tally[run("create: happy path computes nights, price, persists, room to MAINTENANCE",
                exec(ReservationServiceTest::testCreateHappy))]++;
        tally[run("create: null user throws ClientNotFoundException",
                exec(ReservationServiceTest::testCreateNullUser))]++;
        tally[run("create: null room throws RoomNotFoundException",
                exec(ReservationServiceTest::testCreateNullRoom))]++;
        tally[run("create: unknown client throws ClientNotFoundException",
                exec(ReservationServiceTest::testCreateUnknownClient))]++;
        tally[run("create: unknown room throws RoomNotFoundException",
                exec(ReservationServiceTest::testCreateUnknownRoom))]++;
        tally[run("create: exceeding capacity throws RoomCapacityException",
                exec(ReservationServiceTest::testCreateCapacityExceeded))]++;
        tally[run("create: check-out not after check-in throws InvalidReservationInfoException",
                exec(ReservationServiceTest::testCreateInvalidDateRange))]++;
        tally[run("create: overlapping dates throw RoomNotAvailableException",
                exec(ReservationServiceTest::testCreateOverlap))]++;
        tally[run("create: null dates throw InvalidReservationInfoException",
                exec(ReservationServiceTest::testCreateNullDates))]++;
        tally[run("create: non-positive guests throw InvalidReservationInfoException",
                exec(ReservationServiceTest::testCreateInvalidGuests))]++;
        tally[run("create: back-to-back and gapped bookings allowed on same room",
                exec(ReservationServiceTest::testCreateNonOverlapping))]++;
        tally[run("create: cancelled reservation does not block new booking",
                exec(ReservationServiceTest::testCreateAfterCancelled))]++;
        tally[run("create: second booking counts existing confirmed reservations correctly",
                exec(ReservationServiceTest::testCreateNightsAndPrice))]++;
        tally[run("find: findByUserId returns only that user's reservations",
                exec(ReservationServiceTest::testFindByUserIdIsolation))]++;
        tally[run("find: findByUserId returns empty list for unknown user",
                exec(ReservationServiceTest::testFindByUserIdUnknown))]++;
        tally[run("find: findByUserId includes cancelled reservations",
                exec(ReservationServiceTest::testFindByUserIdIncludesCancelled))]++;
        tally[run("find: getReservationShortId matches full and prefix id",
                exec(ReservationServiceTest::testGetReservationShortId))]++;
        tally[run("find: getReservationShortId returns null when no match",
                exec(ReservationServiceTest::testGetReservationShortIdNoMatch))]++;
        tally[run("find: afficherReservation throws on empty list",
                exec(ReservationServiceTest::testAfficherEmptyThrows))]++;
        tally[run("find: afficherReservation prints non-empty list without error",
                exec(ReservationServiceTest::testAfficherList))]++;
        tally[run("update: happy path updates dates, nights, price and persists",
                exec(ReservationServiceTest::testUpdateHappyPath))]++;
        tally[run("update: moving to another room frees old room and takes new one",
                exec(ReservationServiceTest::testUpdateChangeRoom))]++;
        tally[run("update: unknown reservation throws ReservationNotFoundException",
                exec(ReservationServiceTest::testUpdateUnknownReservation))]++;
        tally[run("update: non-owner is rejected and nothing changes",
                exec(ReservationServiceTest::testUpdateNotOwner))]++;
        tally[run("update: cancelled reservation cannot be updated",
                exec(ReservationServiceTest::testUpdateCancelled))]++;
        tally[run("update: null date throws InvalidReservationInfoException",
                exec(ReservationServiceTest::testUpdateNullDate))]++;
        tally[run("update: unknown room throws RoomNotFoundException",
                exec(ReservationServiceTest::testUpdateUnknownRoom))]++;
        tally[run("update: exceeding new room capacity throws RoomCapacityException",
                exec(ReservationServiceTest::testUpdateCapacityExceeded))]++;
        tally[run("update: room not AVAILABLE throws RoomNotAvailableException",
                exec(ReservationServiceTest::testUpdateRoomNotAvailable))]++;
        tally[run("update: overlapping other reservation is rejected",
                exec(ReservationServiceTest::testUpdateOverlap))]++;
        tally[run("cancel: happy path cancels reservation and frees room",
                exec(ReservationServiceTest::testCancelHappyPath))]++;
        tally[run("cancel: cancelled reservation disappears from confirmed list",
                exec(ReservationServiceTest::testCancelRemovesFromConfirmed))]++;
        tally[run("cancel: unknown reservation throws ReservationNotFoundException",
                exec(ReservationServiceTest::testCancelUnknown))]++;
        tally[run("cancel: non-owner is rejected and reservation stays confirmed",
                exec(ReservationServiceTest::testCancelNotOwner))]++;
        tally[run("cancel: already cancelled reservation cannot be cancelled again",
                exec(ReservationServiceTest::testCancelAlreadyCancelled))]++;
        tally[run("cancel: cancelled reservation still readable via findByUserId",
                exec(ReservationServiceTest::testCancelStillFindable))]++;
        tally[run("crud: create then read then update then cancel full lifecycle",
                exec(ReservationServiceTest::testFullLifecycle))]++;

        System.out.println("=====================");
        System.out.println("PASSED: " + tally[1] + "  FAILED: " + tally[0]);

        cleanup();
    }

    private static int run(String name, boolean ok) {
        System.out.println((ok ? "[PASS] " : "[FAIL] ") + name);
        return ok ? 1 : 0;
    }

    private static void testCreateHappy() throws Exception {
        Reservation reservation = service.ajouterReservation(
                CLIENT_A, "SRV-A", D1, D3, 2);

        check(reservation != null, "ajouterReservation returned null");
        check(reservation.getNumberOfNights() == 3,
                "nights expected 3 got " + reservation.getNumberOfNights());
        check(reservation.getTotalPrice().compareTo(new BigDecimal("300.00")) == 0,
                "price expected 300.00 got " + reservation.getTotalPrice());
        check(reservation.getStatus() == ReservationStatus.CONFIRMED,
                "status expected CONFIRMED got " + reservation.getStatus());
        check(reservation.getUserId().equals(CLIENT_A), "userId mismatch");
        check("SRV-A".equals(reservation.getRoomNumber()), "roomNumber mismatch");
        check(reservationRepo.existByUuid(reservation.getId()), "reservation not persisted");

        Room room = roomRepo.getByNumber("SRV-A");
        check(room != null && room.getStatus() == RoomStatus.MAINTENANCE,
                "room expected MAINTENANCE got " + (room == null ? null : room.getStatus()));
    }

    private static void testCreateNullUser() {
        check(expectThrows(ClientNotFoundException.class,
                () -> service.ajouterReservation(null, "SRV-A", D1, D3, 2)),
                "expected ClientNotFoundException for null user");
    }

    private static void testCreateNullRoom() {
        check(expectThrows(RoomNotFoundException.class,
                () -> service.ajouterReservation(CLIENT_A, null, D1, D3, 2)),
                "expected RoomNotFoundException for null room");
    }

    private static void testCreateUnknownClient() {
        check(expectThrows(ClientNotFoundException.class,
                () -> service.ajouterReservation(UUID.randomUUID(), "SRV-A", D1, D3, 2)),
                "expected ClientNotFoundException for unknown client");
    }

    private static void testCreateUnknownRoom() {
        check(expectThrows(RoomNotFoundException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-NOPE", D1, D3, 2)),
                "expected RoomNotFoundException for unknown room");
    }

    private static void testCreateCapacityExceeded() {
        check(expectThrows(RoomCapacityException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-SMALL", D1, D3, 2)),
                "expected RoomCapacityException when guests > capacity");
    }

    private static void testCreateInvalidDateRange() {
        check(expectThrows(InvalidReservationInfoException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", D1, D1, 2)),
                "expected exception when check-out equals check-in");
        check(expectThrows(InvalidReservationInfoException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", D3, D1, 2)),
                "expected exception when check-out before check-in");
    }

    private static void testCreateOverlap() {
        saveReservation(CLIENT_B, "SRV-A", D1, D3, 2, ReservationStatus.CONFIRMED);

        check(expectThrows(RoomNotAvailableException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", LocalDate.of(2026, 11, 3),
                        LocalDate.of(2026, 11, 6), 2)),
                "expected exception when new range starts inside existing");
        check(expectThrows(RoomNotAvailableException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", LocalDate.of(2026, 11, 2),
                        LocalDate.of(2026, 11, 3), 2)),
                "expected exception when new range inside existing");
        check(expectThrows(RoomNotAvailableException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", LocalDate.of(2026, 10, 30),
                        LocalDate.of(2026, 11, 2), 2)),
                "expected exception when new range ends inside existing");
    }

    private static void testCreateNullDates() {
        check(expectThrows(InvalidReservationInfoException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", null, D3, 2)),
                "expected exception for null check-in");
        check(expectThrows(InvalidReservationInfoException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", D1, null, 2)),
                "expected exception for null check-out");
    }

    private static void testCreateInvalidGuests() {
        check(expectThrows(InvalidReservationInfoException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", D1, D3, 0)),
                "expected exception for 0 guests");
        check(expectThrows(InvalidReservationInfoException.class,
                () -> service.ajouterReservation(CLIENT_A, "SRV-A", D1, D3, -1)),
                "expected exception for negative guests");
    }

    private static void testCreateNonOverlapping() {
        saveReservation(CLIENT_B, "SRV-A", D1, D3, 2, ReservationStatus.CONFIRMED);

        Reservation backToBack = service.ajouterReservation(CLIENT_A, "SRV-A", D3, D6, 2);
        check(backToBack != null, "back-to-back booking should be allowed");
        check(backToBack.getNumberOfNights() == 4,
                "back-to-back nights expected 4 got " + backToBack.getNumberOfNights());
        check(backToBack.getTotalPrice().compareTo(new BigDecimal("400.00")) == 0,
                "back-to-back price expected 400.00 got " + backToBack.getTotalPrice());

        Reservation gapped = service.ajouterReservation(CLIENT_A, "SRV-A",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5), 2);
        check(gapped != null, "gapped booking should be allowed");
        check(gapped.getNumberOfNights() == 4,
                "gapped nights expected 4 got " + gapped.getNumberOfNights());
        check(gapped.getTotalPrice().compareTo(new BigDecimal("400.00")) == 0,
                "gapped price expected 400.00 got " + gapped.getTotalPrice());
    }

    private static void testCreateAfterCancelled() {
        saveReservation(CLIENT_B, "SRV-A", D1, D3, 2, ReservationStatus.CANCELLED);

        Reservation reservation = service.ajouterReservation(CLIENT_A, "SRV-A", D1, D3, 2);
        check(reservation != null, "cancelled reservation must not block new booking");
        check(reservationRepo.existByUuid(reservation.getId()), "new booking not persisted");
    }

    private static void testCreateNightsAndPrice() throws Exception {
        Room room = roomRepo.getByNumber("SRV-B");
        check(room != null, "SRV-B missing");

        Reservation reservation = service.ajouterReservation(CLIENT_A, "SRV-B",
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 14), 2);
        check(reservation != null, "ajouterReservation returned null");
        check(reservation.getNumberOfNights() == 4,
                "nights expected 4 got " + reservation.getNumberOfNights());

        BigDecimal expected = room.getPricePerNight().multiply(BigDecimal.valueOf(4));
        check(reservation.getTotalPrice().compareTo(expected) == 0,
                "price expected " + expected + " got " + reservation.getTotalPrice());

        Reservation persisted = reservationRepo.getByUuid(reservation.getId());
        check(persisted != null
                        && persisted.getTotalPrice().compareTo(expected) == 0
                        && persisted.getNumberOfNights() == 4,
                "persisted nights/price mismatch");
    }

    private static void testFindByUserIdIsolation() {
        Reservation own = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2, ReservationStatus.CONFIRMED);
        saveReservation(CLIENT_B, "SRV-B", D4, D6, 1, ReservationStatus.CONFIRMED);

        List<Reservation> list = service.findByUserId(CLIENT_A);
        check(!list.isEmpty(), "expected at least one reservation for CLIENT_A");
        check(list.stream().allMatch(r -> CLIENT_A.equals(r.getUserId())),
                "list leaked reservations of other users");
        check(list.stream().anyMatch(r -> r.getId().equals(own.getId())),
                "own reservation missing from result");
    }

    private static void testFindByUserIdUnknown() {
        List<Reservation> list = service.findByUserId(UUID.randomUUID());
        check(list != null && list.isEmpty(),
                "expected empty list for unknown user, got "
                        + (list == null ? "null" : list.size()));
    }

    private static void testFindByUserIdIncludesCancelled() {
        Reservation cancelled = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CANCELLED);

        List<Reservation> list = service.findByUserId(CLIENT_A);
        check(list.stream().anyMatch(r -> r.getId().equals(cancelled.getId())
                        && r.getStatus() == ReservationStatus.CANCELLED),
                "cancelled reservation missing from findByUserId result");
    }

    private static void testGetReservationShortId() {
        Reservation reservation = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);
        String fullId = reservation.getId().toString();

        Reservation byFull = service.getReservationShortId(fullId);
        check(byFull != null && byFull.getId().equals(reservation.getId()),
                "full id lookup failed");

        Reservation byPrefix = service.getReservationShortId(fullId.substring(0, 8));
        check(byPrefix != null && byPrefix.getId().equals(reservation.getId()),
                "prefix id lookup failed");
    }

    private static void testGetReservationShortIdNoMatch() {
        saveReservation(CLIENT_A, "SRV-A", D1, D3, 2, ReservationStatus.CONFIRMED);

        Reservation result = service.getReservationShortId("zzzz-zzzz");
        check(result == null, "expected null for non-matching id, got " + result);
    }

    private static void testAfficherEmptyThrows() {
        check(expectThrows(ReservationNotFoundException.class,
                () -> service.afficherReservation(List.of())),
                "expected ReservationNotFoundException for empty list");
    }

    private static void testAfficherList() {
        Reservation reservation = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);
        service.afficherReservation(List.of(reservation));
    }

    private static void testUpdateHappyPath() throws Exception {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        LocalDate newIn = LocalDate.of(2026, 11, 10);
        LocalDate newOut = LocalDate.of(2026, 11, 13);

        Reservation updated = service.reservationUpdate(
                existing.getId(), CLIENT_A, newIn, newOut, "SRV-A");

        check(updated != null, "reservationUpdate returned null");
        check(updated.getCheckIn().equals(newIn),
                "checkIn expected " + newIn + " got " + updated.getCheckIn());
        check(updated.getCheckOut().equals(newOut),
                "checkOut expected " + newOut + " got " + updated.getCheckOut());
        check(updated.getNumberOfNights() == 3,
                "nights expected 3 got " + updated.getNumberOfNights());
        check(updated.getTotalPrice().compareTo(new BigDecimal("300.00")) == 0,
                "price expected 300.00 got " + updated.getTotalPrice());

        Reservation persisted = reservationRepo.getByUuid(existing.getId());
        check(persisted != null && persisted.getCheckIn().equals(newIn)
                        && persisted.getCheckOut().equals(newOut),
                "update not persisted");

        Room room = roomRepo.getByNumber("SRV-A");
        check(room != null && room.getStatus() == RoomStatus.AVAILABLE,
                "same-room update must keep room AVAILABLE, got "
                        + (room == null ? null : room.getStatus()));
    }

    private static void testUpdateChangeRoom() throws Exception {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1,
                LocalDate.of(2026, 11, 3), 2, ReservationStatus.CONFIRMED);

        Reservation updated = service.reservationUpdate(
                existing.getId(), CLIENT_A, D1, LocalDate.of(2026, 11, 3), "SRV-B");

        check(updated != null, "reservationUpdate returned null");
        check("SRV-B".equals(updated.getRoomNumber()),
                "roomNumber expected SRV-B got " + updated.getRoomNumber());
        check(updated.getTotalPrice().compareTo(new BigDecimal("160.00")) == 0,
                "price expected 160.00 got " + updated.getTotalPrice());

        Reservation persisted = reservationRepo.getByUuid(existing.getId());
        check(persisted != null && "SRV-B".equals(persisted.getRoomNumber()),
                "room change not persisted");

        Room oldRoom = roomRepo.getByNumber("SRV-A");
        Room newRoom = roomRepo.getByNumber("SRV-B");
        check(oldRoom != null && oldRoom.getStatus() == RoomStatus.AVAILABLE,
                "old room expected AVAILABLE got "
                        + (oldRoom == null ? null : oldRoom.getStatus()));
        check(newRoom != null && newRoom.getStatus() == RoomStatus.MAINTENANCE,
                "new room expected MAINTENANCE got "
                        + (newRoom == null ? null : newRoom.getStatus()));
    }

    private static void testUpdateUnknownReservation() {
        check(expectThrows(ReservationNotFoundException.class,
                () -> service.reservationUpdate(UUID.randomUUID(), CLIENT_A, D1, D3, "SRV-A")),
                "expected ReservationNotFoundException for unknown id");
    }

    private static void testUpdateNotOwner() throws Exception {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        Reservation result = service.reservationUpdate(
                existing.getId(), CLIENT_B, LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 4), "SRV-A");

        check(result == null, "non-owner update should return null");

        Reservation persisted = reservationRepo.getByUuid(existing.getId());
        check(persisted != null && persisted.getCheckIn().equals(D1),
                "reservation was modified by non-owner");
    }

    private static void testUpdateCancelled() {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CANCELLED);

        Reservation result = service.reservationUpdate(
                existing.getId(), CLIENT_A, LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 4), "SRV-A");

        check(result == null, "cancelled reservation update should return null");
    }

    private static void testUpdateNullDate() {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        check(expectThrows(InvalidReservationInfoException.class,
                () -> service.reservationUpdate(existing.getId(), CLIENT_A, null, D3, "SRV-A")),
                "expected exception for null check-in");
        check(expectThrows(InvalidReservationInfoException.class,
                () -> service.reservationUpdate(existing.getId(), CLIENT_A, D1, null, "SRV-A")),
                "expected exception for null check-out");
    }

    private static void testUpdateUnknownRoom() {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        check(expectThrows(RoomNotFoundException.class,
                () -> service.reservationUpdate(existing.getId(), CLIENT_A, D1, D3, "SRV-NOPE")),
                "expected RoomNotFoundException for unknown room");
    }

    private static void testUpdateCapacityExceeded() {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        check(expectThrows(RoomCapacityException.class,
                () -> service.reservationUpdate(existing.getId(), CLIENT_A, D1, D3, "SRV-SMALL")),
                "expected RoomCapacityException when moving to smaller room");
    }

    private static void testUpdateRoomNotAvailable() throws Exception {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);
        roomRepo.save("SRV-A", RoomType.DOUBLE, RoomStatus.MAINTENANCE, 4,
                new BigDecimal("100.00"));

        check(expectThrows(RoomNotAvailableException.class,
                () -> service.reservationUpdate(existing.getId(), CLIENT_A, D1, D3, "SRV-A")),
                "expected RoomNotAvailableException for MAINTENANCE room");
    }

    private static void testUpdateOverlap() throws Exception {
        Reservation own = saveReservation(CLIENT_A, "SRV-B", D1, D3, 2,
                ReservationStatus.CONFIRMED);
        saveReservation(CLIENT_B, "SRV-B", LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 12), 1, ReservationStatus.CONFIRMED);

        LocalDate newIn = LocalDate.of(2026, 12, 9);
        LocalDate newOut = LocalDate.of(2026, 12, 11);

        Reservation result = service.reservationUpdate(own.getId(), CLIENT_A, newIn, newOut, "SRV-B");
        check(result == null, "overlapping update should return null");

        Reservation persisted = reservationRepo.getByUuid(own.getId());
        check(persisted != null && persisted.getCheckIn().equals(D1),
                "dates changed despite overlap rejection");
    }

    private static void testCancelHappyPath() throws Exception {
        roomRepo.save("SRV-A", RoomType.DOUBLE, RoomStatus.MAINTENANCE, 4,
                new BigDecimal("100.00"));
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        Reservation cancelled = service.reservationCancelation(existing.getId(), CLIENT_A);

        check(cancelled != null, "reservationCancelation returned null");
        check(cancelled.getStatus() == ReservationStatus.CANCELLED,
                "status expected CANCELLED got " + cancelled.getStatus());

        Reservation persisted = reservationRepo.getByUuid(existing.getId());
        check(persisted != null && persisted.getStatus() == ReservationStatus.CANCELLED,
                "cancellation not persisted");

        Room room = roomRepo.getByNumber("SRV-A");
        check(room != null && room.getStatus() == RoomStatus.AVAILABLE,
                "room expected AVAILABLE after cancel, got "
                        + (room == null ? null : room.getStatus()));
    }

    private static void testCancelRemovesFromConfirmed() {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        service.reservationCancelation(existing.getId(), CLIENT_A);

        boolean stillConfirmed = reservationRepo.getConfirmedReservations().stream()
                .anyMatch(r -> r.getId().equals(existing.getId()));
        check(!stillConfirmed, "cancelled reservation still in confirmed list");
    }

    private static void testCancelUnknown() {
        check(expectThrows(ReservationNotFoundException.class,
                () -> service.reservationCancelation(UUID.randomUUID(), CLIENT_A)),
                "expected ReservationNotFoundException for unknown id");
    }

    private static void testCancelNotOwner() throws Exception {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        Reservation result = service.reservationCancelation(existing.getId(), CLIENT_B);
        check(result == null, "non-owner cancel should return null");

        Reservation persisted = reservationRepo.getByUuid(existing.getId());
        check(persisted != null && persisted.getStatus() == ReservationStatus.CONFIRMED,
                "reservation status changed by non-owner");
    }

    private static void testCancelAlreadyCancelled() {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CANCELLED);

        Reservation result = service.reservationCancelation(existing.getId(), CLIENT_A);
        check(result == null, "second cancel should return null");
    }

    private static void testCancelStillFindable() {
        Reservation existing = saveReservation(CLIENT_A, "SRV-A", D1, D3, 2,
                ReservationStatus.CONFIRMED);

        service.reservationCancelation(existing.getId(), CLIENT_A);

        List<Reservation> list = service.findByUserId(CLIENT_A);
        check(list.stream().anyMatch(r -> r.getId().equals(existing.getId())
                        && r.getStatus() == ReservationStatus.CANCELLED),
                "cancelled reservation not readable via findByUserId");
    }

    private static void testFullLifecycle() throws Exception {
        Reservation created = service.ajouterReservation(CLIENT_A, "SRV-A", D1, D3, 2);
        check(created != null, "create failed");
        check(reservationRepo.existByUuid(created.getId()), "created reservation not found");

        Reservation read = service.getReservationShortId(created.getId().toString());
        check(read != null && read.getId().equals(created.getId()),
                "read after create failed");

        service.afficherReservation(List.of(read));

        LocalDate newIn = LocalDate.of(2026, 12, 20);
        LocalDate newOut = LocalDate.of(2026, 12, 23);
        Reservation updated = service.reservationUpdate(
                created.getId(), CLIENT_A, newIn, newOut, "SRV-B");
        check(updated != null, "update in lifecycle failed");
        check(updated.getCheckIn().equals(newIn), "lifecycle update checkIn wrong");

        Reservation cancelled = service.reservationCancelation(created.getId(), CLIENT_A);
        check(cancelled != null && cancelled.getStatus() == ReservationStatus.CANCELLED,
                "cancel in lifecycle failed");

        List<Reservation> confirmed = reservationRepo.getConfirmedReservations();
        check(confirmed.stream().noneMatch(r -> r.getId().equals(created.getId())),
                "cancelled reservation still confirmed");

        Reservation reReadable = service.getReservationShortId(created.getId().toString());
        check(reReadable != null && reReadable.getStatus() == ReservationStatus.CANCELLED,
                "cancelled reservation no longer readable");

        reservationRepo.delete(created.getId());
        check(!reservationRepo.existByUuid(created.getId()), "hard delete failed");
        check(service.getReservationShortId(created.getId().toString()) == null,
                "reservation still readable after delete");
    }
}
