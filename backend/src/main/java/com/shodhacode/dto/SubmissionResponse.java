package com.shodhacode.dto;

import com.shodhacode.model.SubmissionStatus;

import java.time.Instant;

public class SubmissionResponse {
    private Long id;
    private SubmissionStatus status;
    private String message;
    private Instant createdAt;
    private Instant updatedAt;

    public SubmissionResponse(Long id, SubmissionStatus status, String message, Instant createdAt, Instant updatedAt) {
        this.id = id; this.status = status; this.message = message; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public SubmissionStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
