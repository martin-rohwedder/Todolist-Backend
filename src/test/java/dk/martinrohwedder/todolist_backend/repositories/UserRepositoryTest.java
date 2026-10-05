package dk.martinrohwedder.todolist_backend.repositories;

import dk.martinrohwedder.todolist_backend.entities.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    // *********************************************************
    // Helper Method
    // *********************************************************

    private AppUser createUser(String username) {
        return AppUser.builder()
                .username(username)
                .password("password")
                .role("USER")
                .build();
    }

    // *********************************************************
    // Optional<AppUser> findByUsername(String username)
    // *********************************************************

    @Test
    void findByUsername_shouldReturnUser_whenUsernameExists() {
        // Arrange
        AppUser expected = createUser("testuser");

        userRepository.save(expected);

        // Act
        var result = userRepository.findByUsername("testuser");

        // Assert
        assertThat(result)
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(expected.getUsername());
                    assertThat(user.getPassword()).isEqualTo(expected.getPassword());
                    assertThat(user.getRole()).isEqualTo(expected.getRole());
                });
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUsernameDoesNotExist() {
        // Arrange & Act
        var result = userRepository.findByUsername("testuser");

        // Assert
        assertThat(result).isEmpty();
    }

    // *********************************************************
    // boolean existsByUsername(String username)
    // *********************************************************

    @Test
    void existsByUsername_shouldReturnTrue_whenUsernameExists() {
        // Arrange
        AppUser expected = createUser("testuser");

        userRepository.save(expected);

        // Act
        boolean result = userRepository.existsByUsername("testuser");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenUsernameDoesNotExist() {
        // Arrange & Act
        var result = userRepository.existsByUsername("testuser");

        // Assert
        assertThat(result).isFalse();
    }

    // *********************************************************
    // save() should fail when username already exists
    // *********************************************************

    @Test
    void save_shouldFail_whenUsernameAlreadyExists() {
        // Arrange
        AppUser firstUser = createUser("testuser");
        AppUser secondUser = createUser("testuser");

        userRepository.saveAndFlush(firstUser);

        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(secondUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
