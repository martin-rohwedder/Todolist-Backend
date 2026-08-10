package dk.martinrohwedder.todolist_backend.services;

import dk.martinrohwedder.todolist_backend.dtos.LoginRequest;
import dk.martinrohwedder.todolist_backend.dtos.LoginResponse;
import dk.martinrohwedder.todolist_backend.dtos.RegisterRequest;
import dk.martinrohwedder.todolist_backend.dtos.RegisterResponse;
import dk.martinrohwedder.todolist_backend.entities.AppUser;
import dk.martinrohwedder.todolist_backend.exceptions.UsernameAlreadyExistsException;
import dk.martinrohwedder.todolist_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()));

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        AppUser user = AppUser.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role("USER")
                .build();

        var savedUser = userRepository.save(user);
        String token = jwtService.generateToken(toUserDetails(savedUser));

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), token);
    }

    private UserDetails toUserDetails(AppUser user) {
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}