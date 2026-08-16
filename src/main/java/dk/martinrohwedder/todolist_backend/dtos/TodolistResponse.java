package dk.martinrohwedder.todolist_backend.dtos;

import dk.martinrohwedder.todolist_backend.entities.Todolist;

import java.time.LocalDateTime;
import java.util.UUID;

public record TodolistResponse(
        UUID id,
        String title,
        UUID userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TodolistResponse from(Todolist todolist) {
        return new TodolistResponse(
                todolist.getId(),
                todolist.getTitle(),
                todolist.getUser().getId(),
                todolist.getCreatedAt(),
                todolist.getUpdatedAt()
        );
    }
}
