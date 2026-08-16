package dk.martinrohwedder.todolist_backend.dtos;

import dk.martinrohwedder.todolist_backend.entities.TodolistItem;

import java.time.LocalDateTime;
import java.util.UUID;

public record TodolistItemResponse(
        UUID id,
        String title,
        boolean completed,
        UUID todolistId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TodolistItemResponse from(TodolistItem item) {
        return new TodolistItemResponse(
                item.getId(),
                item.getTitle(),
                item.isCompleted(),
                item.getTodolist().getId(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
