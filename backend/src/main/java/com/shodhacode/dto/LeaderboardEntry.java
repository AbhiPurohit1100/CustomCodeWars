package com.shodhacode.dto;

import java.time.Instant;

public class LeaderboardEntry {
    public String username;
    public int solved;
    public Instant lastAcceptedAt;

    public LeaderboardEntry(String username, int solved, Instant lastAcceptedAt) {
        this.username = username; this.solved = solved; this.lastAcceptedAt = lastAcceptedAt;
    }
}
