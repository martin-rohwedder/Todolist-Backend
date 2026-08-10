package dk.martinrohwedder.todolist_backend.dtos;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String username,
        String accessToken
) {}
