# Shodh-a-Code

A prototype coding contest platform.

- Frontend: React + Tailwind CSS
- Backend: Java 17 + Spring Boot
- Database: H2 (file-based for local persistence)
- Execution Engine: Two modes
  - Local JDK: compile/run inside backend container (javac/java)
  - Docker mode (default in compose): each test runs in an `openjdk:17` container with CPU/mem limits
- Monorepo layout: `frontend/` and `backend/`

## Quick Start 


### Run with Docker Compose (Docker judge enabled)

Prerequisites:
- Docker Desktop (Linux containers)

Start services (first run may pull a few images):

```powershell

docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/shodha`)

Admin UI (create contests/problems/tests):
- http://localhost:5173/admin

Stop:
```powershell
docker compose down
```

## API Design 

- GET `/api/contests/{contestId}`
  - Returns contest details with problems (statement only, no answers)
- POST `/api/submissions`
  - Body: `{ contestId, problemId, username, sourceCode }`
  - Response: `{ id, status }`
- GET `/api/submissions/{id}`
  - Returns submission with status: `PENDING | RUNNING | ACCEPTED | WRONG_ANSWER | ERROR` and message
- GET `/api/contests/{contestId}/leaderboard`
  - Returns leaderboard entries `{ username, solved, lastAcceptedAt }`

Request/Response examples are included inline in controller JavaDoc and below.

Note: Use actual problemId from the contest response.

# CustomCodeWars (my attempt)

  This is my small end‑to‑end coding contest prototype. It has a React frontend, a Spring Boot backend, and a judge that can run code in Docker. I tried to keep it simple and make it easy to run locally.

  - Frontend: React + Tailwind CSS
  - Backend: Java 17 + Spring Boot
  - Database: H2 (file-based, so nothing to install)
  - Judge: two modes
    - Docker mode (default in docker-compose): each test runs in an `openjdk:17` container with resource limits
    - Local mode: compile/run inside the backend container using `javac/java`
  - Repo layout: `frontend/` and `backend/`

  ## How I run it locally

  Prerequisites: Docker Desktop (Linux containers)

  1) Start everything (first run might pull images, so a bit slower):

  ```powershell
  docker compose up --build
  ```

  2) Open the app:
  - Frontend: http://localhost:5173
  - Admin (to create contests/problems/tests): http://localhost:5173/admin
  - Backend API: http://localhost:8080
  - H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/shodha`)

  3) Stop:

  ```powershell
  docker compose down
  ```

  What I usually do after it starts:
  - Go to the Admin page and make a new contest, add a problem, then add a couple of test cases (input/output)
  - Then go to the Join page, enter the contest ID and a username, and try submitting code

  ## API (just the basics I needed)

  - GET `/api/contests/{contestId}` → contest info with its problems
  - POST `/api/submissions` → submit code `{ contestId, problemId, username, sourceCode }` → returns `{ id, status }`
  - GET `/api/submissions/{id}` → poll this every few seconds to get the status/message
  - GET `/api/contests/{contestId}/leaderboard` → simple live-ish leaderboard

  Note: use the real `problemId` from the contest response (IDs are created by the DB).

  Submission example body:

  ```json
  {
    "contestId": 1,
    "problemId": 1,
    "username": "alice",
    "sourceCode": "public class Main { public static void main(String[] a){ java.util.Scanner sc=new java.util.Scanner(System.in); System.out.print(sc.nextInt()+sc.nextInt()); }}"
  }
  ```

  ## How the judge works (in plain words)

  Default (in compose) I run the judge in Docker mode because it feels closer to how real OJ systems isolate code:
  - Backend talks to a `docker:24-dind` sidecar (via `DOCKER_HOST=tcp://docker:2375`)
  - For each submission it makes a temp Docker volume
  - It writes `Main.java` into that volume (tiny Alpine helper)
  - Compiles with `openjdk:17` in a container
  - Runs each test in a fresh container with limits: `--network none`, `--cpus 0.5`, `-m 256m`, `--pids-limit 128`
  - Pipes the test input to stdin, captures stdout, compares to expected
  - Cleans up the volume

  First run note: the sidecar will pull `alpine` and `openjdk:17` the first time. This can make the very first submission take longer.

  Switching to local mode (faster, less isolation):
  - Set `JUDGE_MODE=local` for the backend and remove `DOCKER_HOST` + the `docker` service from compose
  - Then it just runs `javac`/`java` inside the backend container with a small timeout and heap

  ## The data I modeled (basic and enough for this)

  - Contest → Problems → ProblemTestCases (input/output pairs)
  - Submission points to Contest, Problem, and a User (just a username string)
  - Leaderboard is 1 point per problem accepted; ties broken by earlier accepted time

  ## Why I did it this way (and what tripped me up)

  - I started with local JDK execution because it’s simple and fast. Then I added Docker mode (with a sidecar) to get better isolation. First run was slow due to image pulls, so I added a pre-check/pull step and a slightly longer timeout for the first compile.
  - I kept the frontend tiny: just component state, Axios, and a polling loop (every ~3s for submission status, ~15s for leaderboard). No Redux or anything heavy.
  - H2 file DB because it “just works” locally and still keeps data between restarts.

  Stuff I’d improve with more time:
  - Better judge logs and nicer error display in the UI
  - Bulk import for test cases (paste JSON or CSV)
  - Add more languages (right now it’s Java only)
  - Proper auth for the admin page

  ## Little troubleshooting notes

  - If the very first submission says time limit exceeded in Docker mode, it might still be pulling images. Try one more time.
  - Check the H2 console if you’re curious what got stored (URL above). The DB file lives under `backend/data`.
  - If Docker Desktop isn’t running, compose won’t start (I forget this all the time).



