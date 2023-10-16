package com.mchudzik.restapi.exceptions;


public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(Long id) {
        super("User with ID = '" + id + "' does not exist.");
      }
}
