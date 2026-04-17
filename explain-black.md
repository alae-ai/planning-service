# Planning Service Improvements: Resilience, Consistency, Clean Errors

## What changes were made

### 1) Resilience for external microservices
- Removed the external client abstraction layer (`MedecinClient`) to keep the TP design simple.
- Resilience is now handled **only in the service layer** (`CreneauServiceImpl`) using a direct `try/catch` around a conceptual "medecin-service" check.
- When the dependency is simulated as down (`planning.simulate.medecin-service.down=true`), the service throws `ExternalServiceUnavailableException`, which is converted into a clean **503** JSON response (no stack traces in JSON).

### 2) Data consistency (correct state transitions)
Blocking (`bloquer`) and releasing (`liberer`) now enforce:
- `bloquer(id)`:
  - must exist (else 404)
  - must be `disponible == true` (else 409)
  - then sets `disponible = false`
- `liberer(id)`:
  - must exist (else 404)
  - must be `disponible == false` (else 409)
  - then sets `disponible = true`

To avoid race conditions with concurrent requests, the repository uses atomic update queries:
- `bloquerIfDisponible(id)` updates only when `disponible=true`
- `libererIfBloque(id)` updates only when `disponible=false`

### 3) Clean global error handling
- Updated the global `@RestControllerAdvice` to return clean JSON responses:

```json
{
  "error": "CRENEAU_CONFLICT",
  "message": "Creneau is already in the requested state",
  "status": 409
}
```

Handled cases:
- 404: `CRENEAU_NOT_FOUND`, `MEDECIN_NOT_FOUND`
- 409: `CRENEAU_CONFLICT`
- 503: `DEPENDENCY_UNAVAILABLE` (external service failure)
- 400: `BAD_REQUEST`
- 500: `INTERNAL_ERROR` with a generic message (stack traces are only logged server-side)

## Why each change was necessary
- **Resilience:** external dependencies can be slow/down; wrapping them ensures the Planning Service returns a controlled response instead of crashing or leaking stack traces.
- **Consistency:** list-based logic was vulnerable to concurrency; even with a database you can still have races unless state transitions are atomic or locked.
- **Clean errors:** consistent JSON error shapes improve client integration and debugging, while preventing internal details from being exposed.

## Architecture (Clean separation)
Controller → Service → Repository → DB

- **Controller**: remains thin; only delegates to the service (no business rules).
- **Service**: validates rules, performs state transitions, calls external dependencies safely.
- **Repository**: only database operations (CRUD + atomic state updates).
- **DB**: PostgreSQL stores persistent `creneau` rows.

## Error handling strategy
- Throw domain exceptions from the service (`CreneauNotFoundException`, `CreneauStateException`, etc.).
- Convert them to HTTP status codes + `{error,message,status}` in `ApiExceptionHandler`.
- For unexpected failures, return a generic 500 JSON response and log details on the server.

## Resilience strategy (external failures)
- The service layer contains a simple method that represents the external dependency check.
- A `try/catch` converts any simulated failure into `ExternalServiceUnavailableException`.
- The global exception handler maps it to HTTP **503** with a clean JSON payload.

## Data consistency rules summary
- `bloquer` is allowed only when `disponible=true`.
- `liberer` is allowed only when `disponible=false`.
- Repository uses atomic update queries to enforce correct transitions under concurrency.

## API behavior summary
- Endpoints and URLs are unchanged.
- Successful behavior is the same as before, but persistence is PostgreSQL-backed and state transitions are safer.
- Errors are now returned as clean, stable JSON objects with correct HTTP statuses.
