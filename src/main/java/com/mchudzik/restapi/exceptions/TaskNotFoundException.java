package com.mchudzik.restapi.exceptions;


public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Task with ID = '" + id + "' does not exist.");
      }
    
}
