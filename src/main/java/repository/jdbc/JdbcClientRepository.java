package repository.jdbc;

import db.DatabaseConnection;
import model.Client;
import repository.ClientRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcClientRepository implements ClientRepository {

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void save(Client client) {

        String sql = """
                INSERT INTO clients (id, full_name, email, phone, password)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    full_name = EXCLUDED.full_name,
                    email = EXCLUDED.email,
                    phone = EXCLUDED.phone,
                    password = EXCLUDED.password
                """;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, client.getId());
            stmt.setString(2, client.getFullName());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getPhone());
            stmt.setString(5, client.getPassword());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save client : " + e.getMessage(), e);
        }
    }

    @Override
    public Client findById(UUID id) {

        String sql = "SELECT id, full_name, email, phone, password FROM clients WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find client by id : " + e.getMessage(), e);
        }
    }

    @Override
    public Client findByEmail(String email) {

        String sql = "SELECT id, full_name, email, phone, password FROM clients WHERE email = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {

                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find client by email : " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {

        String sql = "SELECT 1 FROM clients WHERE email = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check client email : " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(UUID id) {

        String sql = "SELECT 1 FROM clients WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check client id : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID id) {

        String sql = "DELETE FROM clients WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            stmt.setObject(1, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete client : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Client> findAll() {

        String sql = "SELECT id, full_name, email, phone, password FROM clients";
        List<Client> clients = new ArrayList<>();

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapRow(rs));
            }

            return clients;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all clients : " + e.getMessage(), e);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {

        Client client = new Client(
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password")
        );

        client.setUuid((UUID) rs.getObject("id"));

        return client;
    }
}