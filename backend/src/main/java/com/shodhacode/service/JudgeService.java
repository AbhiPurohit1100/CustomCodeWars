package com.shodhacode.service;

import com.shodhacode.model.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class JudgeService {

    public static class JudgeResult {
        public final SubmissionStatus status; public final String message;
        public JudgeResult(SubmissionStatus status, String message) { this.status = status; this.message = message; }
    }

    private static final String CLASS_NAME = "Main";
    private static final long TIMEOUT_SECONDS = 5; // allow a bit more for docker
    private static final String DOCKER_IMAGE = "openjdk:17";
    private static final String WRITER_IMAGE = "alpine";

    public JudgeResult judge(Submission submission) {
        Problem problem = submission.getProblem();
        List<ProblemTestCase> tests = problem.getTestCases();
        if (tests == null || tests.isEmpty()) {
            return new JudgeResult(SubmissionStatus.ERROR, "Problem has no testcases");
        }
        boolean useDocker = "docker".equalsIgnoreCase(System.getenv().getOrDefault("JUDGE_MODE", "local"));
        try {
            if (useDocker) {
                return judgeWithDocker(submission, tests);
            }
            Path workDir = Files.createTempDirectory("judge-");
            Path source = workDir.resolve(CLASS_NAME + ".java");
            Files.writeString(source, submission.getSourceCode(), StandardCharsets.UTF_8);


            JudgeResult comp = runProcess(new ProcessBuilder("javac", CLASS_NAME + ".java").directory(workDir.toFile()), null, "Compilation Error");
            if (comp.status != SubmissionStatus.ACCEPTED) {
                cleanup(workDir);
                return comp;
            }


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

    private JudgeResult judgeWithDocker(Submission submission, List<ProblemTestCase> tests) {
        String volumeName = ("shodha_code_" + UUID.randomUUID()).replace("-", "");
        try {
            // Ensure images are present (first run pulls can be slow)
            ensureDockerImage(WRITER_IMAGE);
            ensureDockerImage(DOCKER_IMAGE);

            // Create a volume
            JudgeResult volRes = runDocker(new String[]{"docker","volume","create", volumeName}, null, TIMEOUT_SECONDS);
            if (volRes.status != SubmissionStatus.ACCEPTED) return volRes;

            // Write code into volume using an alpine helper
            String writerCmd = "sh -lc \"cat > /work/" + CLASS_NAME + ".java\"";
        JudgeResult writeRes = runDocker(
            new String[]{"docker","run","--rm","-i","-v", volumeName+":/work", WRITER_IMAGE, "sh","-lc","cat > /work/"+CLASS_NAME+".java"},
            submission.getSourceCode(), TIMEOUT_SECONDS);
            if (writeRes.status != SubmissionStatus.ACCEPTED) return writeRes;

            // Compile inside openjdk
            String compileCmd = "sh -lc \"javac /work/" + CLASS_NAME + ".java 2>&1\"";
        JudgeResult compRes = runDocker(
                    new String[]{"docker","run","--rm","--network","none","-v", volumeName+":/work", DOCKER_IMAGE, "sh","-lc","javac /work/"+CLASS_NAME+".java 2>&1"},
            null, TIMEOUT_SECONDS + 25); // allow extra time on first pull
            if (compRes.status != SubmissionStatus.ACCEPTED) {
                return new JudgeResult(SubmissionStatus.ERROR, "Compilation Error\n" + compRes.message);
            }

            // Run per test with limits
            for (ProblemTestCase tc : tests) {
                JudgeResult runRes = runDocker(
                        new String[]{
                                "docker","run","--rm","-i",
                                "--network","none",
                                "--cpus","0.5","-m","256m","--pids-limit","128",
                                "-v", volumeName+":/work",
                                DOCKER_IMAGE,
                                "sh","-lc","java -Xmx256m -cp /work " + CLASS_NAME + " 2>&1"
                        },
                        tc.getInputText(), TIMEOUT_SECONDS);
                if (runRes.status != SubmissionStatus.ACCEPTED) return runRes;
                String got = normalize(runRes.message);
                String expected = normalize(tc.getExpectedOutput());
                if (!got.equals(expected)) {
                    return new JudgeResult(SubmissionStatus.WRONG_ANSWER,
                            "Wrong Answer\nExpected:\n" + expected + "\nGot:\n" + got);
                }
            }
            return new JudgeResult(SubmissionStatus.ACCEPTED, "All tests passed");
        } finally {
            try {
                runDocker(new String[]{"docker","volume","rm","-f", volumeName}, null, 10);
            } catch (Exception ignored) {}
        }
    }

    private JudgeResult runDocker(String[] cmd, String stdin, long timeoutSec) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            if (stdin != null) {
                try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8))) {
                    w.write(stdin);
                }
            }
            boolean finished = proc.waitFor(timeoutSec, TimeUnit.SECONDS);
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
                return new JudgeResult(SubmissionStatus.ERROR, output);
            }
            return new JudgeResult(SubmissionStatus.ACCEPTED, output);
        } catch (Exception e) {
            return new JudgeResult(SubmissionStatus.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void ensureDockerImage(String image) {
        try {
            ProcessBuilder check = new ProcessBuilder("docker","image","inspect", image);
            Process p = check.start();
            boolean ok = p.waitFor(10, TimeUnit.SECONDS) && p.exitValue() == 0;
            if (!ok) {
                ProcessBuilder pull = new ProcessBuilder("docker","pull", image);
                pull.redirectErrorStream(true);
                Process pr = pull.start();
                pr.waitFor(180, TimeUnit.SECONDS);
            }
        } catch (Exception ignored) {}
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
