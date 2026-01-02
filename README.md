# Urban Fleet Tracker - Backend

## Tech Stack
- **Kotlin 1.9.25** + **Spring Boot 3.3.5**
- **JPA/Hibernate** + **PostgreSQL** (H2 for dev)
- **WebSocket** support for real-time updates
- **Java 21 (Temurin)**

## Getting Started

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The server will start at `http://localhost:8080`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/units` | List all units |
| GET | `/api/units/{id}` | Get unit by ID |
| GET | `/api/units/type/{PATROL\|MEDIC}` | Filter by type |
| GET | `/api/units/status/{ACTIVE\|EMERGENCY\|IDLE}` | Filter by status |
| GET | `/api/units/statistics` | Get fleet statistics |
| POST | `/api/units` | Create new unit |
| PATCH | `/api/units/{id}/status` | Update unit status |
| PATCH | `/api/units/{id}/location` | Update unit location |
| DELETE | `/api/units/{id}` | Delete unit |

## H2 Console (Development)
Access the in-memory database at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:fleetdb`
- Username: `sa`
- Password: (empty)
