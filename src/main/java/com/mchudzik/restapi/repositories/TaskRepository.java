package com.mchudzik.restapi.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mchudzik.restapi.enums.Status;
import com.mchudzik.restapi.models.Task;

public interface TaskRepository extends JpaRepository<Task,Long>{
    
    List<Task> findAllByAssignedUsers(Long ID);

    List<Task> findAllByNameOrDescContainingIgnoreCase(String name, String desc);

    List<Task> findAllByStatus(Status status);

    @Query("select t from Task t where t.finishDate <= :finishDate")
    List<Task> findAllWithFinishDateBefore(LocalDate finishDate);

    List<Task> findAllByFinishDateBetween(LocalDate finishDateStart, LocalDate finishDateEnd);
}

