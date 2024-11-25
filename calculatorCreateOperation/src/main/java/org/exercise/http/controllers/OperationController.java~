package org.exercise.http.controllers;

import lombok.RequiredArgsConstructor;
import org.exercise.core.dtos.Values;
import org.exercise.core.entities.Record;
import org.exercise.core.interfaces.OperationService;
import org.exercise.core.services.OperationServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OperationController {

    private final OperationService operationService;

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @PostMapping("/operations/{type}")
    public ResponseEntity<Record> registerUser(@RequestHeader String accessToken, @PathVariable String type, @RequestBody Values values) {
        Record responseRecord = operationService.doOperation(accessToken, type, values.value1(), values.value2());
        return ResponseEntity.ok(responseRecord);
    }
}
