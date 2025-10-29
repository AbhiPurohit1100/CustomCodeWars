package com.shodhacode.service;

import com.shodhacode.dto.SubmissionRequest;
import com.shodhacode.model.*;
import com.shodhacode.repo.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class SubmissionService {
    private final ContestRepository contestRepo;
    private final ProblemRepository problemRepo;
    private final UserRepository userRepo;
    private final SubmissionRepository submissionRepo;
    private final JudgeService judgeService;

    public SubmissionService(ContestRepository contestRepo, ProblemRepository problemRepo, UserRepository userRepo, SubmissionRepository submissionRepo, JudgeService judgeService) {
        this.contestRepo = contestRepo; this.problemRepo = problemRepo; this.userRepo = userRepo; this.submissionRepo = submissionRepo; this.judgeService = judgeService;
    }

    @Transactional
    public Submission createAndJudge(SubmissionRequest req) {
        Contest contest = contestRepo.findById(req.getContestId()).orElseThrow();
        var problem = problemRepo.findById(req.getProblemId()).orElseThrow();
        UserAccount user = userRepo.findByUsername(req.getUsername()).orElseGet(() -> {
            UserAccount u = new UserAccount(); u.setUsername(req.getUsername()); return userRepo.save(u);
        });
        Submission s = new Submission();
        s.setContest(contest);
        s.setProblem(problem);
        s.setUser(user);
        s.setSourceCode(req.getSourceCode());
        s.setStatus(SubmissionStatus.PENDING);
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());
        s = submissionRepo.save(s);
        judgeAsync(s.getId());
        return s;
    }

    public Optional<Submission> get(Long id) { return submissionRepo.findById(id); }

    @Async
    @Transactional
    public void judgeAsync(Long submissionId) {
        Submission s = submissionRepo.findById(submissionId).orElseThrow();
        s.setStatus(SubmissionStatus.RUNNING);
        s.setUpdatedAt(Instant.now());
        submissionRepo.save(s);

        JudgeService.JudgeResult res = judgeService.judge(s);
        s.setStatus(res.status);
        s.setMessage(res.message);
        s.setUpdatedAt(Instant.now());
        if (res.status == SubmissionStatus.ACCEPTED && s.getAcceptedAt() == null) {
            s.setAcceptedAt(Instant.now());
        }
        submissionRepo.save(s);
    }
}
