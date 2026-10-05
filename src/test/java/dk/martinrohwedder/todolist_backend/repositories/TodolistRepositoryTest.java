package dk.martinrohwedder.todolist_backend.repositories;

import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.entities.Todolist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TodolistRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private UserRepository userRepository;

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
        return Todolist.builder()
                .title(title)
                .user(user)
                .build();
    }

    // ******************************************************************
    // List<Todolist> findAllByUserUsername(String username)
    // ******************************************************************

    @Test
    void findAllByUserUsername_shouldReturnAllTodolistsForUser() {
        // Arrange
        AppUser user = createUser("testuser");

        Todolist todolist1 = createTodolist("todolist1", user);
        Todolist todolist2 = createTodolist("todolist2", user);

        todolistRepository.saveAll(List.of(todolist1, todolist2));

        // Act
        List<Todolist> result = todolistRepository.findAllByUserUsername(user.getUsername());

        // Assert
        assertThat(result)
                .hasSize(2)
                .extracting(Todolist::getTitle)
                .containsExactlyInAnyOrder("todolist1", "todolist2");
    }

    @Test
    void findAllByUserUsername_shouldReturnEmptyList_whenUserHadNoTodolists() {
        // Arrange & Act
        List<Todolist> result = todolistRepository.findAllByUserUsername("testuser");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByUserUsername_shouldOnlyReturnTodolistsBelongingToUser() {
        // Arrange
        AppUser user1 = createUser("testuser1");
        AppUser user2 = createUser("testuser2");

        Todolist user1Todolist = createTodolist("user1 todolist", user1);
        Todolist user2Todolist = createTodolist("user2 todolist", user2);

        todolistRepository.saveAll(List.of(user1Todolist, user2Todolist));

        // Act
        List<Todolist> result = todolistRepository.findAllByUserUsername("testuser1");

        // Assert
        assertThat(result)
                .hasSize(1)
                .extracting(Todolist::getTitle)
                .containsExactly("user1 todolist");
    }

    // **********************************************************************
    // Optional<Todolist> findByIdAndUserUsername(UUID uuid, String username)
    // **********************************************************************

    @Test
    void findByIdAndUserUsername_shouldReturnTodolist_whenTodolistsBelongsToUser() {
        // Arrange
        AppUser user = createUser("testuser");

        Todolist todolist = createTodolist("todolist", user);
        Todolist savedTodolist = todolistRepository.save(todolist);

        // Act
        var result = todolistRepository.findByIdAndUserUsername(savedTodolist.getId(), "testuser");

        // Assert
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(resultTodolist -> {
                    assertThat(resultTodolist.getTitle()).isEqualTo(savedTodolist.getTitle());
                    assertThat(resultTodolist.getUser().getUsername()).isEqualTo(savedTodolist.getUser().getUsername());
                });
    }

    @Test
    void findByIdAndUserUsername_shouldReturnEmpty_whenTodolistBelongsToAnotherUser() {
        // Arrange
        AppUser user = createUser("testuser1");
        createUser("testuser2");

        Todolist todolist = createTodolist("user1 todolist", user);
        Todolist savedTodolist = todolistRepository.save(todolist);

        // Act
        var result = todolistRepository.findByIdAndUserUsername(savedTodolist.getId(), "testuser2");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndUserUsername_shouldReturnEmpty_whenTodolistDoesNotExist() {
        // Arrange & Act
        var result = todolistRepository.findByIdAndUserUsername(UUID.randomUUID(), "testuser");

        // Assert
        assertThat(result).isEmpty();
    }
}
