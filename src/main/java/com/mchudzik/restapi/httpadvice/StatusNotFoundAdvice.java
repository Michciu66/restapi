package com.mchudzik.restapi.httpadvice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.mchudzik.restapi.exceptions.StatusNotFoundException;

@ControllerAdvice
public class StatusNotFoundAdvice {
    
    @ResponseBody
    @ExceptionHandler(StatusNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    String statuskNotFoundHandler(StatusNotFoundException e)
    {
        return e.getMessage();
    }
}
