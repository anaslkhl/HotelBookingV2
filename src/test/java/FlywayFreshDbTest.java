import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FlywayFreshDbTest {

    public static void main(String[] args) throws Exception {

        String url = "jdbc:postgresql://localhost:5433/hotel_booking_fresh";

        Flyway flyway = Flyway.configure()
                .dataSource(url, "user", "hotelbooking")
                .load();

        var result = flyway.migrate();
        System.out.println("Migrations run: " + result.migrationsExecuted);

        List<String> expectedTables = List.of(
                "clients", "rooms", "reservations", "invoices", "payments"
        );

        try (Connection connection = DriverManager.getConnection(url, "user", "hotelbooking");
             Statement statement = connection.createStatement()) {

            List<String> actualTables = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery(
                    "SELECT table_name FROM information_schema.tables "
                            + "WHERE table_schema = 'public' AND table_name NOT LIKE 'flyway%'")) {
                while (rs.next()) {
                    actualTables.add(rs.getString(1));
                }
            }
            System.out.println("Tables created from scratch: " + actualTables);

            boolean allFound = actualTables.containsAll(expectedTables);
            System.out.println("All expected tables created: " + allFound);

            if (!allFound || result.migrationsExecuted != 1) {
                throw new AssertionError("Fresh DB migration verification FAILED");
            }
            System.out.println("FRESH DB MIGRATION TEST PASSED");
        }
    }
}