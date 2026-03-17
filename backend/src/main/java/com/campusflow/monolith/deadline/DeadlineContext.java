package com.campusflow.monolith.deadline;
public class DeadlineContext {

    private final long startTimeMs;
    private final long deadlineMs;

    public DeadlineContext(long startTimeMs, long deadlineMs) {
        this.startTimeMs = startTimeMs;
        this.deadlineMs = deadlineMs;
    }

    public long getRemainingTimeMs() {
        long elapsed = System.currentTimeMillis() - startTimeMs;
        return deadlineMs - elapsed;
    }

    public boolean isExpired() {
        return getRemainingTimeMs() <= 0;
    }
}