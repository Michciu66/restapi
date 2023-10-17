package com.mchudzik.restapi.assemblers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.mchudzik.restapi.controllers.UserController;
import com.mchudzik.restapi.models.User;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserModelAssembler implements  RepresentationModelAssembler<User,EntityModel<User>>{
    
    @Override
    public EntityModel<User> toModel(User user)
    {
        return EntityModel.of(user, 
        linkTo(methodOn(UserController.class).findUserByID(user.getId())).withSelfRel(),
        linkTo(methodOn(UserController.class).listUsers()).withRel("users"));
    }
}
