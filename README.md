[![Java CI](https://github.com/martin-rohwedder/Todolist-Backend/actions/workflows/ci.yml/badge.svg)](https://github.com/martin-rohwedder/Todolist-Backend/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-25-d15e5c?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18.4-316192?logo=postgresql&logoColor=white)

# Todolist Backend

A todolist API created with Java and Spring Boot 4.1.1. It uses PostgreSQL 18.4 for data storage.

## Run Locally

### Prerequisites

- Java 25
- Docker Desktop
- Intellij IDEA (Recommended)

### 1. Clone the repository

- `git clone https://github.com/martin-rohwedder/Todolist-Backend.git`
- `cd Todolist-Backend`

### 2. Open and run the project in IntelliJ IDEA

Spring Boot’s Docker Compose integration will automatically start the PostgreSQL container, apply the Flyway migrations, and connect the application to the database.

## How to deploy

The project docker image can be pulled from (GHCR) GitHub Container Registry.

### Prerequisites

- Docker Desktop/Docker Engine

### 1. Create `compose.yml`

Create a `compose.yml` file with the following:

```yaml
services:
  postgres:
    image: 'postgres:18.4-alpine3.24'
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB_NAME}
      POSTGRES_USER: ${POSTGRES_DB_USER}
      POSTGRES_PASSWORD: ${POSTGRES_DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql
    ports:
      - '5432:5432'
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}" ]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s

  todolist_backend:
    image: ghcr.io/martin-rohwedder/todolist-backend:latest
    restart: on-failure:3
    depends_on:
      postgres:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB_NAME}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_DB_PASSWORD}
    healthcheck:
      test: [ "CMD", "wget", "--spider", "-q", "http://localhost:8080/actuator/health" ]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 30s

volumes:
  pgdata:
```

### 2. Create `.env` file

Create a `.env` file with the following environment variables (edit credentials to what you prefer)

```dotenv
POSTGRES_DB_NAME = mydatabase
POSTGRES_DB_USER = myuser
POSTGRES_DB_PASSWORD = secret
```

### 3. How to start and stop the application

When the compose and environment file has been created run this command to start and stop the containers:

Start: `docker compose up -d`

Stop: `docker compose down`

## Endpoint Overview

The application is using JWT authentication, which means a `Bearer Token` needs to be applied to the requests after login.

### Authentication

| **Method** | **Endpoint** | **Decription** |
|------------|--------------|----------------|
| POST | /api/auth/register | Register a new user |
| POST | /api/auth/login | Login with an existing user |

### Todolists

| **Method** | **Endpoint** | **Decription** |
|------------|--------------|----------------|
| GET | /api/todolists | Get all todolists |
| GET | /api/todolists/{id} | Get a todolist by id |
| POST | /api/todolists | Create a new todolist |
| PUT | /api/todolists/{id} | Update a todolist |
| DELETE | /api/todolists/{id} | Delete a specific todolist |

### Todolist Items

| **Method** | **Endpoint** | **Decription** |
|------------|--------------|----------------|
| GET | /api/todolists/{todolistId}/items | Get all todolists items |
| GET | /api/todolists/{todolistId}/items/{itemId} | Get a todolist item by id |
| POST | /api/todolists/{todolistId}/items | Create a new todolist item |
| PUT | /api/todolists/{todolistId}/items/{itemId} | Update a todolist item |
| PATCH | /api/todolists/{todolistId}/items/{itemId}/toggleCompleted | Toggle completed status for a todolist item |
| DELETE | /api/todolists/{todolistId}/items/{itemId} | Delete a specific todolist item |

---

&copy; 2026 Martin Rohwedder