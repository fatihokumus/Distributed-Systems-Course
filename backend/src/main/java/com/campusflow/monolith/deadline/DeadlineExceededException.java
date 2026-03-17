package com.campusflow.monolith.deadline;
public class DeadlineExceededException extends RuntimeException {

    public DeadlineExceededException(String message) {
        super(message);
    }
}