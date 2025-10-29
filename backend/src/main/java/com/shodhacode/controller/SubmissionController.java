package com.shodhacode.controller;

import com.shodhacode.dto.SubmissionRequest;
import com.shodhacode.dto.SubmissionResponse;
import com.shodhacode.model.Submission;
import com.shodhacode.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(@Valid @RequestBody SubmissionRequest request) {
        Submission s = submissionService.createAndJudge(request);
        return ResponseEntity.ok(new SubmissionResponse(
                s.getId(), s.getStatus(), s.getMessage(), s.getCreatedAt(), s.getUpdatedAt()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> get(@PathVariable("id") Long id) {
        Optional<Submission> s = submissionService.get(id);
        if (s.isEmpty()) return ResponseEntity.notFound().build();
        Submission sub = s.get();
        return ResponseEntity.ok(new SubmissionResponse(
                sub.getId(), sub.getStatus(), sub.getMessage(), sub.getCreatedAt(), sub.getUpdatedAt()
        ));
    }
}
