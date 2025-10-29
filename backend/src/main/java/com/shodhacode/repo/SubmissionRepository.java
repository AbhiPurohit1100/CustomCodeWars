package com.shodhacode.repo;

import com.shodhacode.model.Submission;
import com.shodhacode.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByContestId(Long contestId);
    List<Submission> findByContestIdAndUserUsername(Long contestId, String username);
    List<Submission> findByContestIdAndProblemIdAndUserUsernameAndStatus(Long contestId, Long problemId, String username, SubmissionStatus status);
    List<Submission> findByContestIdAndStatus(Long contestId, SubmissionStatus status);
    List<Submission> findByContestIdAndStatusAndAcceptedAtIsNotNull(Long contestId, SubmissionStatus status);
}
