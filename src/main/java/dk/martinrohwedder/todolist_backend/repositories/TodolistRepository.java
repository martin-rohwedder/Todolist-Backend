package dk.martinrohwedder.todolist_backend.repositories;

import dk.martinrohwedder.todolist_backend.entities.Todolist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodolistRepository extends JpaRepository<Todolist, UUID> {
    List<Todolist> findAllByUserUsername(String username);
    Optional<Todolist> findByIdAndUserUsername(UUID uuid, String username);
}
