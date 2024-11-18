package org.exercise.http.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.exercise.core.dtos.Register;
import org.exercise.core.interfaces.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid Register dto) {
            userService.registerUser(dto);
            return new ResponseEntity<>("User created successfully!", HttpStatus.CREATED);
    }
}
