package com.shodhacode.data;

import com.shodhacode.model.*;
import com.shodhacode.repo.ContestRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {
    private final ContestRepository contestRepo;

    public DataLoader(ContestRepository contestRepo) { this.contestRepo = contestRepo; }

    @PostConstruct
    public void init() {
        if (!contestRepo.findAll().isEmpty()) return;
        Contest c = new Contest();
        c.setTitle("Sample Contest");

        // Problem 1: Sum two integers
        Problem p1 = new Problem();
        p1.setTitle("A + B");
        p1.setStatement("Read two integers and output their sum.");
        p1.setContest(c);
        ProblemTestCase p1t1 = new ProblemTestCase(); p1t1.setProblem(p1); p1t1.setInputText("1 2\n"); p1t1.setExpectedOutput("3"); p1t1.setOrderIndex(1);
        ProblemTestCase p1t2 = new ProblemTestCase(); p1t2.setProblem(p1); p1t2.setInputText("-5 7\n"); p1t2.setExpectedOutput("2"); p1t2.setOrderIndex(2);
        p1.getTestCases().add(p1t1); p1.getTestCases().add(p1t2);

        // Problem 2: Reverse string (single line)
        Problem p2 = new Problem();
        p2.setTitle("Reverse String");
        p2.setStatement("Read a line and print it reversed.");
        p2.setContest(c);
        ProblemTestCase p2t1 = new ProblemTestCase(); p2t1.setProblem(p2); p2t1.setInputText("hello\n"); p2t1.setExpectedOutput("olleh"); p2t1.setOrderIndex(1);
        ProblemTestCase p2t2 = new ProblemTestCase(); p2t2.setProblem(p2); p2t2.setInputText("abcde\n"); p2t2.setExpectedOutput("edcba"); p2t2.setOrderIndex(2);
        p2.getTestCases().add(p2t1); p2.getTestCases().add(p2t2);

        c.getProblems().add(p1);
        c.getProblems().add(p2);

        contestRepo.save(c);

        Contest c2 = new Contest();
        c2.setTitle("Algorithms Warmup");


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
    }
}
