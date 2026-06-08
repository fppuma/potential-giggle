# Customer Service

REST microservice for managing customers, built with Spring Boot 3.3.x and Java 21.

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Spring Data JPA
- Spring Web (REST)
- Maven Wrapper
- PostgreSQL (production) / H2 (dev & test)

## Prerequisites

- Java 21+
- PostgreSQL (production profile only)

## Running the application

### Development profile (H2 in-memory database)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The H2 console will be available at `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:devdb`
- Username: `sa`
- Password: *(empty)*

### Production profile (PostgreSQL)

Set the environment variables before running:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
./mvnw spring-boot:run
```

## Build

```bash
# Build and run tests
./mvnw clean package

# Build skipping tests
./mvnw clean package -DskipTests
```

## API Endpoints

Base URL: `http://localhost:8080/api/v1/customers`

| Method   | Path   | Description        |
|----------|--------|--------------------|
| `POST`   | `/`    | Create a customer  |
| `GET`    | `/`    | List all customers |
| `GET`    | `/{id}`| Get by ID          |
| `PUT`    | `/{id}`| Update a customer  |
| `DELETE` | `/{id}`| Delete a customer  |

## curl Examples

**Create a customer:**
```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john.doe@example.com", "phone": "555-1234"}'
```

**List all customers:**
```bash
curl http://localhost:8080/api/v1/customers
```

**Get customer by ID:**
```bash
curl http://localhost:8080/api/v1/customers/1
```

**Update a customer:**
```bash
curl -X PUT http://localhost:8080/api/v1/customers/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Jane Doe", "email": "jane.doe@example.com", "phone": "555-5678"}'
```

**Delete a customer:**
```bash
curl -X DELETE http://localhost:8080/api/v1/customers/1
```
