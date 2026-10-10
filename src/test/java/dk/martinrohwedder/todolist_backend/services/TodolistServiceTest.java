package dk.martinrohwedder.todolist_backend.services;

import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.entities.Todolist;
import dk.martinrohwedder.todolist_backend.exceptions.ResourceNotFoundException;
import dk.martinrohwedder.todolist_backend.repositories.TodolistRepository;
import dk.martinrohwedder.todolist_backend.repositories.UserRepository;
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
class TodolistServiceTest {
    @Mock
    private TodolistRepository todolistRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodolistService todolistService;

    // *********************************************************
    // getTodolists()
    // *********************************************************

    @Test
    void getTodolists_shouldReturnTodolistsForUser() {
        Todolist todolist1 = Todolist.builder()
                .title("Todolist 1")
                .build();

        Todolist todolist2 = Todolist.builder()
                .title("Todolist 2")
                .build();

        when(todolistRepository.findAllByUserUsername("testuser"))
                .thenReturn(List.of(todolist1, todolist2));

        List<Todolist> result = todolistService.getTodolists("testuser");

        assertThat(result)
                .hasSize(2)
                .containsExactly(todolist1, todolist2);

        verify(todolistRepository).findAllByUserUsername("testuser");
    }

    @Test
    void getTodolists_shouldReturnEmptyList_whenUserHasNoTodolists() {
        when(todolistRepository.findAllByUserUsername("testuser"))
                .thenReturn(List.of());

        List<Todolist> result = todolistService.getTodolists("testuser");

        assertThat(result).isEmpty();

        verify(todolistRepository).findAllByUserUsername("testuser");
    }

    // *********************************************************
    // getTodolist()
    // *********************************************************

    @Test
    void getTodolist_shouldReturnTodolist_whenTodolistExistsForUser() {
        UUID todolistId = UUID.randomUUID();

        Todolist todolist = Todolist.builder()
                .id(todolistId)
                .title("My Todolist")
                .build();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        Todolist result = todolistService.getTodolist(todolistId, "testuser");

        assertThat(result).isSameAs(todolist);
        assertThat(result.getId()).isEqualTo(todolistId);
        assertThat(result.getTitle()).isEqualTo("My Todolist");

        verify(todolistRepository).findByIdAndUserUsername(todolistId, "testuser");
    }

    @Test
    void getTodolist_shouldThrowException_whenTodolistDoesNotExistForUser() {
        UUID todolistId = UUID.randomUUID();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                todolistService.getTodolist(todolistId, "testuser")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist not found");

        verify(todolistRepository).findByIdAndUserUsername(todolistId, "testuser");
    }

    // *********************************************************
    // createTodolist()
    // *********************************************************

    @Test
    void createTodolist_shouldCreateAndReturnTodolist_whenUserExists() {
        AppUser user = AppUser.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("encoded-password")
                .role("USER")
                .build();

        Todolist savedTodolist = Todolist.builder()
                .id(UUID.randomUUID())
                .title("My Todolist")
                .user(user)
                .build();

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(todolistRepository.save(any(Todolist.class)))
                .thenReturn(savedTodolist);

        Todolist result = todolistService.createTodolist("testuser", "My Todolist");

        assertThat(result).isSameAs(savedTodolist);

        ArgumentCaptor<Todolist> captor = ArgumentCaptor.forClass(Todolist.class);

        verify(todolistRepository).save(captor.capture());

        Todolist savedArgument = captor.getValue();

        assertThat(savedArgument.getTitle()).isEqualTo("My Todolist");
        assertThat(savedArgument.getUser()).isSameAs(user);

        verify(userRepository).findByUsername("testuser");
        verify(todolistRepository).save(any(Todolist.class));
    }

    @Test
    void createTodolist_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                todolistService.createTodolist("testuser", "My Todolist")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUsername("testuser");
        verify(todolistRepository, never()).save(any(Todolist.class));
    }

    // *********************************************************
    // updateTodolist()
    // *********************************************************

    @Test
    void updateTodolist_shouldUpdateTitleAndSaveTodolist() {
        UUID todolistId = UUID.randomUUID();

        AppUser user = AppUser.builder()
                .username("testuser")
                .role("USER")
                .build();

        Todolist todolist = Todolist.builder()
                .id(todolistId)
                .title("Old title")
                .user(user)
                .build();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        when(todolistRepository.save(todolist))
                .thenReturn(todolist);

        Todolist result = todolistService.updateTodolist(todolistId, "testuser", "New title");

        assertThat(result).isSameAs(todolist);
        assertThat(result.getTitle()).isEqualTo("New title");

        verify(todolistRepository).findByIdAndUserUsername(todolistId, "testuser");
        verify(todolistRepository).save(todolist);
    }

    @Test
    void updateTodolist_shouldThrowException_whenTodolistDoesNotExistForUser() {
        UUID todolistId = UUID.randomUUID();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> todolistService.updateTodolist(todolistId, "testuser", "New title"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist not found");

        verify(todolistRepository, never()).save(any(Todolist.class));
    }

    // *********************************************************
    // deleteTodolist()
    // *********************************************************

    @Test
    void deleteTodolist_shouldDeleteTodolist_whenTodolistExistsForUser() {
        UUID todolistId = UUID.randomUUID();

        Todolist todolist = Todolist.builder()
                .id(todolistId)
                .title("My Todolist")
                .build();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.of(todolist));

        todolistService.deleteTodolist(todolistId, "testuser");

        verify(todolistRepository).findByIdAndUserUsername(todolistId, "testuser");
        verify(todolistRepository).delete(todolist);
    }

    @Test
    void deleteTodolist_shouldThrowException_whenTodolistDoesNotExistForUser() {
        UUID todolistId = UUID.randomUUID();

        when(todolistRepository.findByIdAndUserUsername(todolistId, "testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                todolistService.deleteTodolist(todolistId, "testuser")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todolist not found");

        verify(todolistRepository).findByIdAndUserUsername(todolistId, "testuser");
        verify(todolistRepository, never()).delete(any(Todolist.class));
    }
}