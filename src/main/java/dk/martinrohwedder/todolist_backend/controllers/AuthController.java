package dk.martinrohwedder.todolist_backend.controllers;

import dk.martinrohwedder.todolist_backend.dtos.LoginRequest;
import dk.martinrohwedder.todolist_backend.dtos.LoginResponse;
import dk.martinrohwedder.todolist_backend.dtos.RegisterRequest;
import dk.martinrohwedder.todolist_backend.dtos.RegisterResponse;
import dk.martinrohwedder.todolist_backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var response = authService.register(request);
        var uriLocation = UriComponentsBuilder.fromPath("/api/users/{id}").buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(uriLocation).body(response);
    }
}
