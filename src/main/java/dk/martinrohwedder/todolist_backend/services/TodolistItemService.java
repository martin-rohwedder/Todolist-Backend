package dk.martinrohwedder.todolist_backend.services;

import dk.martinrohwedder.todolist_backend.entities.TodolistItem;
import dk.martinrohwedder.todolist_backend.exceptions.ResourceNotFoundException;
import dk.martinrohwedder.todolist_backend.repositories.TodolistItemRepository;
import dk.martinrohwedder.todolist_backend.repositories.TodolistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodolistItemService {
    private final TodolistItemRepository todolistItemRepository;
    private final TodolistRepository todolistRepository;

    public List<TodolistItem> getItems(UUID todolistId, String username) {
        verifyTodolistOwnership(todolistId, username);
        return todolistItemRepository.findAllByTodolistIdAndTodolistUserUsername(todolistId, username);
    }

    public TodolistItem getItem(UUID todolistId, UUID itemId, String username) {
        verifyTodolistOwnership(todolistId, username);
        TodolistItem item = todolistItemRepository.findByIdAndTodolistUserUsername(itemId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Todolist item not found"));

        // Make sure the item actually belongs to the todolist
        if (!item.getTodolist().getId().equals(todolistId)) {
            throw new ResourceNotFoundException("Todolist item not found");
        }

        return item;
    }

    public TodolistItem createItem(UUID todolistId, String username, String title) {
        var todolist = todolistRepository.findByIdAndUserUsername(todolistId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Todolist not found"));

        TodolistItem item = TodolistItem.builder()
                .title(title)
                .todolist(todolist)
                .build();

        return todolistItemRepository.save(item);
    }

    public TodolistItem updateItem(UUID todolistId, UUID itemId, String username, String title) {
        TodolistItem item = getItem(todolistId, itemId, username);
        item.setTitle(title);

        return todolistItemRepository.save(item);
    }

    public TodolistItem toggleCompleted(UUID todolistId, UUID itemId, String username) {
        TodolistItem item = getItem(todolistId, itemId, username);
        item.setCompleted(!item.isCompleted());

        return todolistItemRepository.save(item);
    }

    public void deleteItem(UUID todolistId, UUID itemId, String username) {
        TodolistItem item = getItem(todolistId, itemId, username);
        todolistItemRepository.delete(item);
    }

    private void verifyTodolistOwnership(UUID todolistId, String username) {
        todolistRepository.findByIdAndUserUsername(todolistId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Todolist not found"));
    }
}
