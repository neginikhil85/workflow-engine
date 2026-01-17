# Backend Coding Style Guidelines

## Core Philosophy

> **Layered Architecture with Single Responsibility**
> 
> Each class has one job. Layers communicate through interfaces.
> SOLID principles are non-negotiable.

---

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Controller Layer                        │
│  (REST endpoints, request/response handling, thin layer)    │
├─────────────────────────────────────────────────────────────┤
│                     Service Layer                            │
│  (Business logic, orchestration, transactions)              │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                             │
│  (Core entities, executors, adapters, engine)               │
├─────────────────────────────────────────────────────────────┤
│                     Repository Layer                         │
│  (Data access, MongoDB operations)                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Package Structure

```
dev.base.workflow/
├── config/           # Configuration classes (Security, CORS)
├── controller/       # REST controllers
├── service/          # Business logic services
├── domain/
│   ├── engine/       # Workflow execution engine
│   ├── executor/     # Node executors (trigger/, integration/, logic/)
│   └── core/         # Core domain classes
├── mongo/
│   ├── collection/   # MongoDB entities (@Document)
│   └── repository/   # Spring Data repositories
├── model/
│   ├── dto/          # Request/Response DTOs
│   ├── nodetype/     # Node type enums
│   └── core/         # Domain models (non-MongoDB)
├── security/         # Auth, JWT, OAuth2
├── exception/        # Custom exceptions
└── constant/         # Constants and enums
```

---

## Class Patterns

### 1. Entity (MongoDB Collection)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    private String name;
    private LocalDateTime createdAt;
    
    @Builder.Default
    private boolean active = true;
}
```

**Rules**:
- Use Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@Id` on primary key
- `@Indexed` for queried fields
- `@Builder.Default` for default values

### 2. Repository

```java
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndActiveTrue(String id);
    List<User> findByActiveTrue();
}
```

**Rules**:
- Extend `MongoRepository<Entity, IdType>`
- Method naming: `findBy<Field><Condition>`
- Return `Optional<T>` for single results

### 3. Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    public User getUser(String id) {
        return userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        log.info("Creating user: {}", user.getEmail());
        return userRepository.save(user);
    }
}
```

**Rules**:
- `@RequiredArgsConstructor` for constructor injection
- `@Slf4j` for logging
- Business logic lives here
- Throw domain exceptions

### 4. Controller

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable String id) {
        return ApiResponse.success(userService.getUser(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<User> createUser(@RequestBody User user) {
        return ApiResponse.success(userService.createUser(user), "User created");
    }
}
```

**Rules**:
- THIN controllers - delegate to service
- Use `ApiResponse<T>` wrapper
- `@ResponseStatus` for POST/DELETE
- No business logic

---

## SOLID Compliance

### S - Single Responsibility

```java
// ✅ GOOD: Each service has one job
UserService           → User CRUD
WorkflowService       → Workflow execution orchestration
WorkflowQueryService  → Workflow queries only

// ❌ BAD: God service doing everything
WorkflowService       → CRUD + Execution + Scheduling + Queries
```

### O - Open/Closed

```java
// ✅ GOOD: New nodes via new executor, no engine changes
public interface NodeExecutor {
    NodeType getSupportedNodeType();
    NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx);
}

@Component
public class HttpCallExecutor implements NodeExecutor { ... }

@Component  // Just add new class, engine unchanged
public class SlackExecutor implements NodeExecutor { ... }
```

### D - Dependency Inversion

```java
// ✅ Service depends on interface, not implementation
@RequiredArgsConstructor
public class WorkflowEngine {
    private final NodeTypeRegistry registry;  // Abstraction
    
    public void run() {
        NodeExecutor executor = registry.resolve(nodeType);  // Injected
        executor.execute(...);
    }
}
```

---

## Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Entity | Singular noun | `User`, `WorkflowDefinition` |
| Repository | Entity + Repository | `UserRepository` |
| Service | Entity/Feature + Service | `UserService`, `WorkflowExecutionService` |
| Controller | Entity + Controller | `UserController` |
| Exception | Condition + Exception | `UserNotFoundException` |
| Executor | NodeType + Executor | `HttpCallExecutor`, `CronTriggerExecutor` |

---

## Facade Pattern for Complex Features

When a service grows large, split and facade:

```java
// Facade service (public API)
@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final WorkflowExecutionService executionService;
    private final WorkflowManagementService managementService;
    private final WorkflowQueryService queryService;

    public Object execute(String id, Object input) {
        return executionService.executeWorkflow(id, input);
    }
    
    public WorkflowDefinition save(WorkflowDefinition wf) {
        return managementService.saveWorkflow(wf);
    }
}
```

---

## Code Quality Rules

1. **No @Autowired on fields** - Use constructor injection
2. **Lombok everywhere** - No manual getters/setters
3. **Log important operations** - Use `@Slf4j`
4. **Throw meaningful exceptions** - Custom exception classes
5. **No null returns** - Use `Optional<T>`
6. **Max 15 lines per method** - Extract helper methods if longer
7. **Package by feature, not layer** - `domain/executor/` not `executors/`
8. **No magic values in code** - Use constants or config

---

## Configuration Management

### Rule: @ConfigurationProperties for Grouped Configs

When you have multiple related config values, use `@ConfigurationProperties` with a dedicated class:

```java
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret;
        private long expiration = 86400000;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
    }
}
```

**Usage in services:**
```java
@RequiredArgsConstructor
public class JwtService {
    private final AppProperties appProperties;
    
    public void doSomething() {
        String secret = appProperties.getJwt().getSecret();
    }
}
```

### Rule: @Value for Single Standalone Values

Only use `@Value` for truly single, unrelated config values:

```java
@Value("${server.port}")
private int port;
```

### Rule: Constants vs Config

| Type | Where | Example |
|------|-------|---------|
| **Immutable business constants** | `constant/` package | OAuth2 provider names, attribute keys |
| **Configurable values** | `application.yml` | URLs, paths, timeouts, lists |
| **Secrets** | Environment variables | API keys, passwords |

**Constants file example:**
```java
public final class SecurityConstants {
    // ✅ Immutable - defined by OAuth2 spec
    public static final String PROVIDER_GOOGLE = "google";
    public static final String ATTR_EMAIL = "email";
    
    // ❌ DON'T put configurable values here
    // public static final String[] AUTH_PATHS = {...}
}
```

**application.yml example:**
```yaml
app:
  security:
    public-paths:
      - /
      - /error
    auth-paths:
      - /auth/**
      - /oauth2/**
```

---

## API Response Pattern

```java
@Data
@Builder
public class ApiResponse<T> {
    private T data;
    private String message;
    private boolean success;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .success(true)
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .data(data)
                .message(message)
                .success(true)
                .build();
    }
}
```

All endpoints return `ApiResponse<T>` for consistency.
