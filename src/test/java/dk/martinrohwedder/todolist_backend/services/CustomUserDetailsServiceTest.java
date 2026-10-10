package dk.martinrohwedder.todolist_backend.services;

import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    // *********************************************************
    // loadUserByUsername()
    // *********************************************************

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        AppUser user = AppUser.builder()
                .username("testuser")
                .password("encoded-password")
                .role("USER")
                .build();

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("testuser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("The username 'testuser' was not found");

        verify(userRepository).findByUsername("testuser");
    }
}