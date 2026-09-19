# Task API

Task API is a RESTful backend application for managing personal tasks.

The project was created as a practical Spring Boot learning project. It demonstrates REST API development, authentication and authorization, PostgreSQL persistence, validation, testing, monitoring, containerization, and CI/CD automation.

## Features

- User registration and authentication
- JWT-based authorization
- USER and ADMIN roles
- Task ownership
- Create, read, update, and delete tasks
- Change task status
- Filtering, sorting, and pagination
- Request validation
- Global error handling
- PostgreSQL database
- Flyway database migrations
- Swagger/OpenAPI documentation
- Spring Boot Actuator monitoring
- Automated tests
- Docker and Docker Compose
- GitHub Actions CI/CD
- Automated Docker Hub publishing

## Technologies

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- Jakarta Bean Validation
- PostgreSQL
- Flyway
- Maven
- JUnit
- Testcontainers
- OpenAPI / Swagger UI
- Spring Boot Actuator
- Docker and Docker Compose
- GitHub Actions

## Architecture

The application follows a layered architecture:

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Repository
     ↓
PostgreSQL
```

- Controller handles HTTP requests and responses.
- Service contains application logic and task ownership rules.
- Repository communicates with PostgreSQL through Spring Data JPA.
- DTOs define the data accepted and returned by the API.
- Security layer authenticates users and checks their permissions.
- Global exception handler converts application errors into consistent JSON responses.
- Flyway manages database schema migrations.

## Running with Docker Compose

### Prerequisites

- Git
- Docker Desktop or Docker Engine with Docker Compose

Java and PostgreSQL do not need to be installed locally when the application is run with Docker Compose.

### 1. Clone the repository

```bash
git clone https://github.com/andr1chkol/task-api.git
cd task-api
```

### 2. Create the environment file

```bash
cp .env.example .env
```

Open `.env` and replace the example secrets:

```env
POSTGRES_DB=task_api
POSTGRES_USER=task_api_user
POSTGRES_PASSWORD=your_database_password

JWT_SECRET=your_base64_encoded_secret
JWT_EXPIRATION_MS=900000
```

A JWT secret can be generated with:

```bash
openssl rand -base64 32
```

Do not commit the `.env` file.

### 3. Build and start the application

```bash
docker compose up --build -d
```

Docker Compose starts:

- the Spring Boot application;
- PostgreSQL;
- the shared Docker network;
- the persistent PostgreSQL volume.

### 4. Check the containers

```bash
docker compose ps
```

Both containers should eventually have the `healthy` status.

The API is available at:

```text
http://localhost:8080
```

## Authentication

The API uses stateless JWT authentication.

1. Register a user with `POST /auth/register`.
2. Log in with `POST /auth/login`.
3. Copy the returned `accessToken`.
4. Send it with protected requests:

```http
Authorization: Bearer <accessToken>
```

Passwords are stored as BCrypt hashes. JWT tokens expire after the configured `JWT_EXPIRATION_MS`.

Regular users can access only their own tasks. Administrative endpoints require the `ADMIN` role.

## API Endpoints

### Authentication

| Method | Endpoint | Authentication | Description |
|---|---|---|---|
| POST | `/auth/register` | No | Register a new user |
| POST | `/auth/login` | No | Authenticate and receive a JWT |

### Tasks

All task endpoints require a valid JWT.

| Method | Endpoint | Description | Successful status |
|---|---|---|---|
| GET | `/tasks` | Get the current user's tasks | `200 OK` |
| GET | `/tasks/{id}` | Get one task | `200 OK` |
| POST | `/tasks` | Create a task | `201 Created` |
| PUT | `/tasks/{id}` | Replace task data | `200 OK` |
| PATCH | `/tasks/{id}/status` | Change task status | `200 OK` |
| DELETE | `/tasks/{id}` | Delete a task | `204 No Content` |

### Filtering and pagination

`GET /tasks` supports these optional query parameters:

| Parameter | Default | Description |
|---|---|---|
| `status` | all statuses | `TODO`, `IN_PROGRESS`, or `DONE` |
| `direction` | `DESC` | Creation-time sorting: `ASC` or `DESC` |
| `page` | `0` | Zero-based page number |
| `size` | `10` | Page size from 1 to 100 |

Example:

```http
GET /tasks?status=TODO&direction=DESC&page=0&size=10
```

## Request Examples

### Register

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Log in

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

Successful login response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

### Create a task

Replace `<accessToken>` with the token returned by the login endpoint.

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn Spring Boot",
    "description": "Finish the Task API project"
  }'
```

### Change task status

```bash
curl -X PATCH http://localhost:8080/tasks/1/status \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DONE"
  }'
```

## Validation and Error Responses

Incoming request DTOs are validated with Jakarta Bean Validation. Examples of enforced rules include:

- valid email format;
- passwords between 8 and 72 characters;
- non-blank task titles;
- task titles up to 100 characters;
- task descriptions up to 1000 characters;
- valid pagination parameters.

Application errors are returned in a consistent JSON format:

```json
{
  "timestamp": "2026-09-19T20:16:29",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required",
  "path": "/tasks"
}
```

## API Documentation

Swagger UI is available while the application is running:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI can also authorize protected requests. Use the JWT returned by `/auth/login` in the **Authorize** dialog.

## Monitoring

Spring Boot Actuator provides application health and runtime information.

Public endpoints:

| Endpoint | Description |
|---|---|
| `/actuator/health` | Application health status |
| `/actuator/info` | Basic application information |

The `/actuator/metrics` endpoints require a user with the `ADMIN` role.

## Database Migrations

Flyway manages the PostgreSQL schema. Migrations are stored in:

```text
src/main/resources/db/migration
```

They run automatically when the application starts. Hibernate validates the mapped entities against the migrated database schema.

## Testing

The project contains unit, controller, repository, security, and integration tests.

Testcontainers starts a real temporary PostgreSQL container for database-related tests.

Run all tests with:

```bash
./mvnw verify
```

Docker must be running for Testcontainers-based tests.

## CI/CD

GitHub Actions runs automatically after each push to `main` and for pull requests.

The CI workflow:

1. checks out the repository;
2. installs Java 21;
3. builds the project with Maven;
4. runs the complete test suite.

After a successful CI run on `main`, a second workflow:

1. builds the Docker image;
2. creates images for `linux/amd64` and `linux/arm64`;
3. publishes the image to Docker Hub.

Docker image:

```text
andr1chkol/task-api:latest
```

Pull it with:

```bash
docker pull andr1chkol/task-api:latest
```

## Project Structure

```text
src/main/java/com/andr1chkol/taskapi
├── config       application and security configuration
├── controller   REST controllers
├── dto          request and response models
├── exception    exceptions and global error handling
├── model        JPA entities and enums
├── repository   Spring Data JPA repositories
├── security     JWT authentication components
└── service      application logic

src/main/resources
├── db/migration Flyway migrations
└── application.properties
```

## Author

Created by [Andrii Lazaresku](https://github.com/andr1chkol) as a practical Java backend development project.
