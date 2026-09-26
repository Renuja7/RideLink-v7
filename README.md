# RideLink — Backend Microservices for a Ride-Sharing Platform

RideLink is a fictional ride-sharing backend built as four independently deployable Spring Boot microservices, each owning its own MongoDB database.

## Architecture

| # | Service | Port | Owns | Database |
|---|---------|------|------|----------|
| 1 | account-service | 8081 | Accounts, auth, roles | ridelink_account |
| 2 | driver-service | 8082 | Driver profile, vehicles, availability | ridelink_driver |
| 3 | ride-service | 8083 | Ride requests, assignment, lifecycle | ridelink_ride |
| 4 | payment-service | 8084 | Fare estimation, payments, receipts | ridelink_payment |

Inter-service communication uses **synchronous REST via OpenFeign**:
- `ride-service → driver-service` — fetch eligible available drivers for assignment.
- `ride-service → payment-service` — fetch a fare estimate on ride creation, and trigger a simulated payment on ride completion.

Each service owns its own persistence boundary — no service queries another service's database directly. Services communicate only through their public REST APIs, using stable string IDs (`userId`, `driverId`, `rideId`) as references across service boundaries. Every id is a MongoDB-native ObjectId, rendered as its 24-character hex string (e.g. `"671e2f9a1b2c3d4e5f678901"`) — ids are opaque, stable identifiers passed between services and are never treated as numeric.

## Tech Stack

- Java 21, Spring Boot 3.3
- Spring Web, Spring Data MongoDB, Spring Security
- JWT authentication (jjwt) — shared signing secret (`JWT_SECRET`) across services so any service can independently validate a token issued by account-service
- MongoDB 7 — one independent MongoDB instance per service (no shared database across services)
- OpenFeign (ride-service only)
- springdoc-openapi (Swagger UI) on every service
- Lombok
- JUnit 5 + Mockito
- Maven
- Docker Compose
- GitHub Actions CI

## Project Structure

Each service follows the same internal layout:

```
<service>/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/ridelink/<service>/
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── entity/
    │   │   ├── dto/
    │   │   ├── config/
    │   │   ├── security/
    │   │   ├── exception/
    │   │   └── client/
    │   └── resources/application.yml
    └── test/
        ├── java/com/ridelink/<service>/service/
        └── resources/application-test.yml
```

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker & Docker Compose (recommended)
- MongoDB 7 (only if running services outside Docker)

## Configuration

All configuration is externalized via environment variables (no secrets committed):

| Variable | Description | Default |
|---|---|---|
| `MONGO_HOST`, `MONGO_PORT`, `MONGO_DATABASE` | Per-service MongoDB connection | see `application.yml` |
| `JWT_SECRET` | HMAC-SHA256 signing key shared by all services | dev default in `application.yml` |
| `DRIVER_SERVICE_URL` | Base URL ride-service uses to call driver-service | `http://localhost:8082` |
| `PAYMENT_SERVICE_URL` | Base URL ride-service uses to call payment-service | `http://localhost:8084` |

## Start-up Order (running locally without Docker)

1. Start 4 separate MongoDB instances (or one MongoDB server with 4 distinct databases, one per service — never a single database shared across services): `ridelink_account`, `ridelink_driver`, `ridelink_ride`, `ridelink_payment`.
2. Start **account-service** (`mvn spring-boot:run` in `account-service/`).
3. Start **driver-service** and **payment-service** (any order).
4. Start **ride-service** last, since it calls driver-service and payment-service via Feign.

## Running with Docker Compose (recommended)

```bash
docker compose up --build
```

This starts 4 MongoDB containers (one per service, each with its own volume — satisfying the "own database, not shared" requirement) and all 4 Spring Boot services, wired together on a shared bridge network. Services become available at:

- Account: http://localhost:8081/swagger-ui.html
- Driver & Vehicle: http://localhost:8082/swagger-ui.html
- Ride Management: http://localhost:8083/swagger-ui.html
- Fare & Payment: http://localhost:8084/swagger-ui.html

## Running Tests

From each service directory:

```bash
mvn test
```

Or for all four services:

```bash
for s in account-service driver-service ride-service payment-service; do (cd $s && mvn test); done
```

## Sample Test Data / Credentials

Register your own test accounts via `POST /api/auth/register`. Example passenger:

```json
{
  "fullName": "Nimal Perera",
  "email": "nimal@example.com",
  "phoneNumber": "0771234567",
  "password": "password123",
  "role": "PASSENGER"
}
```

Example driver:

```json
{
  "fullName": "Kamal Silva",
  "email": "kamal@example.com",
  "phoneNumber": "0777654321",
  "password": "password123",
  "role": "DRIVER"
}
```

Then log in via `POST /api/auth/login` to obtain a JWT, and send it as `Authorization: Bearer <token>` to every other endpoint.

## Endpoint Summary

### account-service (`/api/auth`, `/api/users`)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/{id}`
- `PUT /api/users/{id}` — update profile (name, phone)
- `PUT /api/users/{id}/role` (ADMIN only) — role management
- `PUT /api/users/{id}/status` (ADMIN only) — account status management (ACTIVE/INACTIVE/SUSPENDED)
- `GET /api/users` (ADMIN only)

### driver-service (`/api/drivers`, `/api/vehicles`)
- `POST /api/drivers`
- `POST /api/vehicles`
- `PUT /api/drivers/{id}/availability`
- `PUT /api/drivers/{id}/location`
- `PUT /api/drivers/{id}/service-area`
- `GET /api/drivers/available`

### ride-service (`/api/rides`)
- `POST /api/rides`
- `PUT /api/rides/{id}/assign`
- `PUT /api/rides/{id}/accept`
- `PUT /api/rides/{id}/start`
- `PUT /api/rides/{id}/complete`
- `PUT /api/rides/{id}/cancel`
- `GET /api/rides/{id}`

Ride status lifecycle: `REQUESTED → ASSIGNED → ACCEPTED → IN_PROGRESS → COMPLETED`, with cancellation allowed from any state prior to `COMPLETED`.

### payment-service (`/api/fare`, `/api/payments`, `/api/receipts`)
- `POST /api/fare/estimate` — `fare = 200 + (distanceKm * 50)`
- `POST /api/payments`
- `GET /api/payments/{id}`
- `GET /api/receipts/{id}`

## Security

- JWT bearer authentication on all endpoints except `/api/auth/**` and Swagger UI.
- Role-based authorization: `PASSENGER`, `DRIVER`, `ADMIN`.
- Passwords hashed with BCrypt; no secrets committed — all sensitive values are environment variables.

## Error Response Format

All services return a consistent error shape via a `GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-09-17T10:15:30",
  "status": 400,
  "message": "Full name is required",
  "path": "/api/auth/register"
}
```

## Negative Scenarios Covered

- Registering with a duplicate email → `409 Conflict`
- Logging in with invalid credentials → `401 Unauthorized`
- Assigning a driver when none are available → `409 Conflict`
- Invalid ride status transition (e.g. completing a ride that hasn't started) → `400 Bad Request`
- Invalid/failed simulated payment (non-positive amount) → `422 Unprocessable Entity`

## Continuous Integration

`.github/workflows/ci.yml` runs a matrix build across all four services on every push/PR: compiles, runs unit tests, packages the jar, and verifies each service's Docker image builds.
