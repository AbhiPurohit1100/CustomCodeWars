package com.shodhacode.controller;

import com.shodhacode.dto.*;
import com.shodhacode.model.*;
import com.shodhacode.repo.ContestRepository;
import com.shodhacode.repo.ProblemRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ContestRepository contestRepo;
    private final ProblemRepository problemRepo;
    public AdminController(ContestRepository contestRepo, ProblemRepository problemRepo) {
        this.contestRepo = contestRepo; this.problemRepo = problemRepo; }

    @PostMapping("/seed/contest2")
    public ResponseEntity<?> seedContest2() {
        String title = "Algorithms Warmup";
        if (contestRepo.findByTitle(title).isPresent()) {
            return ResponseEntity.ok("Contest already present");
        }
        Contest c2 = new Contest();
        c2.setTitle(title);

        Problem m1 = new Problem();
        m1.setTitle("A * B");
        m1.setStatement("Read two integers and output their product.");
        m1.setContest(c2);
        ProblemTestCase m1t1 = new ProblemTestCase(); m1t1.setProblem(m1); m1t1.setInputText("3 4\n"); m1t1.setExpectedOutput("12"); m1t1.setOrderIndex(1);
        ProblemTestCase m1t2 = new ProblemTestCase(); m1t2.setProblem(m1); m1t2.setInputText("-5 7\n"); m1t2.setExpectedOutput("-35"); m1t2.setOrderIndex(2);
        m1.getTestCases().add(m1t1); m1.getTestCases().add(m1t2);

        Problem m2 = new Problem();
        m2.setTitle("Palindrome");
        m2.setStatement("Read a line and print YES if it is a palindrome, otherwise NO.");
        m2.setContest(c2);
        ProblemTestCase m2t1 = new ProblemTestCase(); m2t1.setProblem(m2); m2t1.setInputText("racecar\n"); m2t1.setExpectedOutput("YES"); m2t1.setOrderIndex(1);
        ProblemTestCase m2t2 = new ProblemTestCase(); m2t2.setProblem(m2); m2t2.setInputText("hello\n"); m2t2.setExpectedOutput("NO"); m2t2.setOrderIndex(2);
        m2.getTestCases().add(m2t1); m2.getTestCases().add(m2t2);

        c2.getProblems().add(m1);
        c2.getProblems().add(m2);

        contestRepo.save(c2);
        return ResponseEntity.ok("Contest 2 seeded");
    }


    @GetMapping("/contests")
    public ResponseEntity<?> listContests() {
        List<ContestSummary> list = contestRepo.findAll().stream()
                .sorted(Comparator.comparing(Contest::getId))
                .map(c -> new ContestSummary(c.getId(), c.getTitle()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

 
    @PostMapping("/contests")
    public ResponseEntity<?> createContest(@Valid @RequestBody CreateContestRequest req) {
        if (contestRepo.findByTitle(req.title).isPresent()) {
            return ResponseEntity.badRequest().body("Contest title already exists");
        }
        Contest c = new Contest();
        c.setTitle(req.title);
        contestRepo.save(c);
        return ResponseEntity.ok(new IdResponse(c.getId()));
    }

    @PostMapping("/contests/{contestId}/problems")
    public ResponseEntity<?> createProblem(@PathVariable("contestId") Long contestId, @Valid @RequestBody CreateProblemRequest req) {
        Optional<Contest> oc = contestRepo.findById(contestId);
        if (oc.isEmpty()) return ResponseEntity.notFound().build();
        Contest c = oc.get();
        Problem p = new Problem();
        p.setTitle(req.title);
        p.setStatement(req.statement);
        p.setContest(c);
        c.getProblems().add(p);
        contestRepo.save(c);
        return ResponseEntity.ok(new IdResponse(p.getId()));
    }


    @PostMapping("/problems/{problemId}/tests")
    public ResponseEntity<?> addTest(@PathVariable("problemId") Long problemId, @Valid @RequestBody CreateTestCaseRequest req) {
        Optional<Problem> op = problemRepo.findById(problemId);
        if (op.isEmpty()) return ResponseEntity.notFound().build();
        Problem p = op.get();
        ProblemTestCase tc = new ProblemTestCase();
        tc.setProblem(p);
        tc.setInputText(req.inputText);
        tc.setExpectedOutput(req.expectedOutput);
        int idx = req.orderIndex != null ? req.orderIndex : (p.getTestCases().size() + 1);
        tc.setOrderIndex(idx);
        p.getTestCases().add(tc);
        problemRepo.save(p);
        return ResponseEntity.ok(new IdResponse(tc.getId()));
    }
}
