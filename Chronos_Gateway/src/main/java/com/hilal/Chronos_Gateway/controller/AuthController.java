package com.hilal.Chronos_Gateway.controller;

import com.hilal.Chronos_Gateway.model.User;
import com.hilal.Chronos_Gateway.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:8085")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public Mono<User> signup(@RequestBody SignupRequest request) {
        return userRepository.findByUsername(request.username())
                .flatMap(existing -> Mono.<User>error(new RuntimeException("User already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User();
                    newUser.setUsername(request.username());
                    // Hash the password before saving!
                    newUser.setPassword(passwordEncoder.encode(request.password()));
                    // Default role
                    newUser.setRoles("ROLE_USER");
                    return userRepository.save(newUser);
                }));
    }

    // DTO for the request
    public record SignupRequest(String username, String password, String name) {}
}