package org.exercise.http.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.exercise.core.dtos.AuthResponse;
import org.exercise.core.interfaces.UserService;
import org.exercise.core.services.UserServiceImpl;
import org.exercise.core.dtos.AuthDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody @Valid AuthDto dto) {
        return ResponseEntity.ok(userService.authenticate(dto.username(), dto.password()));
    }
}
