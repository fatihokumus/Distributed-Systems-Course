# campusflow-monolith

Monolithic full-stack enrollment demo built for course labs.

## Stack
- Backend: Spring Boot 3.x, Java 21, Maven, PostgreSQL, Flyway, JPA
- Frontend: React + Vite + TypeScript
- Infra: Docker Compose

## Prerequisites
- Docker + Docker Compose
- (Optional local backend tests) Java 21 and Maven 3.9+

## Run everything
```bash
cd campusflow-monolith
docker compose up --build
```

## URLs
- Frontend: http://localhost:8081
- Backend health: http://localhost:8080/health
- Backend actuator health: http://localhost:8080/actuator/health

## Verify seeded courses
```bash
curl http://localhost:8080/api/v1/courses
```

Expected seeded course codes:
- CSE401 (capacity 2)
- CSE402 (capacity 1)
- CSE403 (capacity 3)

## Enrollment examples
```bash
curl -X POST http://localhost:8080/api/v1/enrollments \
  -H "Content-Type: application/json" \
  -d '{"studentNo":"20201234","courseCode":"CSE401"}'

curl "http://localhost:8080/api/v1/enrollments?studentNo=20201234"

curl "http://localhost:8080/api/v1/audit?limit=50"
```

## Run backend tests
```bash
cd backend
mvn test
```
