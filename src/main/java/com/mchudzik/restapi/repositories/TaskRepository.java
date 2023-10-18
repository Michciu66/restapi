package com.mchudzik.restapi.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mchudzik.restapi.enums.Status;
import com.mchudzik.restapi.models.Task;

public interface TaskRepository extends JpaRepository<Task,Long>{
    
    List<Task> findAllByAssignedUsers(Long id);

    List<Task> findAllByNameContainingOrDescContainingAllIgnoreCase(String name, String desc);

    List<Task> findAllByStatus(Status status);

    List<Task> findAllByFinishDateLessThanEqual(LocalDate finishDate);

    List<Task> findAllByFinishDateBetween(LocalDate finishDateStart, LocalDate finishDateEnd);
}

