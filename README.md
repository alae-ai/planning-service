# 🗓️ Planning Service

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![REST API](https://img.shields.io/badge/REST-API-blue?style=for-the-badge)

A Spring Boot microservice for managing doctor consultation time slots (créneaux). Part of a larger medical planning system.

## ✨ Features

- List, create, and retrieve consultation slots
- Block and release slots (reservation management)
- Filter available slots (excludes expired ones)
- Filter slots by doctor (`medecinId`)
- Monthly statistics: reserved slots per doctor

## 🏗️ Architecture

```
planning-service/
├── model/          # Creneau entity
├── repository/     # Spring Data JPA
├── service/        # Business logic (CreneauServiceImpl)
└── web/
    ├── controller/ # REST endpoints
    └── error/      # Global exception handler
```

## 🔌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/creneaux` | List all slots (sorted by date) |
| GET | `/creneaux/{id}` | Get slot by ID |
| GET | `/creneaux/disponibles` | List available slots |
| GET | `/creneaux/medecin/{medecinId}` | Slots by doctor |
| POST | `/creneaux` | Create a new slot |
| PUT | `/creneaux/{id}/bloquer` | Reserve a slot |
| PUT | `/creneaux/{id}/liberer` | Release a slot |
| GET | `/creneaux/stats/medecins?year=YYYY&month=MM` | Monthly stats |

## ⚙️ Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Run

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8080` by default.

## 🧠 Business Rules

- Slots in the past are automatically marked unavailable
- Missing fields (date, times) are auto-corrected with defaults
- Blocking/releasing uses atomic DB updates to prevent race conditions
- Invalid `medecinId` causes the slot to be marked unavailable

## ❌ Error Codes

| Code | Meaning |
|------|---------|
| 400 | Invalid parameters |
| 404 | Slot or doctor not found |
| 409 | Conflict (e.g. slot already reserved) |
| 503 | External service unavailable |
