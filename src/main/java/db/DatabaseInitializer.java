package db;

import org.flywaydb.core.Flyway;

import java.io.InputStream;
import java.util.Properties;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        Properties props = new Properties();

        try (InputStream input = DatabaseInitializer.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("db.properties not found");
            }

            props.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }

        Flyway flyway = Flyway.configure()
                .dataSource(
                        props.getProperty("db.url"),
                        props.getProperty("db.user"),
                        props.getProperty("db.password")
                )
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();

        flyway.migrate();
    }
}