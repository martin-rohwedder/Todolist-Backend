package dk.martinrohwedder.todolist_backend.dtos;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp
) {}
