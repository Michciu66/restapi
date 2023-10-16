package com.mchudzik.restapi.models;


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

    public void setID(Long ID)
    {
        this.id = ID;
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
        User User = (User) o;
        return this.id == User.id;
    }

    @Override
  public String toString() {
    return "User{" + "id= " + this.id + ", name= " + this.name + ", surname=" + this.surname + ", email= " + this.email + "}";
  }

}
