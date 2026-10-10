
package dk.martinrohwedder.todolist_backend.controllers;

import dk.martinrohwedder.todolist_backend.TestcontainersConfiguration;
import dk.martinrohwedder.todolist_backend.dtos.TodolistItemRequest;
import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.entities.Todolist;
import dk.martinrohwedder.todolist_backend.entities.TodolistItem;
import dk.martinrohwedder.todolist_backend.repositories.TodolistItemRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers
class TodolistItemControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private TodolistItemRepository todolistItemRepository;

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

    private Todolist createTodolist(String username, String title) {
        AppUser user = userRepository.findByUsername(username)
                .orElseGet(() -> createUser(username));

        Todolist todolist = Todolist.builder()
                .title(title)
                .user(user)
                .build();

        return todolistRepository.save(todolist);
    }

    private TodolistItem createItem(Todolist todolist, String title) {
        TodolistItem item = TodolistItem.builder()
                .title(title)
                .isCompleted(false)
                .todolist(todolist)
                .build();

        return todolistItemRepository.save(item);
    }

    @BeforeEach
    void setUp() {
        todolistItemRepository.deleteAll();
        todolistRepository.deleteAll();
        userRepository.deleteAll();
    }

    // GET: /api/todolists/{todolistId}/items

    @Test
    @WithMockUser(username = "testuser")
    void getItems_shouldReturnItems_whenUserOwnsTodolist() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");
        createItem(todolist, "Buy milk");
        createItem(todolist, "Buy bread");

        mockMvc.perform(get("/api/todolists/{todolistId}/items", todolist.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.containsInAnyOrder("Buy milk", "Buy bread")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItems_shouldReturnEmptyList_whenTodolistHasNoItems() throws Exception {
        Todolist todolist = createTodolist("testuser", "Empty list");

        mockMvc.perform(get("/api/todolists/{todolistId}/items", todolist.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItems_shouldReturn404_whenTodolistBelongsToAnotherUser() throws Exception {
        Todolist todolist = createTodolist("anotheruser", "Private list");
        createItem(todolist, "Private item");

        mockMvc.perform(get("/api/todolists/{todolistId}/items", todolist.getId()))
                .andExpect(status().isNotFound());
    }

    // GET: /api/todolists/{todolistId}/items/{itemId}

    @Test
    @WithMockUser(username = "testuser")
    void getItem_shouldReturnItem_whenItemBelongsToUserAndTodolist() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");
        TodolistItem item = createItem(todolist, "Buy milk");

        mockMvc.perform(get("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId().toString()))
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItem_shouldReturn404_whenItemDoesNotExist() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");

        mockMvc.perform(get("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItem_shouldReturn404_whenItemBelongsToAnotherTodolist() throws Exception {
        Todolist requestedTodolist = createTodolist("testuser", "First list");
        Todolist anotherTodolist = createTodolist("testuser", "Second list");
        TodolistItem item = createItem(anotherTodolist, "Item in second list");

        mockMvc.perform(get("/api/todolists/{todolistId}/items/{itemId}", requestedTodolist.getId(), item.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getItem_shouldReturn404_whenTodolistBelongsToAnotherUser() throws Exception {
        Todolist todolist = createTodolist("anotheruser", "Private list");
        TodolistItem item = createItem(todolist, "Private item");

        mockMvc.perform(get("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), item.getId()))
                .andExpect(status().isNotFound());
    }

    // POST: /api/todolists/{todolistId}/items

    @Test
    @WithMockUser(username = "testuser")
    void createItem_shouldReturn201AndCreateItem() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");

        TodolistItemRequest request = new TodolistItemRequest("Buy milk");

        mockMvc.perform(post("/api/todolists/{todolistId}/items", todolist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createItem_shouldReturn400_whenTitleIsInvalid() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");

        TodolistItemRequest request = new TodolistItemRequest("");

        mockMvc.perform(post("/api/todolists/{todolistId}/items", todolist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createItem_shouldReturn400_whenTitleIsLongerThanMaxSizeOf255() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");

        TodolistItemRequest request = new TodolistItemRequest("A".repeat(256));

        mockMvc.perform(post("/api/todolists/{todolistId}/items", todolist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createItem_shouldReturn404_whenTodolistBelongsToAnotherUser() throws Exception {
        Todolist todolist = createTodolist("anotheruser", "Private list");

        TodolistItemRequest request = new TodolistItemRequest("Buy milk");

        mockMvc.perform(post("/api/todolists/{todolistId}/items", todolist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // PUT: /api/todolists/{todolistId}/items/{itemId}

    @Test
    @WithMockUser(username = "testuser")
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");
        TodolistItem item = createItem(todolist, "Old title");

        TodolistItemRequest request = new TodolistItemRequest("New title");

        mockMvc.perform(put("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId().toString()))
                .andExpect(jsonPath("$.title").value("New title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateItem_shouldReturn404_whenItemDoesNotExist() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");

        TodolistItemRequest request = new TodolistItemRequest("New title");

        mockMvc.perform(put("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateItem_shouldReturn400_whenTitleIsInvalid() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");
        TodolistItem item = createItem(todolist, "Old title");

        TodolistItemRequest request = new TodolistItemRequest("");

        mockMvc.perform(put("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateItem_shouldReturn400_whenTitleIsLongerThanMaxSizeOf255() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");
        TodolistItem item = createItem(todolist, "Old title");

        TodolistItemRequest request = new TodolistItemRequest("A".repeat(256));

        mockMvc.perform(put("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // PATCH: /api/todolists/{todolistId}/items/{itemId}/toggleCompleted

    @Test
    @WithMockUser(username = "testuser")
    void toggleCompleted_shouldChangeFalseToTrue() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");
        TodolistItem item = createItem(todolist, "Buy milk");

        mockMvc.perform(patch("/api/todolists/{todolistId}/items/{itemId}/toggleCompleted", todolist.getId(), item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void toggleCompleted_shouldChangeTrueToFalse() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");
        TodolistItem item = createItem(todolist, "Buy milk");
        item.setCompleted(true);
        todolistItemRepository.save(item);

        mockMvc.perform(patch("/api/todolists/{todolistId}/items/{itemId}/toggleCompleted", todolist.getId(), item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @WithMockUser(username = "testuser")
    void toggleCompleted_shouldReturn404_whenItemDoesNotExist() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");

        mockMvc.perform(patch("/api/todolists/{todolistId}/items/{itemId}/toggleCompleted", todolist.getId(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // DELETE: /api/todolists/{todolistId}/items/{itemId}

    @Test
    @WithMockUser(username = "testuser")
    void deleteItem_shouldReturn204AndDeleteItem() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");
        TodolistItem item = createItem(todolist, "Buy milk");

        mockMvc.perform(delete("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), item.getId()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(todolistItemRepository.existsById(item.getId())).isFalse();
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteItem_shouldReturn404_whenItemDoesNotExist() throws Exception {
        Todolist todolist = createTodolist("testuser", "Shopping list");

        mockMvc.perform(delete("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteItem_shouldReturn404_whenItemBelongsToAnotherUser() throws Exception {
        Todolist todolist = createTodolist("anotheruser", "Private list");
        TodolistItem item = createItem(todolist, "Private item");

        mockMvc.perform(delete("/api/todolists/{todolistId}/items/{itemId}", todolist.getId(), item.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItems_shouldReturn401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/todolists/{todolistId}/items", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
