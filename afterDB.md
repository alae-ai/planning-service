# Planning Service: Migration From In-Memory Storage To PostgreSQL (Spring Data JPA)

## What changed (ArrayList → Database)
- Removed the in-memory `ArrayList` “database” and the static sample data initialization.
- Converted `Creneau` from a plain POJO into a JPA entity so it can be persisted by Hibernate in PostgreSQL.
- Replaced the custom DAO layer with a Spring Data JPA repository (`JpaRepository`).
- Refactored the service layer to use repository calls (and proper `Optional` handling) instead of list operations.

## Why the DAO layer was replaced with a Repository
The previous DAO existed mainly to simulate persistence (static `List<Creneau>` + ID sequence). With a real database:
- Spring Data JPA already provides the standard CRUD operations (create, read, update, delete).
- Repositories remove boilerplate code (manual ID generation, searching lists, updating items in-place).
- Query methods can be declared by method name (derived queries), which is simpler and less error-prone.

## How Spring Data JPA works here
- `Creneau` is annotated with `@Entity`, so Hibernate maps it to a table (`creneaux`).
- `Creneau` is annotated with `@Entity`, so Hibernate maps it to a table (`creneau`).
- `CreneauRepository extends JpaRepository<Creneau, Long>` provides:
  - `save(...)`, `findById(...)`, `findAll(...)`, etc.
  - Derived query methods used by the service:
    - `findByMedecinId(Long medecinId)`
    - `findByDisponibleTrue()`
- The service (`CreneauServiceImpl`) contains the business rules (block/free slot, validation) and delegates persistence to the repository.

## How PostgreSQL is connected
PostgreSQL is configured in `src/main/resources/application.properties` using:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Hibernate is configured with:
- `spring.jpa.hibernate.ddl-auto=update` (creates/updates tables based on entities)
- `spring.jpa.show-sql=true` (logs SQL)
- `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect`

The PostgreSQL JDBC driver is added to `pom.xml` as a runtime dependency (`org.postgresql:postgresql`).

Note: In this workspace the configured database name is `planning_db` (PostgreSQL on `localhost:5432`).

## Final architecture
Controller → Service → Repository → PostgreSQL

- **Controller**: HTTP layer only (no business logic).
- **Service**: business rules + orchestration.
- **Repository**: persistence abstraction (Spring Data JPA).
- **DB**: PostgreSQL stores `creneaux` data.
- **DB**: PostgreSQL stores `creneau` data.

## Benefits vs ArrayList storage
- Data persists across restarts (real persistence).
- Concurrency is handled properly at the database level (no shared static lists).
- Querying/filtering scales better and is simpler to extend.
- Standard, production-grade architecture for microservices.
