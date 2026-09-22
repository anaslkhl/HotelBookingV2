import db.DatabaseConnection;
import model.Room;
import model.enums.RoomStatus;
import model.enums.RoomType;
import repository.jdbc.JdbcRoomRepository;

import java.math.BigDecimal;
import java.sql.Statement;
import java.util.List;

public class JdbcRoomRepositoryTest {

    private static final JdbcRoomRepository repo = new JdbcRoomRepository();

    private static void cleanup() throws Exception {
        Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
        stmt.execute("DELETE FROM rooms WHERE room_number IN ('TEST-101', 'TEST-202')");
    }

    public static void main(String[] args) throws Exception {
        cleanup();

        int[] tally = {0, 0};
        tally[run("save() creates room", testSave())]++;
        tally[run("existByNumber() finds saved room", testExists())]++;
        tally[run("getByNumber() reads back room", testGetByNumber())]++;
        tally[run("getAvailableRooms() filters status", testGetAvailable())]++;
        tally[run("findAll() lists rooms", testFindAll())]++;
        tally[run("delete() removes room", testDelete())]++;

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
            repo.save("TEST-101", RoomType.SINGLE, RoomStatus.AVAILABLE, 2, new BigDecimal("99.50"));
            boolean saved = repo.existByNumber("TEST-101");
            cleanup();
            return saved;
        } catch (Exception e) {
            System.out.println("   save threw: " + e);
            return false;
        }
    }

    private static boolean testExists() {
        try {
            repo.save("TEST-101", RoomType.SINGLE, RoomStatus.AVAILABLE, 2, new BigDecimal("99.50"));
            boolean yes = repo.existByNumber("TEST-101");
            boolean no = repo.existByNumber("NOPE-000");
            cleanup();
            System.out.println("   exist(TEST-101)=" + yes + " exist(NOPE-000)=" + no);
            return yes && !no;
        } catch (Exception e) {
            System.out.println("   exists threw: " + e);
            return false;
        }
    }

    private static boolean testGetByNumber() {
        try {
            repo.save("TEST-101", RoomType.SINGLE, RoomStatus.AVAILABLE, 2, new BigDecimal("99.50"));
            Room room = repo.getByNumber("TEST-101");
            boolean ok = room != null
                    && "TEST-101".equals(room.getRoomNumber())
                    && room.getType() == RoomType.SINGLE
                    && room.getStatus() == RoomStatus.AVAILABLE
                    && room.getCapacity() == 2
                    && room.getPricePerNight().compareTo(new BigDecimal("99.50")) == 0;
            if (room == null) System.out.println("   getByNumber returned null");
            else System.out.println("   got=" + room.getRoomNumber() + " / " + room.getType()
                    + " / " + room.getStatus() + " / cap=" + room.getCapacity()
                    + " / price=" + room.getPricePerNight());
            cleanup();
            return ok;
        } catch (Exception e) {
            System.out.println("   getByNumber threw: " + e);
            return false;
        }
    }

    private static boolean testGetAvailable() {
        try {
            repo.save("TEST-101", RoomType.SINGLE, RoomStatus.AVAILABLE, 2, new BigDecimal("99.50"));
            repo.save("TEST-202", RoomType.SUITE, RoomStatus.MAINTENANCE, 4, new BigDecimal("150.00"));
            List<Room> available = repo.getAvailableRooms();
            boolean has101 = available.stream().anyMatch(r -> "TEST-101".equals(r.getRoomNumber()));
            boolean has202 = available.stream().anyMatch(r -> "TEST-202".equals(r.getRoomNumber()));
            System.out.println("   available rooms: " + available.stream().map(Room::getRoomNumber).toList());
            cleanup();
            return has101 && !has202;
        } catch (Exception e) {
            System.out.println("   getAvailableRooms threw: " + e);
            return false;
        }
    }

    private static boolean testFindAll() {
        try {
            repo.save("TEST-101", RoomType.SINGLE, RoomStatus.AVAILABLE, 2, new BigDecimal("99.50"));
            List<Room> all = repo.findAll();
            System.out.println("   total rooms in DB: " + all.size());
            boolean ok = all.stream().anyMatch(r -> "TEST-101".equals(r.getRoomNumber()));
            cleanup();
            return ok;
        } catch (Exception e) {
            System.out.println("   findAll threw: " + e);
            return false;
        }
    }

    private static boolean testDelete() {
        try {
            repo.save("TEST-101", RoomType.SINGLE, RoomStatus.AVAILABLE, 2, new BigDecimal("99.50"));
            repo.delete("TEST-101");
            boolean gone = !repo.existByNumber("TEST-101");
            cleanup();
            return gone;
        } catch (Exception e) {
            System.out.println("   delete threw: " + e);
            return false;
        }
    }
}