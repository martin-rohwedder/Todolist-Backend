package dk.martinrohwedder.todolist_backend.controllers;

import dk.martinrohwedder.todolist_backend.TestcontainersConfiguration;
import dk.martinrohwedder.todolist_backend.dtos.TodolistRequest;
import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.entities.Todolist;
import dk.martinrohwedder.todolist_backend.repositories.TodolistRepository;
import dk.martinrohwedder.todolist_backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers
class TodolistControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Test helpers

    private AppUser createUser(String username) {
        return userRepository.save(
                AppUser.builder()
                        .username(username)
                        .password(passwordEncoder.encode("password123"))
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

    @BeforeEach
    void setUp() {
        todolistRepository.deleteAll();
        userRepository.deleteAll();
    }

    // GET: /api/todolists

    @Test
    @WithMockUser(username = "testuser")
    void getTodolists_shouldReturnUsersTodolists() throws Exception {
        AppUser user = createUser("testuser");

        Todolist todolist1 = createTodolist("Shopping list", user);
        Todolist todolist2 = createTodolist("Work tasks", user);

        todolistRepository.save(todolist1);
        todolistRepository.save(todolist2);

        mockMvc.perform(get("/api/todolists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Shopping list"))
                .andExpect(jsonPath("$[1].title").value("Work tasks"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTodolists_shouldReturnEmptyList_whenUserHasNoTodolists() throws Exception {
        createUser("testuser");

        mockMvc.perform(get("/api/todolists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTodolists_shouldNotReturnAnotherUsersTodolists() throws Exception {
        AppUser testUser = createUser("testuser");
        AppUser anotherUser = createUser("anotheruser");

        Todolist usersTodolist = createTodolist("My todolist", testUser);
        Todolist otherUsersTodolist = createTodolist("Other todolist", anotherUser);

        todolistRepository.save(usersTodolist);
        todolistRepository.save(otherUsersTodolist);

        mockMvc.perform(get("/api/todolists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("My todolist"));
    }

    @Test
    void getTodolists_shouldReturn401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/todolists"))
                .andExpect(status().isUnauthorized());
    }

    // GET: /api/todolists/{id}

    @Test
    @WithMockUser(username = "testuser")
    void getTodolist_shouldReturnTodolist_whenUserOwnsIt() throws Exception {
        AppUser user = createUser("testuser");

        Todolist todolist = createTodolist("Shopping list", user);

        todolistRepository.save(todolist);

        mockMvc.perform(get("/api/todolists/{id}", todolist.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todolist.getId().toString()))
                .andExpect(jsonPath("$.title").value("Shopping list"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTodolist_shouldReturn404_whenTodolistDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/api/todolists/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTodolist_shouldReturn404_whenTodolistBelongsToAnotherUser() throws Exception {
        AppUser anotherUser = createUser("anotheruser");

        Todolist todolist = createTodolist("Private list", anotherUser);

        todolistRepository.save(todolist);

        mockMvc.perform(get("/api/todolists/{id}", todolist.getId()))
                .andExpect(status().isNotFound());
    }

    // POST: /api/todolists

    @Test
    @WithMockUser(username = "testuser")
    void createTodolist_shouldCreateTodolistAndReturn201() throws Exception {
        createUser("testuser");

        var request = new TodolistRequest("New todolist");

        mockMvc.perform(post("/api/todolists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New todolist"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createTodolist_shouldReturn400_whenTitleIsInvalid() throws Exception {
        createUser("testuser");

        var request = new TodolistRequest("");

        mockMvc.perform(post("/api/todolists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // PUT: /api/todolists/{id}

    @Test
    @WithMockUser(username = "testuser")
    void updateTodolist_shouldReturnUpdatedTodolist() throws Exception {
        AppUser user = createUser("testuser");

        Todolist todolist = Todolist.builder()
                .title("Old title")
                .user(user)
                .build();

        todolist = todolistRepository.save(todolist);

        TodolistRequest request = new TodolistRequest("New title");

        mockMvc.perform(put("/api/todolists/{id}", todolist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todolist.getId().toString()))
                .andExpect(jsonPath("$.title").value("New title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTodolist_shouldReturn400_whenTitleIsInvalid() throws Exception {
        createUser("testuser");

        TodolistRequest request = new TodolistRequest("");

        mockMvc.perform(put("/api/todolists/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTodolist_shouldReturn400_whenTitleIsLongerThanMaxSizeOf50() throws Exception {
        createUser("testuser");

        // Title max size = 50
        TodolistRequest request = new TodolistRequest("A very long title, which is longer than 50 characters");

        mockMvc.perform(put("/api/todolists/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTodolist_shouldReturn404_whenTodolistDoesNotExist() throws Exception {
        TodolistRequest request = new TodolistRequest("New title");

        mockMvc.perform(put("/api/todolists/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateTodolist_shouldReturn404_whenTodolistBelongsToAnotherUser() throws Exception {
        AppUser anotherUser = createUser("anotheruser");

        Todolist todolist = Todolist.builder()
                .title("Another user's list")
                .user(anotherUser)
                .build();

        todolist = todolistRepository.save(todolist);

        TodolistRequest request = new TodolistRequest("Updated title");

        mockMvc.perform(put("/api/todolists/{id}", todolist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        Todolist storedTodolist = todolistRepository.findById(todolist.getId()).orElseThrow();

        assertThat(storedTodolist.getTitle()).isEqualTo("Another user's list");
    }

    // DELETE: /api/todolists/{id}

    @Test
    @WithMockUser(username = "testuser")
    void deleteTodolist_shouldReturn204_whenTodolistExists() throws Exception {
        AppUser user = createUser("testuser");

        Todolist todolist = createTodolist("Todolist to delete", user);

        todolistRepository.save(todolist);

        mockMvc.perform(delete("/api/todolists/{id}", todolist.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteTodolist_shouldReturn404_whenTodolistDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/todolists/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteTodolist_shouldReturn404_whenTodolistBelongsToAnotherUser() throws Exception {
        AppUser anotherUser = createUser("anotheruser");

        Todolist todolist = createTodolist("Private list", anotherUser);

        todolistRepository.save(todolist);

        mockMvc.perform(delete("/api/todolists/{id}", todolist.getId()))
                .andExpect(status().isNotFound());
    }
}