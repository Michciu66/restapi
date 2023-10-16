package com.mchudzik.restapi.exceptions;

public class StatusNotFoundException extends RuntimeException {
    public StatusNotFoundException(String input)
    {
        super(input + " is not a valid status.");
    }
}
