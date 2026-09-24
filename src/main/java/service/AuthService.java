package service;

import util.EmailValidation;
import util.NameValidation;
import util.PasswordValidation;
import util.PhoneValidation;
import model.Client;
import exception.*;
import repository.ClientRepository;


import java.util.UUID;

public class AuthService {


    private Client currentUser;

    private final ClientRepository clientRepository;

    public AuthService(ClientRepository clientRepository){

        this.clientRepository = clientRepository;
    }


    public Client ajouterClient(Client client){

        EmailValidation.validateEmail(client.getEmail());
        NameValidation.validateName(client.fullName);
        PhoneValidation.validatePhone(client.getPhone());
        PasswordValidation.validatePassword(client.getPassword());

            if(clientRepository.existsByEmail(client.getEmail())){

                throw new ClientAlreadyExistException("Client already exist !");
            }else{
                clientRepository.save(client);
                System.out.println(">> Client added successfully <<");

            }

        return client;

    }

    public Client Login(String email, String password){

        Client client = clientRepository.findByEmail(email);

        if(client == null){

            throw new ClientNotFoundException("Client not found !");
        }
        if(!client.getPassword().equals(password)){
            throw new PasswordIncorrectException("the password is incorrect !");

        }
            System.out.println("<< Client logged successfully >>");
            return client;
    }

    public Client chercherClient(UUID id){

        Client client = clientRepository.findById(id);

        if(client == null){

            throw new ClientNotFoundException("this Client ID not found " + id);
        }
        return client;
    }

    public void setCurrentUser(Client client){
        this.currentUser = client;
    }

    public Client getCurrentUser(){
        return currentUser;
    }

    public void logout(){
        currentUser = null;
    }

    public Client updateProfile(UUID userId, String fullName, String email, String phone){

        Client client = clientRepository.findById(userId);

        if(client == null){
            throw new ClientNotFoundException("Client not found ! ");
        }
        NameValidation.validateName(fullName);
        EmailValidation.validateEmail(email);
        PhoneValidation.validatePhone(phone);

        if(!client.getEmail().equals(email) && clientRepository.existsByEmail(email)){
            throw new ClientAlreadyExistException("This email already used by another client !!");
        }

        client.setFullName(fullName);
        client.setEmail(email);
        client.setPhone(phone);

        clientRepository.save(client);
        return client;
    }

    public Client changePassword(UUID userId, String currentPassword, String newPassword){

        Client client = clientRepository.findById(userId);

        if(client == null){
            throw new ClientNotFoundException("Client not found !! ");
        }

        if(!client.getPassword().equals(currentPassword)){
            throw new InvalidPasswordException("Current password is incorrect ! ");
        }

        if(!PasswordValidation.validatePassword(newPassword)){
            throw new InvalidPasswordException("The password must be more than 6 characters !");
        }
        client.setPassword(newPassword);
        clientRepository.save(client);
        System.out.println("Password changed successfully!");

        return client;
    }

    public void initializeClients(){

        ajouterClient(new Client("Anas lakhal", "anas.lakhal123@gmail.com", "0664060379", "anaslkhl"));
        ajouterClient(new Client("Amine fadil", "anas_lakhal123@gmail.com", "0654650379", "aminfad"));
        ajouterClient(new Client("kaml akmir", "anasakhal1@gmail.com", "0664512379", "klamakmi"));
        ajouterClient(new Client("Ilayja pobe", "illa.laal12@gmail.com", "0612450379", "illapobe"));
        ajouterClient(new Client("Carl marks", "carl.marks@gmail.com", "0664598379", "carlamrk"));
        ajouterClient(new Client("Albert camus", "albert.camus@gmail.com", "0735960379", "albertcam"));
    }
}