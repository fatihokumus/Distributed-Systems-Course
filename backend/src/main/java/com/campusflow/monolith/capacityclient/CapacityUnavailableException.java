package com.campusflow.monolith.capacityclient;

public class CapacityUnavailableException extends RuntimeException {
    public CapacityUnavailableException(String message) {
        super(message);
    }

    public CapacityUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}