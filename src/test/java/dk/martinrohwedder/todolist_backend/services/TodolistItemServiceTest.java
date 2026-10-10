package dk.martinrohwedder.todolist_backend.services;

import dk.martinrohwedder.todolist_backend.entities.Todolist;
import dk.martinrohwedder.todolist_backend.entities.TodolistItem;
import dk.martinrohwedder.todolist_backend.exceptions.ResourceNotFoundException;
import dk.martinrohwedder.todolist_backend.repositories.TodolistItemRepository;
import dk.martinrohwedder.todolist_backend.repositories.TodolistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodolistItemServiceTest {
    @Mock
    private TodolistItemRepository todolistItemRepository;

    @Mock
    private TodolistRepository todolistRepository;

    @InjectMocks
    private TodolistItemService todolistItemService;

    // *********************************************************
    // Helper Methods
    // *********************************************************

    private Todolist createTodolist(UUID id) {
        return Todolist.builder()
                .id(id)
                .title("Test todolist")
                .build();
    }

    private TodolistItem createItem(UUID id, String title, Todolist todolist) {
        return TodolistItem.builder()
                .id(id)
                .title(title)
                .todolist(todolist)
                .build();
    }

    // *********************************************************
    // getItems()
    // *********************************************************

    @Test
    void getItems_shouldReturnItems_whenUserOwnsTodolist() {
        UUID todolistId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        TodolistItem item1 = createItem(
                UUID.randomUUID(),
                "Item 1",
                todolist
        );

        TodolistItem item2 = createItem(
                UUID.randomUUID(),
                "Item 2",
                todolist
        );

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findAllByTodolistIdAndTodolistUserUsername(todolistId, "testuser"))
                .thenReturn(List.of(item1, item2));

        List<TodolistItem> result = todolistItemService.getItems(todolistId, "testuser");

        assertThat(result)
                .hasSize(2)
                .containsExactly(item1, item2);

        verify(todolistRepository).findByIdAndUserUsername(todolistId, "testuser");
        verify(todolistItemRepository).findAllByTodolistIdAndTodolistUserUsername(todolistId, "testuser");
    }

    @Test
    void getItems_shouldReturnEmptyList_whenTodolistHasNoItems() {
        UUID todolistId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findAllByTodolistIdAndTodolistUserUsername(todolistId, "testuser"))
                .thenReturn(List.of());

        List<TodolistItem> result = todolistItemService.getItems(todolistId, "testuser");

        assertThat(result).isEmpty();
    }

    @Test
    void getItems_shouldThrowException_whenUserDoesNotOwnTodolist() {
        UUID todolistId = UUID.randomUUID();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                todolistItemService.getItems(todolistId, "testuser")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist not found");

        verify(todolistItemRepository, never())
                .findAllByTodolistIdAndTodolistUserUsername(
                        any(),
                        any()
                );
    }

    // *********************************************************
    // getItem()
    // *********************************************************

    @Test
    void getItem_shouldReturnItem_whenItemBelongsToTodolistAndUser() {
        UUID todolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);
        TodolistItem item = createItem(
                itemId,
                "Test item",
                todolist
        );

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findByIdAndTodolistUserUsername(itemId, "testuser"))
                .thenReturn(Optional.of(item));

        TodolistItem result = todolistItemService.getItem(todolistId, itemId, "testuser");

        assertThat(result).isSameAs(item);
        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getTitle()).isEqualTo("Test item");
    }

    @Test
    void getItem_shouldThrowException_whenTodolistDoesNotBelongToUser() {
        UUID todolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> todolistItemService.getItem(todolistId, itemId, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist not found");

        verify(todolistItemRepository, never()).findByIdAndTodolistUserUsername(any(), any());
    }

    @Test
    void getItem_shouldThrowException_whenItemDoesNotExist() {
        UUID todolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findByIdAndTodolistUserUsername(itemId, "testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> todolistItemService.getItem(todolistId, itemId, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist item not found");
    }

    @Test
    void getItem_shouldThrowException_whenItemBelongsToAnotherTodolist() {
        UUID todolistId = UUID.randomUUID();
        UUID anotherTodolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Todolist requestedTodolist = createTodolist(todolistId);
        Todolist anotherTodolist = createTodolist(anotherTodolistId);

        TodolistItem item = createItem(
                itemId,
                "Test item",
                anotherTodolist
        );

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(requestedTodolist));

        when(todolistItemRepository.findByIdAndTodolistUserUsername(itemId, "testuser"))
                .thenReturn(Optional.of(item));

        assertThatThrownBy(() -> todolistItemService.getItem(todolistId, itemId, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist item not found");
    }

    // *********************************************************
    // createItem()
    // *********************************************************

    @Test
    void createItem_shouldCreateAndReturnItem_whenTodolistExistsForUser() {
        UUID todolistId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        TodolistItem savedItem = createItem(
                UUID.randomUUID(),
                "Test item",
                todolist
        );

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.save(any(TodolistItem.class)))
                .thenReturn(savedItem);

        TodolistItem result = todolistItemService.createItem(todolistId, "testuser", "Test item");

        assertThat(result).isSameAs(savedItem);

        ArgumentCaptor<TodolistItem> captor = ArgumentCaptor.forClass(TodolistItem.class);

        verify(todolistItemRepository).save(captor.capture());

        TodolistItem savedArgument = captor.getValue();

        assertThat(savedArgument.getTitle())
                .isEqualTo("Test item");

        assertThat(savedArgument.getTodolist())
                .isSameAs(todolist);
    }

    @Test
    void createItem_shouldThrowException_whenTodolistDoesNotExistForUser() {
        UUID todolistId = UUID.randomUUID();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> todolistItemService.createItem(todolistId, "testuser", "Test item"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist not found");

        verify(todolistItemRepository, never())
                .save(any(TodolistItem.class));
    }

    // *********************************************************
    // updateItem()
    // *********************************************************

    @Test
    void updateItem_shouldUpdateTitleAndSaveItem() {
        UUID todolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        TodolistItem item = createItem(
                itemId,
                "Old title",
                todolist
        );

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findByIdAndTodolistUserUsername(itemId, "testuser"))
                .thenReturn(Optional.of(item));

        when(todolistItemRepository.save(item))
                .thenReturn(item);

        TodolistItem result = todolistItemService.updateItem(todolistId, itemId, "testuser", "New title");

        assertThat(result).isSameAs(item);
        assertThat(result.getTitle()).isEqualTo("New title");

        verify(todolistItemRepository).save(item);
    }

    // *********************************************************
    // toogleCompleted()
    // *********************************************************

    @Test
    void toggleCompleted_shouldToggleFromFalseToTrue() {
        UUID todolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        TodolistItem item = createItem(
                itemId,
                "Test item",
                todolist
        );

        assertThat(item.isCompleted()).isFalse();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findByIdAndTodolistUserUsername(itemId, "testuser"))
                .thenReturn(Optional.of(item));

        when(todolistItemRepository.save(item))
                .thenReturn(item);

        TodolistItem result = todolistItemService.toggleCompleted(todolistId, itemId, "testuser");

        assertThat(result.isCompleted()).isTrue();

        verify(todolistItemRepository).save(item);
    }

    @Test
    void toggleCompleted_shouldToggleFromTrueToFalse() {
        UUID todolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        TodolistItem item = createItem(
                itemId,
                "Test item",
                todolist
        );

        item.setCompleted(true);

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findByIdAndTodolistUserUsername(itemId, "testuser"))
                .thenReturn(Optional.of(item));

        when(todolistItemRepository.save(item))
                .thenReturn(item);

        TodolistItem result = todolistItemService.toggleCompleted(todolistId, itemId, "testuser");

        assertThat(result.isCompleted()).isFalse();

        verify(todolistItemRepository).save(item);
    }

    // *********************************************************
    // deleteItem()
    // *********************************************************

    @Test
    void deleteItem_shouldDeleteItem_whenItemExists() {
        UUID todolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        TodolistItem item = createItem(
                itemId,
                "Test item",
                todolist
        );

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findByIdAndTodolistUserUsername(itemId, "testuser"))
                .thenReturn(Optional.of(item));

        todolistItemService.deleteItem(todolistId, itemId, "testuser");

        verify(todolistItemRepository).delete(item);
    }

    @Test
    void deleteItem_shouldNotDeleteItem_whenItemDoesNotExist() {
        UUID todolistId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Todolist todolist = createTodolist(todolistId);

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistItemRepository.findByIdAndTodolistUserUsername(itemId, "testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> todolistItemService.deleteItem(todolistId, itemId, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist item not found");

        verify(todolistItemRepository, never())
                .delete(any(TodolistItem.class));
    }
}