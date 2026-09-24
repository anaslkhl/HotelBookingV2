package model;

import java.math.BigDecimal;
import java.util.UUID;

public class Client {


    private UUID id;
    private static int counter =0;
    public String fullName;
    private String email;
    private String phone;
    private String password;
    private BigDecimal sold;

    public Client(String fullName, String email, String phone, String password){

        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.id = UUID.randomUUID();
        this.sold = new BigDecimal("1000.00");
        counter++;

    }

    public String getFullName(){
        return this.fullName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getPassword(){return this.password;}


    public void setFullName(String fullName){

        this.fullName = fullName;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void  setUuid(UUID id){
        this.id = id;
    }

    public UUID getId(){
        return  this.id;
    }

    public BigDecimal getSold() {
        return sold;
    }

    public void setSold(BigDecimal sold) {
        this.sold = sold;
    }
}