package com.machine.atm.controller;

import com.machine.atm.exception.CustomerNotFoundException;
import com.machine.atm.exception.PreDispenseValidationException;
import com.machine.atm.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Error resourceNotFoundException(CustomerNotFoundException ex) {
        return new Error(new Date() , ex.getMessage(), ex.getDescription());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public Error globalExceptionHandler(Exception ex) {
        return new Error(new Date(),ex.getMessage() , null);
    }

    @ExceptionHandler(PreDispenseValidationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Error preDispenseValidation(PreDispenseValidationException ex) {
        return new Error(new Date(),ex.getMessage() , ex.getDescription());
    }

}
