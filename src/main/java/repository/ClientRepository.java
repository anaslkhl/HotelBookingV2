package repository;

import model.Client;

import java.util.List;
import java.util.UUID;

public interface ClientRepository {

    void save(Client client);

    Client findById(UUID id);

    Client findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsById(UUID id);

    void delete(UUID id);

    List<Client> findAll();


}