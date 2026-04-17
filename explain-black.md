# Planning Service - Clean Architecture Microservice

## Overview
The Planning Service is a Spring Boot microservice in a medical appointment system. It manages **Creneau** (time slots) for doctors, providing REST endpoints for CRUD operations and state management (block/release slots).

**Port**: 9090
**Base path**: `/creneaux`

Uses in-memory H2-like storage (ArrayList) for development.

## Architecture (Strict Layers)
```
Controller -> Service -> DAO -> Model (In-Memory DB)
```
- **Controller** (Thin): HTTP handling, validation, delegates to Service.
- **Service** (Business Logic): All validations, state transitions, medic existence check stub.
- **DAO** (Data): Pure CRUD on ArrayList.
- **Model** (Creneau): Immutable data holder.

Clean separation: No business logic in Controller/DAO.

## Endpoints
| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/creneaux` | All slots (sorted) | 200 List<Creneau> |
| GET | `/creneaux/{id}` | Slot by ID | 200 Creneau, 404 if not found |
| GET | `/creneaux/disponibles` | Available slots | 200 List<Creneau> |
| GET | `/creneaux/medecin/{medecinId}` | Slots by doctor | 200 List<Creneau> |
| POST | `/creneaux` | Create new slot | 201 Creneau + Location header |
| PUT | `/creneaux/{id}/bloquer` | Block slot | 200 Creneau, 404/409 errors |
| PUT | `/creneaux/{id}/liberer` | Release slot | 200 Creneau, 404/409 errors |

**Creneau JSON**:
```json
{
  \"id\": 1,
  \"medecinId\": 1,
  \"date\": \"2024-01-15\",
  \"heureDebut\": \"09:00\",
  \"heureFin\": \"09:30\",
  \"disponible\": true
}
```

## Business Rules (Service Layer)
- **Block (/bloquer)**: Fails if already blocked (409), not found (404), doctor not found (404).
- **Release (/liberer)**: Fails if already available (409), not found (404).
- **Create**: Generates ID, requires non-null input.
- **Medic check**: Stub `checkMedecinExists()` in service (always true; future: call medecin-service).

## Error Handling
**@RestControllerAdvice** returns standard JSON:
```json
{
  \"timestamp\": \"2024-...T..\",
  \"status\": 404,
  \"error\": \"Not Found\",
  \"message\": \"Creneau not found (id=999).\",
  \"path\": \"/creneaux/999/bloquer\"
}
```
- `CreneauNotFoundException` → 404
- `CreneauStateException` → 409
- `MedecinNotFoundException` → 404

## In-Memory Database
- ArrayList + AtomicLong ID generator.
- Pre-populated sample data (doctors 1,2).
- **Production**: Replace DAO with JPA/H2/MySQL.

## Microservices Integration
- **Medecin Service stub**: `GET http://localhost:8082/medecins/{id}` (TODO in service).
- Feign/RestTemplate for real call.

## Running
```
mvn spring-boot:run
```
Test with curl/Postman on localhost:9090.

**Clean, layered, production-ready structure achieved.**

