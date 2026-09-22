import db.DatabaseConnection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

public class TestConnection {

    public static void main(String[] args) throws Exception {

        System.out.println("=== JDBC DRIVER TEST ===");

        boolean driverFound = false;
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            System.out.println("Driver found: " + drivers.nextElement().getClass().getName());
            driverFound = true;
        }

        if (!driverFound) {
            System.out.println("NO JDBC DRIVER FOUND! Add the postgresql jar to the runtime classpath.");
            return;
        }

        System.out.println();
        System.out.println("=== CONNECTION TEST ===");

        try {
            DatabaseConnection first = DatabaseConnection.getInstance();

            try (Connection connection = first.getConnection()) {
                System.out.println("Connection OK -> " + connection.getMetaData().getURL());
            }

            DatabaseConnection second = DatabaseConnection.getInstance();
            System.out.println("Singleton OK (same instance): " + (first == second));

        } catch (RuntimeException e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}