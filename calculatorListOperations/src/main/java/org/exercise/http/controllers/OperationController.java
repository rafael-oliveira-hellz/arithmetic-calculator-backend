package org.exercise.http.controllers;

import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Operation;
import org.exercise.core.interfaces.OperationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OperationController {

    private final OperationService operationService;

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @GetMapping("/operations")
    public ResponseEntity<List<Operation>> getOperations() {
        List<Operation> operations = operationService.getOperations();
        return ResponseEntity.ok(operations);
    }
}
