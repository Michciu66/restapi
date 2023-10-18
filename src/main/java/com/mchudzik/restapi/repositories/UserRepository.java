package com.mchudzik.restapi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mchudzik.restapi.models.User;

public interface UserRepository extends JpaRepository<User,Long>{
    

    List<User> findAllByNameContainingOrSurnameContainingAllIgnoreCase(String name, String surname);
}
