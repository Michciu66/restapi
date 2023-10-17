package com.mchudzik.restapi.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.mchudzik.restapi.assemblers.UserModelAssembler;
import com.mchudzik.restapi.exceptions.UserNotFoundException;
import com.mchudzik.restapi.models.User;
import com.mchudzik.restapi.repositories.UserRepository;

@RestController()
@RequestMapping("/users")
public class UserController {
    private final UserRepository repo;
    private final UserModelAssembler assembler;

    UserController(UserRepository repo, UserModelAssembler assembler)
    {
        this.repo = repo;
        this.assembler = assembler;
    }

    @GetMapping()
    public CollectionModel<EntityModel<User>> listUsers()
    {
        List<EntityModel<User>> users =  repo.findAll().stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(UserController.class).listUsers()).withSelfRel());
    }

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody User user)
    {

        EntityModel<User> entityModel = assembler.toModel(repo.save(user));

        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) 
    {
        repo.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public EntityModel<User> findUserByID(@PathVariable Long id)
    {
        User user = repo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        
        return assembler.toModel(user);

    }

    @GetMapping("ByName")
    public CollectionModel<EntityModel<User>> findUserByString(@RequestParam String str)
    {

        List<EntityModel<User>> users =  repo.findAllByNameOrSurnameContainingIgnoreCase(str, str).stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

        return CollectionModel.of(users, linkTo(methodOn(UserController.class).findUserByString(str)).withSelfRel());

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editUser(@RequestBody User newUser, @PathVariable Long id) 
    {
    User editedUser = repo.findById(id).map(user -> {
        user.setName(newUser.getName());
        user.setSurname(newUser.getSurname());
        user.setEmail(newUser.getEmail());
        return repo.save(user);
      })
      .orElseGet(() -> {
        newUser.setID(id);
        return repo.save(newUser);
      });

      EntityModel<User> entityModel = assembler.toModel(editedUser);

      return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }
}
