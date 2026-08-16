package dk.martinrohwedder.todolist_backend.repositories;

import dk.martinrohwedder.todolist_backend.entities.TodolistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodolistItemRepository extends JpaRepository<TodolistItem, UUID> {
    List<TodolistItem> findAllByTodolistIdAndTodolistUserUsername(UUID todolistId, String username);
    Optional<TodolistItem> findByIdAndTodolistUserUsername(UUID id, String username);
}
