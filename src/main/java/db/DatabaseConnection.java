package db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {

    private static volatile DatabaseConnection instance;

    private final Connection connection;

    private DatabaseConnection() {
        try {
            Properties props = new Properties();


            try (InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("db.properties")) {

                if (input == null) {
                    throw new RuntimeException("db.properties not found");
                }

                props.load(input);
            }

            this.connection = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur critique de connexion JDBC : " + e.getMessage(), e
            );
        }
    }

    public static DatabaseConnection getInstance() {

        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }

        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}