package com.cyboul.eda.userservice;

import com.cyboul.eda.common.dto.UserDTO;
import com.cyboul.eda.common.events.UserCreatedEvent;
import com.cyboul.eda.common.events.UserUpdatedEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final String USER_EVENTS_TOPIC = "user-events";

    private final UserRepository repo;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping
    public List<UserDTO> getAll() {
        return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable String id) {
        return repo.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDTO> getByEmail(@RequestParam @NotBlank @Email String email) {
        return repo.findByEmail(email)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody CreateUserRequest request) {
        if (repo.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .build();
        try {
            User savedUser = repo.save(user);
            kafkaTemplate.send(USER_EVENTS_TOPIC, savedUser.getId(),
                            new UserCreatedEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getName()))
                    .whenComplete((r, ex) -> {
                        if (ex != null) log.error("Failed to publish UserCreatedEvent for user {}", savedUser.getId(), ex);
                    });
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedUser));

        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest request) {
        return repo.findById(id)
                .map(existingUser -> {
                    User updatedUser = existingUser.toBuilder()
                            .name(request.name())
                            .build();
                    User saved = repo.save(updatedUser);
                    kafkaTemplate.send(USER_EVENTS_TOPIC, saved.getId(),
                                    new UserUpdatedEvent(saved.getName()))
                            .whenComplete((r, ex) -> {
                                if (ex != null) log.error("Failed to publish UserUpdatedEvent for user {}", saved.getId(), ex);
                            });
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ----- Mapper methods -----

    private UserDTO toResponse(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getName());
    }

    // ----- Request DTOs -----

    public record CreateUserRequest(
            @NotBlank @Email String email,
            @NotBlank String name,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
    ) {}

    public record UpdateUserRequest(
            @NotBlank String name
    ) {}
}
