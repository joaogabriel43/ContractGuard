# ContractGuard 🛡️

> **Automatic breaking-change detector for OpenAPI contracts in CI/CD pipelines.**

ContractGuard compares versions of your OpenAPI specification and classifies every difference as
`BREAKING` or `NON_BREAKING`, failing your GitHub Actions workflow before the change reaches production.

---

## Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL 16 (JSONB for spec storage) |
| Migrations | Flyway |
| OpenAPI Parsing | Swagger Parser v2.1.x |
| Testing | JUnit 5 · Mockito · Testcontainers |

## Architecture

Clean Architecture + DDD. See [`CLAUDE.md`](./CLAUDE.md) for full details.

```
presentation → application → domain ← infrastructure
```

## Quick Start (Local)

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker + Docker Compose

### 1. Start the database

```bash
docker-compose up -d
```

### 2. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. Access the API docs

```
http://localhost:8080/swagger-ui.html
```

### 4. Run tests

```bash
# Unit tests only
mvn test

# Full build including integration tests
mvn verify
```

## Project Structure

```
src/
└── main/
    ├── java/br/com/contractguard/
    │   ├── domain/          # Aggregates, Value Objects, Ports
    │   ├── application/     # Use Cases, Commands, Queries
    │   ├── infrastructure/  # JPA adapters, Diff Engine, Config
    │   └── presentation/    # REST Controllers, Mappers
    └── resources/
        ├── application.yml
        ├── application-local.yml
        └── db/migration/    # Flyway SQL scripts
```

## Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/). See [`CLAUDE.md`](./CLAUDE.md#5-regras-de-git-e-commits) for full rules.

## License

Proprietary — All rights reserved.
