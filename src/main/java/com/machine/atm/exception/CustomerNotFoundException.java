package com.machine.atm.exception;

public class CustomerNotFoundException extends RuntimeException{
    private String description;
    public CustomerNotFoundException(String message, String description){
        super(message);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}