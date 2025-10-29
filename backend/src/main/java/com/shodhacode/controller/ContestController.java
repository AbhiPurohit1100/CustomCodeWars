package com.shodhacode.controller;

import com.shodhacode.dto.ContestResponse;
import com.shodhacode.dto.ContestSummary;
import com.shodhacode.dto.LeaderboardEntry;
import com.shodhacode.dto.ProblemDto;
import com.shodhacode.model.Contest;
import com.shodhacode.model.Problem;
import com.shodhacode.model.Submission;
import com.shodhacode.model.SubmissionStatus;
import com.shodhacode.repo.ContestRepository;
import com.shodhacode.repo.ProblemRepository;
import com.shodhacode.repo.SubmissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestRepository contestRepo;
    private final ProblemRepository problemRepo;
    private final SubmissionRepository submissionRepo;

    public ContestController(ContestRepository contestRepo, ProblemRepository problemRepo, SubmissionRepository submissionRepo) {
        this.contestRepo = contestRepo; this.problemRepo = problemRepo; this.submissionRepo = submissionRepo;
    }

    @GetMapping
    public ResponseEntity<?> listContests() {
        List<ContestSummary> list = contestRepo.findAll().stream()
                .sorted(Comparator.comparing(Contest::getId))
                .map(c -> new ContestSummary(c.getId(), c.getTitle()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{contestId}")
    public ResponseEntity<?> getContest(@PathVariable("contestId") Long contestId) {
        Optional<Contest> c = contestRepo.findById(contestId);
        if (c.isEmpty()) return ResponseEntity.notFound().build();
        List<Problem> problems = problemRepo.findByContestId(contestId);
        List<ProblemDto> problemDtos = problems.stream()
            .map(p -> new ProblemDto(p.getId(), p.getTitle(), p.getStatement()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(new ContestResponse(c.get().getId(), c.get().getTitle(), problemDtos));
    }

    @GetMapping("/{contestId}/leaderboard")
    public ResponseEntity<?> leaderboard(@PathVariable("contestId") Long contestId) {
        List<Submission> subs = submissionRepo.findByContestId(contestId);
        Map<String, Map<Long, Instant>> acceptedByUser = new HashMap<>();
        for (Submission s : subs) {
            if (s.getStatus() == SubmissionStatus.ACCEPTED) {
                acceptedByUser.computeIfAbsent(s.getUser().getUsername(), k -> new HashMap<>())
                    .merge(s.getProblem().getId(), s.getAcceptedAt(), (oldV, newV) -> {
                        if (oldV == null) return newV; return oldV.isBefore(newV) ? oldV : newV;
                    });
            }
        }
        List<LeaderboardEntry> entries = acceptedByUser.entrySet().stream()
            .map(e -> {
                int solved = e.getValue().size();
                Instant lastAccepted = e.getValue().values().stream().filter(Objects::nonNull).max(Instant::compareTo).orElse(null);
                return new LeaderboardEntry(e.getKey(), solved, lastAccepted);
            })
            .sorted(Comparator.comparingInt((LeaderboardEntry le) -> -le.solved)
                .thenComparing(le -> le.lastAcceptedAt == null ? Instant.EPOCH : le.lastAcceptedAt))
            .collect(Collectors.toList());
        return ResponseEntity.ok(entries);
    }
}
