package com.example.ticktracker.exception;

import java.util.Map;

public class InvalidSightingException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public InvalidSightingException(Map<String, String> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}