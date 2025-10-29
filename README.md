# Shodh-a-Code

A prototype coding contest platform.

- Frontend: React + Tailwind CSS
- Backend: Java 17 + Spring Boot
- Database: H2 (file-based for local persistence)
- Execution Engine: Compiles and runs user Java code locally inside the backend container (javac/java, JDK 17)
- Monorepo layout: `frontend/` and `backend/`

## Quick Start (local dev)


### Run with Docker Compose

Prerequisites:
- Docker Desktop (Linux containers)

Start services:

```powershell

docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/shodha`)

Stop:
```powershell
docker compose down
```

## API Design (overview)

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

### Submission request example
```json
{
  "contestId": 1,
  "problemId": 101,
  "username": "alice",
  "sourceCode": "public class Main { public static void main(String[] a){ System.out.print(new java.util.Scanner(System.in).nextInt()+new java.util.Scanner(System.in).nextInt()); }}"
}
```

### Submission response example
```json
{ "id": 5, "status": "PENDING" }
```

## Design choices (plain and simple)

- Compiling: I just compile and run Java inside the backend container using the JDK (javac/java). There’s a small timeout (~3s) and a small heap (`-Xmx256m`).
- Database: I used an H2 file database so we don’t have to install anything. `docker-compose.yml` builds and runs both apps.
- Data shape: `Contest` has `Problem`s, and each `Problem` has `ProblemTestCase`s (input/output pairs). A `Submission` points to a `User`, a `Problem`, and a `Contest`. That’s enough for judging and making a leaderboard.
- Frontend: very light. Local component state, a basic polling for updates, and Axios for requests.
- Scoring: 1 point per solved problem. If there’s a tie, whoever solved earlier ranks higher. It’s calculated on the spot.

## Project Structure

- `backend/` — Spring Boot service, REST API, async judge
- `frontend/` — React + Tailwind app (Join and Contest pages)
- `docker-compose.yml` — one command to build and run locally


