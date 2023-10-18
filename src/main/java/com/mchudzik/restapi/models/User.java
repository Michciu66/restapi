package com.mchudzik.restapi.models;


import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String surname;
    private String email;

    User() {}

    public User(String name, String surname, String email)
    {
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public Long getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }
    
    public String getSurname(){
        return this.surname;
    }
    
    public String getEmail(){
        return this.email;
    }

    public void setID(Long id)
    {
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setSurname(String surname){
        this.surname = surname;
    }

    public void setEmail(String email){
        this.email = email;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof User))
        {
            return false;
        }
        User user = (User) o;
        return this.id.equals(user.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.name, this.surname, this.email);
    }

    @Override
  public String toString() {
    return "User{" + "id= " + this.id + ", name= " + this.name + ", surname=" + this.surname + ", email= " + this.email + "}";
  }

}
