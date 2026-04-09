 package com.campusflow.monolith.capacityclient;

public class CapacityClientException extends RuntimeException {
    public CapacityClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CapacityClientException(String message) {
        super(message);
    }
} 
