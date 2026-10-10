package dk.martinrohwedder.todolist_backend.controllers;

import dk.martinrohwedder.todolist_backend.TestcontainersConfiguration;
import dk.martinrohwedder.todolist_backend.dtos.LoginRequest;
import dk.martinrohwedder.todolist_backend.dtos.RegisterRequest;
import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // POST: /api/auth/register

    @Test
    void register_shouldReturn201AndCreateUser() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void register_shouldReturn400_whenUsernameIsTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest("ab", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenPasswordIsTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenUsernameContainsInvalidCharacters() throws Exception {
        RegisterRequest request = new RegisterRequest("test-user", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldRejectDuplicateUsername() throws Exception {
        AppUser existingUser = AppUser.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .role("USER")
                .build();

        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest("testuser", "password456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // POST: /api/auth/login

    @Test
    void login_shouldReturn200AndToken_whenCredentialsAreCorrect() throws Exception {
        AppUser user = AppUser.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .role("USER")
                .build();

        userRepository.save(user);

        LoginRequest request = new LoginRequest("testuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void login_shouldReturn401_whenPasswordIsIncorrect() throws Exception {
        AppUser user = AppUser.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .role("USER")
                .build();

        userRepository.save(user);

        LoginRequest request = new LoginRequest("testuser", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn404_whenUserDoesNotExist() throws Exception {
        LoginRequest request = new LoginRequest("doesnotexist", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}