package com.mchudzik.restapi.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mchudzik.restapi.enums.Status;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Task {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String desc;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDate finishDate;
    @ElementCollection
    private List<Long> assignedUsers;

    Task(){
        assignedUsers = new ArrayList<>();
    }

    public Task(String name, String desc, Status status, LocalDate finishDate)
    {
        this();
        this.name = name;
        this.desc = desc;
        this.status = status;
        this.finishDate = finishDate;
    }

    public Long getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getDesc(){
        return this.desc;
    }

    public Status getStatus(){
        return this.status;
    }

    public LocalDate getFinishDate(){
        return this.finishDate;
    }

    public List<Long> getAssignedUsers(){
        return this.assignedUsers;
    }


    public void setID(Long id)
    {
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }
    
    public void setDesc(String desc){
        this.desc = desc;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public void setFinishDate(LocalDate finishDate){
        this.finishDate = finishDate;
    }

    public void setAssignedUsers(List<Long> assignedUsers)
    {
        this.assignedUsers = assignedUsers;
    }

    public void addUser(Long id){
        if(!assignedUsers.contains(id))
        {
            this.assignedUsers.add(id);
        }
    }

    public void removeUser(Long id)
    {
        assignedUsers.removeIf(n -> (n.equals(id)));
    }


    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof Task))
        {
            return false;
        }
        Task task = (Task) o;
        return this.id.equals(task.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.id,this.name,this.desc,this.status,this.finishDate,this.assignedUsers);
    }

    @Override
  public String toString() {
    return "Task{" + "id= " + this.id + ", name= " + this.name + ", desc=" + this.desc + ", status= " + this.status.name() + ", finishDate= " + this.finishDate + "}";
  }
}
