package com.machine.atm.exception;

public class PreDispenseValidationException extends RuntimeException{
    private String description;
    public PreDispenseValidationException(String description){
        super("Can not dispense the request amount");
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}