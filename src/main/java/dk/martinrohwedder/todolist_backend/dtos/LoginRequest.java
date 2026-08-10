package dk.martinrohwedder.todolist_backend.dtos;

public record LoginRequest(
        String username,
        String password
) {}
