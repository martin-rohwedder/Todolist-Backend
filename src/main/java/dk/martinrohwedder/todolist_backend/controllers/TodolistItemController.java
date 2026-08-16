package dk.martinrohwedder.todolist_backend.controllers;

import dk.martinrohwedder.todolist_backend.dtos.TodolistItemRequest;
import dk.martinrohwedder.todolist_backend.dtos.TodolistItemResponse;
import dk.martinrohwedder.todolist_backend.services.TodolistItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todolists/{todolistId}/items")
@RequiredArgsConstructor
public class TodolistItemController {
    private final TodolistItemService todolistItemService;

    @GetMapping
    public ResponseEntity<List<TodolistItemResponse>> getItems(@PathVariable UUID todolistId, Authentication authentication) {
        String username = authentication.getName();

        var responses = todolistItemService
                .getItems(todolistId, username)
                .stream()
                .map(TodolistItemResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<TodolistItemResponse> getItem(@PathVariable UUID todolistId, @PathVariable UUID itemId, Authentication authentication) {
        String username = authentication.getName();
        var item = todolistItemService.getItem(todolistId, itemId, username);

        return ResponseEntity.ok(TodolistItemResponse.from(item));
    }

    @PostMapping
    public ResponseEntity<TodolistItemResponse> createItem(@PathVariable UUID todolistId, @Valid @RequestBody TodolistItemRequest request, Authentication authentication) {
        String username = authentication.getName();
        var item = todolistItemService.createItem(todolistId, username, request.title());
        var uriLocation = UriComponentsBuilder.fromPath("/api/todolists/{todolistId}/items/{itemId}").buildAndExpand(todolistId, item.getId()).toUri();

        return ResponseEntity.created(uriLocation).body(TodolistItemResponse.from(item));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<TodolistItemResponse> updateItem(@PathVariable UUID todolistId, @PathVariable UUID itemId, @Valid @RequestBody TodolistItemRequest request, Authentication authentication) {
        String username = authentication.getName();
        var item = todolistItemService.updateItem(todolistId, itemId, username, request.title());

        return ResponseEntity.ok(TodolistItemResponse.from(item));
    }

    @PatchMapping("/{itemId}/toogleCompleted")
    public ResponseEntity<TodolistItemResponse> toggleCompleted(@PathVariable UUID todolistId, @PathVariable UUID itemId, Authentication authentication) {
        String username = authentication.getName();
        var item = todolistItemService.toggleCompleted(todolistId, itemId, username);

        return ResponseEntity.ok(TodolistItemResponse.from(item));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID todolistId, @PathVariable UUID itemId, Authentication authentication) {
        String username = authentication.getName();
        todolistItemService.deleteItem(todolistId, itemId, username);

        return ResponseEntity.noContent().build();
    }
}
