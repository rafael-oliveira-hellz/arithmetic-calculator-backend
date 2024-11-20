package org.exercise.http.controllers;

import lombok.RequiredArgsConstructor;
import org.exercise.core.interfaces.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LogoutController {

    private final UserService userService;
    public record Response(String message) {}

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> authenticateUser(@RequestHeader String accessToken) {
        userService.logoutUser(accessToken);
        return ResponseEntity.ok(new Response("Logout successful!"));
    }
}
