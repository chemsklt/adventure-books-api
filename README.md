#  Adventure Book API

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-brightgreen)
![Build](https://img.shields.io/badge/build-Maven-blue)
![Coverage](https://img.shields.io/badge/coverage-60+%25-brightgreen)
![CI](https://github.com/chemsklt/adventure-books-api/actions/workflows/ci.yml/badge.svg)

A RESTful API to manage and play interactive **choose-your-own-adventure** books.

This project was built following an **API-first approach**, with strong validation, clean architecture, and full test coverage.

---

##  Features

###  Book Catalog

* List all books
* Search by:

    * title
    * author
    * category
    * difficulty

###  Book Management

* Retrieve book details
* Add/remove categories
* Create new books dynamically via API

###  Book Reading Engine

* Navigate through sections
* Choose options to move across the story
* Graph-based story structure

###  Game Sessions

* Start a game session
* Track:

    * current position
    * health
    * status

###  Game Mechanics

* Consequences (e.g. lose health)
* Automatic state transitions:

    * `IN_PROGRESS`
    * `PAUSED`
    * `DEAD`
    * `FINISHED`

###  Session Control

* Pause / Resume game
* Prevent invalid actions depending on state

---

##  Architecture

### API-First Approach

* OpenAPI 3 contract defined first (openapi/adventure-book-api.yml)
* Code generated using OpenAPI Generator
* Controllers implement generated interfaces

### Layered Design

```text
Controller → Service → Repository
              ↓
            Domain
```

### Key Principles

* Clear separation between **API models** and **domain models**
* Business logic isolated in services
* Mapping handled via MapStruct (no reflection, compile-time safe)

---

##  Domain Modeling

The core domain models:

* **Book**
* **Section**
* **Option**
* **Consequence**
* **GameSession**

A book is modeled as a **directed graph**:

* Nodes → Sections
* Edges → Options

This allows flexible branching narratives.

---

##  Validation Strategy

A dedicated `BookValidationService` ensures:

* Exactly **one BEGIN section**
* At least **one reachable END section**
* All `gotoId` references are valid
* No broken or unreachable paths
* All non-END sections have options

Validation uses a **BFS traversal** to ensure structural integrity.

---

##  Game Engine

Each game session tracks:

* `gameId`
* `bookId`
* `currentSectionId`
* `health`
* `status`

### State Transitions

| Condition         | Result   |
| ----------------- | -------- |
| health ≤ 0        | DEAD     |
| reach END section | FINISHED |
| manual pause      | PAUSED   |

### Rules

* Choices are only allowed when `IN_PROGRESS`
* Invalid transitions return `400 Bad Request`

---

##  Persistence Strategy

### Current Implementation

* In-memory repository using `ConcurrentHashMap`

### Why?

* Simplicity for the challenge
* No external dependencies
* Fast feedback loop
* Deterministic testing

---

##  Moving to a Real Database

Recommended stack:

* PostgreSQL
* Spring Data JPA

## Suggested Schema
* books
* sections
* options
* consequences
* game_sessions

### Considerations:

* Normalize entities: Book / Section / Option
* Use foreign keys for relationships
* Ensure transactional for consistency
* Add indexes for performance (bookId, sectionId)
* Optional: use JSONB for flexible structures

---

##  Testing Strategy

### Unit Tests

* Service logic
* Validation logic
* Game mechanics

### Controller Tests

* API contract validation
* Error handling

### Integration Tests

* Full end-to-end flows

---

##  Example Test Scenarios

### Create Book

```http
POST /books
```

* Valid → `201 Created`
* Duplicate → `409 Conflict`
* Invalid structure → `400 Bad Request`

---

### Game Flow

1. Start game
2. Choose options
3. Health changes
4. Game ends when:

    * health = 0 → DEAD
    * END reached → FINISHED

---

### Pause / Resume

```http
POST /games/{id}/pause
POST /games/{id}/resume
```

* Cannot play while paused
* Resume restores gameplay

---

## 📦 Postman Collection

A Postman collection is included for easy manual testing.

Location:

```text
postman/adventure-book-api.postman_collection.json
```

### How to use

1. Import into Postman
2. Set variable:

    * `baseUrl = http://localhost:8080`
3. Run collections by feature:

    * catalog
    * reading
    * game sessions
    * pause/resume
    * create book

This allows quick validation of all core flows.

---

##  Tech Stack

* Java 21
* Spring Boot
* MapStruct
* OpenAPI Generator
* JUnit 5
* Mockito
* MockMvc
* Maven
* JaCoCo

---

##  Code Quality

* Minimum coverage 60% enforced via JaCoCo
* Generated code excluded from coverage
* CI pipeline runs tests on every push

---

##  CI/CD

GitHub Actions pipeline:

* Build project
* Run tests
* Validate coverage

---

##  Running the Project

```bash
mvn clean install
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

##  Design Decisions

### Why API-First?

* Clear contract
* Decoupled implementation
* Easier testing and evolution

### Why MapStruct?

* Compile-time mapping
* No runtime overhead
* Cleaner code

### Why In-Memory First?

* Focus on business logic
* Simplifies setup
* Easy to extend later

---

##  Possible Improvements

* Persistent database
* Authentication / authorization
* Multiplayer support
* Event-driven architecture
* Caching layer (Redis)
* Observability (metrics, tracing)

---

##  Author

Built as part of a backend engineering challenge with focus on:

* clean architecture
* domain modeling
* validation
* testability
* production-ready design thinking
