package dk.martinrohwedder.todolist_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodolistRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 50, message = "Title must not exceed 50 characters")
        String title
) {}