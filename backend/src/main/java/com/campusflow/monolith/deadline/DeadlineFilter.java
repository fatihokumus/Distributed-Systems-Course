package com.campusflow.monolith.deadline;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DeadlineFilter extends OncePerRequestFilter {

    private static final long DEFAULT_DEADLINE_MS = 5000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("DeadlineFilter running, header=" + request.getHeader("X-Deadline-Ms"));

        String deadlineHeader = request.getHeader("X-Deadline-Ms");
        long deadlineMs = DEFAULT_DEADLINE_MS;

        if (deadlineHeader != null && !deadlineHeader.isBlank()) {
            try {
                deadlineMs = Long.parseLong(deadlineHeader);
            } catch (NumberFormatException e) {
                deadlineMs = DEFAULT_DEADLINE_MS;
            }
        }

        DeadlineContext context = new DeadlineContext(System.currentTimeMillis(), deadlineMs);
        DeadlineHolder.set(context);

        try {
            filterChain.doFilter(request, response);
        } finally {
            DeadlineHolder.clear();
        }
    }
}