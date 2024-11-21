package org.exercise.http.controllers;

import lombok.RequiredArgsConstructor;
import org.exercise.core.entities.Record;
import org.exercise.core.interfaces.RecordService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("Status ok");
    }

    @GetMapping("/records")
    public ResponseEntity<Page<Record>> getRecords(@RequestHeader String accessToken,
                                                   @RequestParam(defaultValue = "0") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size,
                                                    @RequestParam(defaultValue = "data") String orderedBy) {
        Page<Record> records = recordService.getRecords(accessToken, page, size, orderedBy);
        return ResponseEntity.ok(records);
    }
}
