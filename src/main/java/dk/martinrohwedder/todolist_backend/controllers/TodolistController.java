package dk.martinrohwedder.todolist_backend.controllers;

import dk.martinrohwedder.todolist_backend.dtos.TodolistRequest;
import dk.martinrohwedder.todolist_backend.dtos.TodolistResponse;
import dk.martinrohwedder.todolist_backend.services.TodolistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todolists")
@RequiredArgsConstructor
public class TodolistController {
    private final TodolistService todolistService;

    @GetMapping
    public ResponseEntity<List<TodolistResponse>> getTodolists(Authentication authentication) {
        String username = authentication.getName();

        var todolists = todolistService
                .getTodolists(username)
                .stream()
                .map(TodolistResponse::from)
                .toList();

        return ResponseEntity.ok(todolists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodolistResponse> getTodolist(@PathVariable UUID id, Authentication authentication) {
        String username = authentication.getName();
        var todolist = todolistService.getTodolist(id, username);

        return ResponseEntity.ok(TodolistResponse.from(todolist));
    }

    @PostMapping
    public ResponseEntity<TodolistResponse> createTodolist(@Valid @RequestBody TodolistRequest request, Authentication authentication) {
        String username = authentication.getName();
        var todolist = todolistService.createTodolist(username, request.title());
        var uriLocation = UriComponentsBuilder.fromPath("/api/todolists/{id}").buildAndExpand(todolist.getId()).toUri();

        return ResponseEntity.created(uriLocation).body(TodolistResponse.from(todolist));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodolist(@PathVariable UUID id, Authentication authentication) {
        String username = authentication.getName();
        todolistService.deleteTodolist(id, username);

        return ResponseEntity.noContent().build();
    }
}
