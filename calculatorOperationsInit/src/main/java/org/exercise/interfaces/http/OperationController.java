package org.exercise.interfaces.http;

import lombok.RequiredArgsConstructor;
import org.exercise.domain.services.InitDatabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OperationController {

    private final InitDatabaseService initDatabaseService;

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @PostMapping("/operations/init")
    public ResponseEntity<String> registerUser() {
        initDatabaseService.initDb();
        return ResponseEntity.ok("success");
    }
}
