package com.mchudzik.restapi.assemblers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.mchudzik.restapi.controllers.TaskController;
import com.mchudzik.restapi.models.Task;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TaskModelAssembler implements RepresentationModelAssembler<Task,EntityModel<Task>> {
    @Override
    public EntityModel<Task> toModel(Task task)
    {
        return EntityModel.of(task, 
        linkTo(methodOn(TaskController.class).FindTaskByID(task.getId())).withSelfRel(),
        linkTo(methodOn(TaskController.class).ListTasks()).withRel("tasks"));
    }
}
