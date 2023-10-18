package com.mchudzik.restapi.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.web.bind.annotation.*;

import com.mchudzik.restapi.models.Task;
import com.mchudzik.restapi.repositories.TaskRepository;
import com.mchudzik.restapi.repositories.UserRepository;
import com.mchudzik.restapi.assemblers.TaskModelAssembler;
import com.mchudzik.restapi.enums.Status;
import com.mchudzik.restapi.exceptions.StatusNotFoundException;
import com.mchudzik.restapi.exceptions.TaskNotFoundException;
import com.mchudzik.restapi.exceptions.UserNotFoundException;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskRepository repo;
    private final TaskModelAssembler assembler;
    private final UserRepository userRepo;

    TaskController(TaskRepository repo, TaskModelAssembler assembler, UserRepository userRepo)
    {
        this.repo = repo;
        this.userRepo = userRepo;
        this.assembler = assembler;
        
    }

    @GetMapping
    public CollectionModel<EntityModel<Task>> listTasks()
    {
        List<EntityModel<Task>> tasks = repo.findAll().stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

        return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).listTasks()).withSelfRel());

    }

    @GetMapping("/{id}")
    public EntityModel<Task> findTaskByID(@PathVariable Long id)
    {
        Task task = repo.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        
        return assembler.toModel(task);

    }

    @GetMapping("/byUser")
    public CollectionModel<EntityModel<Task>> findTaskByUserID(@RequestParam Long id)
    {
        List<EntityModel<Task>> tasks = repo.findAllByAssignedUsers(id).stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

        return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).findTaskByUserID(id)).withSelfRel());
    }

    @GetMapping("/byName")
    public CollectionModel<EntityModel<Task>> findTaskByString(@RequestParam String name)
    {
        List<EntityModel<Task>> tasks = repo.findAllByNameContainingOrDescContainingAllIgnoreCase(name,name).stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

        return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).findTaskByString(name)).withSelfRel());
    }

    @GetMapping("/byStatus")
    public CollectionModel<EntityModel<Task>> findTaskByStatus(@RequestParam String status)
    {
        try{
            Status statusEnum = Status.valueOf(status);
            List<EntityModel<Task>> tasks = repo.findAllByStatus(statusEnum).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

            return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).findTaskByStatus(status)).withSelfRel());
        }
        catch(IllegalArgumentException e)
        {
            throw new StatusNotFoundException(status);
        }
    }


    @GetMapping("/byDate")
    public CollectionModel<EntityModel<Task>> findTaskBetweenDates(@RequestParam LocalDate startDate, @RequestParam(required=false) LocalDate endDate)
    {
        if (endDate == null) {
            List<EntityModel<Task>> tasks = repo.findAllByFinishDateLessThanEqual(startDate).stream()
                    .map(assembler::toModel)
                    .collect(Collectors.toList());

            return CollectionModel.of(tasks,
                    linkTo(methodOn(TaskController.class).findTaskBetweenDates(startDate, null)).withSelfRel());
        } else {

            List<EntityModel<Task>> tasks = repo.findAllByFinishDateBetween(startDate, endDate).stream()
                    .map(assembler::toModel)
                    .collect(Collectors.toList());

            return CollectionModel.of(tasks,
                    linkTo(methodOn(TaskController.class).findTaskBetweenDates(startDate, endDate)).withSelfRel());
        }
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task)
    {
        EntityModel<Task> entityModel = assembler.toModel(repo.save(task));

        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) 
    {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> editTask(@RequestBody Task newTask, @PathVariable Long id) 
    {
    Task editedTask = repo.findById(id).map(task -> {
        task.setName(newTask.getName());
        task.setDesc(newTask.getDesc());
        task.setFinishDate(newTask.getFinishDate());
        task.setStatus(newTask.getStatus());
        return repo.save(task);
      })
      .orElseThrow(() -> new TaskNotFoundException(id));

      EntityModel<Task> entityModel = assembler.toModel(editedTask);

      return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);

    }

    @PutMapping("status/{id}")
    public ResponseEntity<?> editStatus(@RequestBody Status newStatus, @PathVariable Long id)
    {
        Task editedTask = repo.findById(id).map(task -> {
            task.setStatus(newStatus);
            return repo.save(task);
        })
        .orElseThrow(() -> new TaskNotFoundException(id));

        EntityModel<Task> entityModel = assembler.toModel(editedTask);

      return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @PutMapping("/{taskId}/{userId}")
    public ResponseEntity<?> assignUser(@RequestParam Long taskId, @RequestParam Long userId)
    {
        Task out = repo.findById(taskId).orElseThrow(() -> new TaskNotFoundException(userId));
        userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        out.addUser(userId);
       
        EntityModel<Task> entityModel = assembler.toModel(out);

        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }
    
    @DeleteMapping("/{taskId}/{userId}")
    public ResponseEntity<?> unassignUser(@RequestParam Long taskId, @RequestParam Long userId)
    {
        Task out = repo.findById(taskId).orElse(null);
        if (out != null)
        {
            out.removeUser(userId);
        }

        return ResponseEntity.noContent().build();
    }  
}
