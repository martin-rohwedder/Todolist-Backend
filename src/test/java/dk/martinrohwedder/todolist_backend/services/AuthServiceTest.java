package dk.martinrohwedder.todolist_backend.services;

import dk.martinrohwedder.todolist_backend.dtos.LoginRequest;
import dk.martinrohwedder.todolist_backend.dtos.RegisterRequest;
import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.exceptions.UsernameAlreadyExistsException;
import dk.martinrohwedder.todolist_backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    // *********************************************************
    // login()
    // *********************************************************

    @Test
    void login_shouldReturnLoginResponse_whenAuthenticationSucceeds() {
        // Arrange
        LoginRequest request = new LoginRequest(
                "testuser",
                "password"
        );

        UserDetails user = User.builder()
                .username("testuser")
                .password("encoded-password")
                .roles("USER")
                .build();

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn("test-jwt-token");

        // Act
        var result = authService.login(request);

        // Assert
        assertThat(result.accessToken())
                .isEqualTo("test-jwt-token");

        verify(authenticationManager).authenticate(
                argThat(token ->
                        token.getName().equals("testuser")
                                && token.getCredentials().equals("password")
                )
        );

        verify(jwtService).generateToken(user);
    }

    @Test
    void login_shouldPropagateException_whenAuthenticationFails() {
        // Arrange
        LoginRequest request = new LoginRequest(
                "testuser",
                "wrong-password"
        );

        RuntimeException exception =
                new RuntimeException("Authentication failed");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(exception);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isSameAs(exception);

        verify(jwtService, never()).generateToken(any());
    }

    // *********************************************************
    // register()
    // *********************************************************

    @Test
    void register_shouldCreateUserAndReturnToken_whenUsernameDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();

        RegisterRequest request = new RegisterRequest(
                "testuser",
                "password"
        );

        AppUser savedUser = AppUser.builder()
                .id(userId)
                .username("testuser")
                .password("encoded-password")
                .role("USER")
                .build();

        when(userRepository.existsByUsername("testuser"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(AppUser.class)))
                .thenReturn(savedUser);

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("test-jwt-token");

        // Act
        var result = authService.register(request);

        // Assert
        assertThat(result.id())
                .isEqualTo(userId);

        assertThat(result.username())
                .isEqualTo("testuser");

        assertThat(result.accessToken())
                .isEqualTo("test-jwt-token");

        ArgumentCaptor<AppUser> userCaptor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(userRepository).save(userCaptor.capture());

        AppUser savedArgument = userCaptor.getValue();

        assertThat(savedArgument.getUsername())
                .isEqualTo("testuser");

        assertThat(savedArgument.getPassword())
                .isEqualTo("encoded-password");

        assertThat(savedArgument.getRole())
                .isEqualTo("USER");

        verify(passwordEncoder)
                .encode("password");

        verify(jwtService)
                .generateToken(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenUsernameAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "testuser",
                "password"
        );

        when(userRepository.existsByUsername("testuser"))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("The username: 'testuser' already exists");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
        verify(jwtService, never()).generateToken(any());
    }
}