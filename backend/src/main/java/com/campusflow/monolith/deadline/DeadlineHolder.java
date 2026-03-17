package com.campusflow.monolith.deadline;
public final class DeadlineHolder {

    private static final ThreadLocal<DeadlineContext> HOLDER = new ThreadLocal<>();

    private DeadlineHolder() {
    }

    public static void set(DeadlineContext context) {
        HOLDER.set(context);
    }

    public static DeadlineContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}