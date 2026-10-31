package com.example.ticktracker.ingestion;

public class ElancoApiException extends RuntimeException {
    public ElancoApiException(String message, Throwable cause) {
        super(message, cause);
    }
}