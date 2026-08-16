package dk.martinrohwedder.todolist_backend.services;

import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.entities.Todolist;
import dk.martinrohwedder.todolist_backend.exceptions.ResourceNotFoundException;
import dk.martinrohwedder.todolist_backend.repositories.TodolistRepository;
import dk.martinrohwedder.todolist_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodolistService {
    private final TodolistRepository todolistRepository;
    private final UserRepository userRepository;

    public List<Todolist> getTodolists(String username) {
        return todolistRepository.findAllByUserUsername(username);
    }

    public Todolist getTodolist(UUID id, String username) {
        return todolistRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Todolist not found"));
    }

    public Todolist createTodolist(String username, String title) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Todolist todolist = Todolist.builder()
                .title(title)
                .user(user)
                .build();

        return todolistRepository.save(todolist);
    }

    public void deleteTodolist(UUID id, String username) {
        Todolist todolist = todolistRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Todolist not found"));

        todolistRepository.delete(todolist);
    }
}
