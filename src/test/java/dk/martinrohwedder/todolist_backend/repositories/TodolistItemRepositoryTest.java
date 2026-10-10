package dk.martinrohwedder.todolist_backend.repositories;

import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.entities.Todolist;
import dk.martinrohwedder.todolist_backend.entities.TodolistItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TodolistItemRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private TodolistItemRepository todolistItemRepository;

    // ******************************************************************
    // Helper Methods
    // ******************************************************************

    private AppUser createUser(String username) {
        return userRepository.save(
                AppUser.builder()
                        .username(username)
                        .password("password")
                        .role("USER")
                        .build()
        );
    }

    private Todolist createTodolist(String title, AppUser user) {
        return todolistRepository.save(
                Todolist.builder()
                        .title(title)
                        .user(user)
                        .build()
        );
    }

    private TodolistItem createItem(String title, Todolist todolist) {
        return TodolistItem.builder()
                .title(title)
                .todolist(todolist)
                .build();
    }

    // ***********************************************************************************************
    // List<TodolistItem> findAllByTodolistIdAndTodolistUserUsername(UUID todolistId, String username)
    // ***********************************************************************************************

    @Test
    void findAllByTodolistIdAndTodolistUserUsername_shouldReturnAllItemsForUser() {
        // Arrange
        AppUser user = createUser("testuser");
        Todolist todolist = createTodolist("todolist", user);

        TodolistItem item1 = createItem("testitem1", todolist);
        TodolistItem item2 = createItem("testitem2", todolist);

        todolistItemRepository.saveAll(List.of(item1, item2));

        // Act
        List<TodolistItem> result = todolistItemRepository.findAllByTodolistIdAndTodolistUserUsername(todolist.getId(), "testuser");

        // Assert
        assertThat(result)
                .hasSize(2)
                .extracting(TodolistItem::getTitle)
                .containsExactlyInAnyOrder("testitem1", "testitem2");
    }

    @Test
    void findAllByTodolistIdAndTodolistUserUsername_shouldReturnEmptyList_whenTodolistHasNoItems() {
        // Arrange
        AppUser user = createUser("testuser");
        Todolist todolist = createTodolist("todolist", user);

        // Act
        List<TodolistItem> result = todolistItemRepository.findAllByTodolistIdAndTodolistUserUsername(todolist.getId(), "testuser");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByTodolistIdAndTodolistUserUsername_shouldReturnEmptyList_whenTodolistBelongsToAnotherUser() {
        // Arrange
        AppUser user1 = createUser("testuser1");
        createUser("testuser2");

        Todolist todolist = createTodolist("user1 todolist", user1);

        TodolistItem item = createItem("testitem", todolist);
        todolistItemRepository.save(item);

        // Act
        List<TodolistItem> result = todolistItemRepository.findAllByTodolistIdAndTodolistUserUsername(todolist.getId(), "testuser2");

        // Assert
        assertThat(result).isEmpty();
    }

    // ***********************************************************************************************
    // Optional<TodolistItem> findByIdAndTodolistUserUsername(UUID id, String username)
    // ***********************************************************************************************

    @Test
    void findByIdAndTodolistUserUsername_shouldReturnItem_whenItemBelongsToUser() {
        // Arrange
        AppUser user = createUser("testuser");
        Todolist todolist = createTodolist("todolist", user);

        TodolistItem item = createItem("testitem", todolist);
        TodolistItem savedItem = todolistItemRepository.save(item);

        // Act
        var result = todolistItemRepository.findByIdAndTodolistUserUsername(savedItem.getId(), "testuser");

        // Assert
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(resultItem -> {
                    assertThat(resultItem.getTitle()).isEqualTo("testitem");
                    assertThat(resultItem.getTodolist().getId()).isEqualTo(todolist.getId());
                });
    }

    @Test
    void findByIdAndTodolistUserUsername_shouldReturnEmpty_whenItemBelongsToAnotherUser() {
        // Arrange
        AppUser user1 = createUser("testuser1");
        createUser("testuser2");

        Todolist todolist = createTodolist("user1 todolist", user1);

        TodolistItem item = createItem("testitem", todolist);
        TodolistItem savedItem = todolistItemRepository.save(item);

        // Act
        var result = todolistItemRepository.findByIdAndTodolistUserUsername(savedItem.getId(), "testuser2");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndTodolistUserUsername_shouldReturnEmpty_whenItemDoesNotExist() {
        // Arrange
        AppUser user = createUser("testuser");

        // Act
        var result = todolistItemRepository.findByIdAndTodolistUserUsername(UUID.randomUUID(), "testuser");

        // Assert
        assertThat(result).isEmpty();
    }
}
