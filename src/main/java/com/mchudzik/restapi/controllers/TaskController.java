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

    @GetMapping("/{id}")
    public EntityModel<Task> findTaskByID(@PathVariable Long id)
    {
        Task task = repo.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        
        return assembler.toModel(task);

    }

    @GetMapping("ByUser")
    public CollectionModel<EntityModel<Task>> findTaskByUserID(@RequestParam Long id)
    {
        List<EntityModel<Task>> tasks = repo.findAllByAssignedUsers(id).stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

        return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).findTaskByUserID(id)).withSelfRel());
    }

    @GetMapping("ByName")
    public CollectionModel<EntityModel<Task>> findTaskByString(@RequestParam String str)
    {
        List<EntityModel<Task>> tasks = repo.findAllByNameOrDescContainingIgnoreCase(str,str).stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

        return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).findTaskByString(str)).withSelfRel());
    }

    @GetMapping("ByStatus")
    public CollectionModel<EntityModel<Task>> findTaskByStatus(@RequestParam String status)
    {
        try{
            Status statusEnum = Status.valueOf(status);
            List<EntityModel<Task>> tasks = repo.findAllByStatus(statusEnum).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

            return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).findTaskByStatus(status)).withSelfRel());
        }
        catch(Exception e)
        {
            throw new StatusNotFoundException(status);
        }
    }

    @GetMapping("BeforeDate")
    public CollectionModel<EntityModel<Task>> findTaskBeforeDate(@RequestParam LocalDate date)
    {
        List<EntityModel<Task>> tasks = repo.findAllWithFinishDateBefore(date).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
                
            return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).findTaskBeforeDate(date)).withSelfRel());
    }

    @GetMapping("BeforeDate/{startDate}/{endDate}")
    public CollectionModel<EntityModel<Task>> findTaskBetweenDates(@PathVariable LocalDate startDate, @PathVariable LocalDate endDate)
    {
        List<EntityModel<Task>> tasks = repo.findAllByFinishDateBetween(startDate, endDate).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
                
            return CollectionModel.of(tasks, linkTo(methodOn(TaskController.class).findTaskBetweenDates(startDate, endDate)).withSelfRel());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> editTask(@RequestBody Task newTask, @PathVariable Long id) 
    {
    Task editedTask = repo.findById(id).map(task -> {
        task.setName(newTask.getName());
        task.setDesc(newTask.getDesc());
        task.setFinishDate(newTask.getFinishDate());
        task.setStatus(newTask.getStatus());
        task.setAssignedUsers(newTask.getAssignedUsers());
        return repo.save(task);
      })
      .orElseThrow(() -> new TaskNotFoundException(id));

      EntityModel<Task> entityModel = assembler.toModel(editedTask);

      return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);

    }

    @PutMapping("Status/{id}")
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

    @PutMapping("/{taskid}/{userid}")
    public ResponseEntity<?> assignUser(@RequestParam Long taskid, @RequestParam Long userid)
    {
        Task out = repo.findById(taskid).orElseThrow(() -> new TaskNotFoundException(userid));
        userRepo.findById(userid).orElseThrow(() -> new UserNotFoundException(userid));
        out.addUser(userid);
       
        EntityModel<Task> entityModel = assembler.toModel(out);

        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }
    
    @DeleteMapping("/{taskid}/{userid}")
    public ResponseEntity<?> unassignUser(@RequestParam Long taskid, @RequestParam Long userid)
    {
        Task out = repo.findById(taskid).orElseThrow(() -> new TaskNotFoundException(userid));
        out.removeUser(userid);

        return ResponseEntity.noContent().build();
    }  
}
