import db.DatabaseConnection;
import db.DatabaseInitializer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FlywayMigrationTest {

    public static void main(String[] args) throws Exception {

        DatabaseInitializer.initialize();

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement statement = connection.createStatement()) {

            List<String> expectedTables = List.of(
                    "clients", "rooms", "reservations", "invoices", "payments"
            );

            List<String> actualTables = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery(
                    "SELECT table_name FROM information_schema.tables "
                            + "WHERE table_schema = 'public'")) {
                while (rs.next()) {
                    actualTables.add(rs.getString(1));
                }
            }

            System.out.println("Tables in database: " + actualTables);

            boolean allFound = actualTables.containsAll(expectedTables);
            System.out.println("All expected tables present: " + allFound);

            boolean migrated = false;
            try (ResultSet rs = statement.executeQuery(
                    "SELECT version, description, success "
                            + "FROM flyway_schema_history ORDER BY installed_rank")) {
                System.out.println("=== FLYWAY SCHEMA HISTORY ===");
                while (rs.next()) {
                    System.out.printf("version=%s description=%s success=%s%n",
                            rs.getString(1), rs.getString(2), rs.getBoolean(3));
                    if ("1".equals(rs.getString(1)) && rs.getBoolean(3)) {
                        migrated = true;
                    }
                }
            }

            System.out.println("Migration V1 applied successfully: " + migrated);

            if (!allFound || !migrated) {
                throw new AssertionError("Flyway migration verification FAILED");
            }
            System.out.println("FLYWAY MIGRATION TEST PASSED");
        }
    }
}