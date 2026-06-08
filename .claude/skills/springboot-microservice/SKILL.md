---
name: springboot-microservice
description: Creates Spring Boot microservices with standard package structure, Repository, Service and Controller classes
---

# Spring Boot Microservice Skill

## Stack and versions

- Java 21
- Spring Boot 3.3.x
- Spring Data JPA
- Spring Web (REST)
- Maven as dependency manager
- Database: PostgreSQL (production), H2 (tests)

## Package structure

Always use the following base structure.
Replace `{microservice}` with the actual name (e.g., `customer`):

```
com.mycompany.{microservice}
├── controller/        ← REST endpoints
├── service/           ← business logic
│   └── impl/          ← implementations
├── repository/        ← JPA interfaces
├── model/             ← JPA entities
├── dto/               ← data transfer objects
│   ├── request/
│   └── response/
├── mapper/            ← entity ↔ dto conversion
├── exception/         ← custom exceptions
└── config/            ← configuration (CORS, beans, etc.)
```

## Naming conventions

| Layer       | Suffix          | Example                  |
|-------------|-----------------|--------------------------|
| Entity      | (none)          | `Customer`               |
| Repository  | `Repository`    | `CustomerRepository`     |
| Service     | `Service`       | `CustomerService`        |
| Impl        | `ServiceImpl`   | `CustomerServiceImpl`    |
| Controller  | `Controller`    | `CustomerController`     |
| Input DTO   | `Request`       | `CreateCustomerRequest`  |
| Output DTO  | `Response`      | `CustomerResponse`       |

## Template: Repository

```java
package com.mycompany.{microservice}.repository;

import com.mycompany.{microservice}.model.{Entity};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface {Entity}Repository extends JpaRepository<{Entity}, Long> {
    // custom query methods here
}
```

## Template: Service (interface)

```java
package com.mycompany.{microservice}.service;

import com.mycompany.{microservice}.dto.request.Create{Entity}Request;
import com.mycompany.{microservice}.dto.response.{Entity}Response;
import java.util.List;

public interface {Entity}Service {
    {Entity}Response create({Entity}Request request);
    {Entity}Response findById(Long id);
    List<{Entity}Response> findAll();
    {Entity}Response update(Long id, {Entity}Request request);
    void delete(Long id);
}
```

## Template: ServiceImpl

```java
package com.mycompany.{microservice}.service.impl;

import com.mycompany.{microservice}.exception.ResourceNotFoundException;
import com.mycompany.{microservice}.repository.{Entity}Repository;
import com.mycompany.{microservice}.service.{Entity}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class {Entity}ServiceImpl implements {Entity}Service {

    private final {Entity}Repository repository;
    private final {Entity}Mapper mapper;

    @Override
    public {Entity}Response create({Entity}Request request) {
        var entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public {Entity}Response findById(Long id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("{Entity}", id));
    }
}
```

## Template: Controller

```java
package com.mycompany.{microservice}.controller;

import com.mycompany.{microservice}.dto.request.Create{Entity}Request;
import com.mycompany.{microservice}.dto.response.{Entity}Response;
import com.mycompany.{microservice}.service.{Entity}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{entities}")
@RequiredArgsConstructor
public class {Entity}Controller {

    private final {Entity}Service service;

    @PostMapping
    public ResponseEntity<{Entity}Response> create(@Valid @RequestBody Create{Entity}Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<{Entity}Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<{Entity}Response> update(
            @PathVariable Long id,
            @Valid @RequestBody Create{Entity}Request request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Template: GlobalExceptionHandler

Every microservice must include a `GlobalExceptionHandler` in the `exception` package.

- Do NOT use `@ResponseStatus` on exception classes — the handler is the single source of truth for HTTP status codes.
- All error responses share the same structure: `timestamp`, `status`, and `error` (or `errors` for validation).

```java
package com.mycompany.{microservice}.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
```

## Template: ResourceNotFoundException

```java
package com.mycompany.{microservice}.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }
}
```

### Error response shapes

**Not found / generic error:**
```json
{
  "timestamp": "2026-06-07T10:30:00",
  "status": 404,
  "error": "Customer not found with id: 99"
}
```

**Validation error (`@Valid` failed):**
```json
{
  "timestamp": "2026-06-07T10:30:00",
  "status": 400,
  "errors": {
    "email": "must be a well-formed email address",
    "name": "must not be blank"
  }
}
```

## Maven Wrapper setup

After creating the project, generate the Maven Wrapper so the project is self-contained (no global Maven required):

```bash
mvn wrapper:wrapper
```

This creates:
```
{microservice}/
├── mvnw        ← Unix/Linux/macOS script
├── mvnw.cmd    ← Windows script
└── .mvn/
    └── wrapper/
        └── maven-wrapper.properties
```

Always commit these files to version control.

## Profiles and configuration files

Use Spring profile-specific `application-{profile}.yml` files placed in `src/main/resources/`.

### production (default) — `application.yml`

```yaml
spring:
  application:
    name: {microservice}-service
  datasource:
    url: jdbc:postgresql://localhost:5432/{microservice}db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080
```

### dev — `application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true
      path: /h2-console
```

### test — `src/test/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
```

### H2 dependency scope

When using a dev profile (not just tests), H2 must be `runtime` scope, not `test`:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Activating the dev profile

```bash
# Via Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Via environment variable
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# Via JVM argument
java -jar target/{microservice}-service.jar -Dspring.profiles.active=dev
```

## README.md template

Every microservice must include a `README.md` at the project root with the following structure:

```markdown
# {Microservice} Service

Brief description of what this service does.

## Tech Stack
- Java 21
- Spring Boot 3.3.x
- Spring Data JPA / Spring Web
- Maven Wrapper
- PostgreSQL (production) / H2 (dev & test)

## Prerequisites
- Java 21+
- PostgreSQL (production profile only)

## Running the application

### Development profile (H2 in-memory database)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

H2 console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:devdb, user: sa, password: empty)

### Production profile (PostgreSQL)
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
./mvnw spring-boot:run

## Build
./mvnw clean package
./mvnw clean package -DskipTests

## API Endpoints

Base URL: http://localhost:8080/api/v1/{entities}

| Method   | Path    | Description       |
|----------|---------|-------------------|
| POST     | /       | Create            |
| GET      | /       | List all          |
| GET      | /{id}   | Get by ID         |
| PUT      | /{id}   | Update            |
| DELETE   | /{id}   | Delete            |

## curl Examples
(include create and list as minimum)
```

## General rules

- Use `@RequiredArgsConstructor` from Lombok, never field injection (`@Autowired`)
- Controllers never access the Repository directly
- Services always return DTOs, never entities
- Use `@Valid` on all endpoints that receive a body
- Every "not found" exception throws `ResourceNotFoundException`
- Endpoints follow the pattern `/api/v1/{resource-in-plural}`
- Include `@Transactional(readOnly = true)` on read-only methods
