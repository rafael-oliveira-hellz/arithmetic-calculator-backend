package org.exercise.http.controllers;

import lombok.RequiredArgsConstructor;
import org.exercise.core.interfaces.RecordService;
import org.exercise.core.services.RecordServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<String> registerUser(@RequestHeader String accessToken, @PathVariable UUID id) {
        recordService.deleteRecord(accessToken, id);
        return ResponseEntity.ok("Record with id #" + id + " deleted successfully!");
    }
}
