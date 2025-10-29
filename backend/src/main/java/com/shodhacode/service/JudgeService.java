package com.shodhacode.service;

import com.shodhacode.model.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class JudgeService {

    public static class JudgeResult {
        public final SubmissionStatus status; public final String message;
        public JudgeResult(SubmissionStatus status, String message) { this.status = status; this.message = message; }
    }

    private static final String CLASS_NAME = "Main";
    private static final long TIMEOUT_SECONDS = 3; // fast local process

    public JudgeResult judge(Submission submission) {
        Problem problem = submission.getProblem();
        List<ProblemTestCase> tests = problem.getTestCases();
        if (tests == null || tests.isEmpty()) {
            return new JudgeResult(SubmissionStatus.ERROR, "Problem has no testcases");
        }
        try {
            Path workDir = Files.createTempDirectory("judge-");
            Path source = workDir.resolve(CLASS_NAME + ".java");
            Files.writeString(source, submission.getSourceCode(), StandardCharsets.UTF_8);

            // Compile
            JudgeResult comp = runProcess(new ProcessBuilder("javac", CLASS_NAME + ".java").directory(workDir.toFile()), null, "Compilation Error");
            if (comp.status != SubmissionStatus.ACCEPTED) {
                cleanup(workDir);
                return comp;
            }

            // Execute all tests
            for (ProblemTestCase tc : tests) {
                JudgeResult exec = runProcess(
                        new ProcessBuilder("java", "-Xmx256m", CLASS_NAME).directory(workDir.toFile()),
                        tc.getInputText(),
                        null
                );
                if (exec.status != SubmissionStatus.ACCEPTED) {
                    cleanup(workDir);
                    return exec;
                }
                String got = normalize(exec.message);
                String expected = normalize(tc.getExpectedOutput());
                if (!got.equals(expected)) {
                    cleanup(workDir);
                    return new JudgeResult(SubmissionStatus.WRONG_ANSWER,
                            "Wrong Answer\nExpected:\n" + expected + "\nGot:\n" + got);
                }
            }
            cleanup(workDir);
            return new JudgeResult(SubmissionStatus.ACCEPTED, "All tests passed");
        } catch (IOException e) {
            return new JudgeResult(SubmissionStatus.ERROR, "IO error: " + e.getMessage());
        }
    }

    private JudgeResult runProcess(ProcessBuilder pb, String stdin, String errorLabel) {
        try {
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            if (stdin != null) {
                try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8))) {
                    w.write(stdin);
                }
            }
            boolean finished = proc.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                proc.destroyForcibly();
                return new JudgeResult(SubmissionStatus.ERROR, "Time Limit Exceeded");
            }
            String output;
            try (InputStream is = proc.getInputStream()) {
                output = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
            }
            int code = proc.exitValue();
            if (code != 0) {
                return new JudgeResult(SubmissionStatus.ERROR, (errorLabel != null ? errorLabel + "\n" : "") + output);
            }
            return new JudgeResult(SubmissionStatus.ACCEPTED, output);
        } catch (IOException | InterruptedException e) {
            return new JudgeResult(SubmissionStatus.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void cleanup(Path workDir) {
        try {
            Files.walk(workDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        } catch (IOException ignored) {}
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.replace("\r\n", "\n").replace("\r", "\n");
        t = t.lines().map(line -> line.replaceAll("\\s+$", "")).collect(Collectors.joining("\n"));
        while (t.endsWith("\n")) t = t.substring(0, t.length()-1);
        return t;
    }
}
