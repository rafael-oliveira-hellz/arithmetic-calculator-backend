package org.exercise.http.controllers;

import lombok.RequiredArgsConstructor;
import org.exercise.core.interfaces.RecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;
    public record Response(String message) {}

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Response> registerUser(@RequestHeader String accessToken, @PathVariable UUID id) {
        recordService.deleteRecord(accessToken, id);
        return ResponseEntity.ok(new Response("Record with id #" + id + " deleted successfully!"));
    }
}