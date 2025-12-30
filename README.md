# Workflow Engine

A flexible, extensible workflow automation engine built with Spring Boot, MongoDB, and modern design patterns.

## Features

✅ **Graph-based Workflow Engine** - Visual workflow builder with drag-and-drop interface
✅ **Plugin Architecture** - Extensible node executors using plugin pattern
✅ **Adapter Pattern** - Easy integration with external services (HTTP, Slack, Teams, WhatsApp, etc.)
✅ **RESTful API** - Complete REST API for workflow management
✅ **MongoDB Persistence** - Workflows and execution history stored in MongoDB
✅ **Conditional Routing** - SpEL-based conditional edges for dynamic workflow paths
✅ **Beautiful UI** - Modern drag-and-drop workflow builder interface

## Architecture

### Design Patterns Used

1. **Plugin Pattern** - All node executors implement the `NodeExecutor` interface for extensibility
2. **Adapter Pattern** - Integration adapters (`IntegrationAdapter`, `MessagingAdapter`) for external services
3. **Registry Pattern** - `NodeTypeRegistry` and `IntegrationAdapterRegistry` for dynamic discovery
4. **Strategy Pattern** - Different node types use different execution strategies
5. **Repository Pattern** - MongoDB repositories for data persistence

### Core Components

- **WorkflowEngine** - Graph-based workflow execution engine
- **NodeExecutor** - Interface for all node executors
- **IntegrationAdapter** - Interface for external service integrations
- **WorkflowDefinition** - Complete workflow model with nodes and edges
- **ExpressionEvaluator** - SpEL-based conditional evaluation

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- MongoDB (running on localhost:27017 by default)

### Running the Application

1. Start MongoDB:
```bash
mongod
```

2. Run the application:
```bash
mvn spring-boot:run
```

3. Access the workflow builder:
```
http://localhost:8080/builder
```

## API Endpoints

### Workflows
- `GET /api/workflows` - List all workflows
- `GET /api/workflows/{id}` - Get workflow by ID
- `POST /api/workflows` - Create workflow
- `PUT /api/workflows/{id}` - Update workflow
- `DELETE /api/workflows/{id}` - Delete workflow
- `POST /api/workflows/{id}/execute` - Execute workflow
- `GET /api/workflows/{id}/executions` - Get execution history

### Node Types
- `GET /api/nodes/types` - Get available node types
- `GET /api/nodes/adapters` - Get available integration adapters

## Adding New Integrations

### Step 1: Create Adapter Implementation

```java
@Component
public class MyServiceAdapter implements MessagingAdapter {
    
    @Override
    public String getAdapterId() {
        return "myservice";
    }
    
    @Override
    public String getName() {
        return "My Service";
    }
    
    @Override
    public Object execute(Map<String, Object> config, Object data) {
        // Implementation
    }
    
    // Implement other required methods...
}
```

### Step 2: Create Node Executor (if needed)

```java
@Component
public class MyServiceExecutor implements NodeExecutor {
    
    private final IntegrationAdapterRegistry adapterRegistry;
    
    @Override
    public NodeType getSupportedNodeType() {
        return IntegrationNodeType.MY_SERVICE;
    }
    
    @Override
    public Object execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        var adapter = adapterRegistry.getAdapter("myservice")
            .orElseThrow(() -> new RuntimeException("Adapter not found"));
        return adapter.execute(node.getConfig(), input);
    }
}
```

The adapter will be automatically discovered and registered!

## Node Types

### Triggers
- Webhook
- Cron
- File Change

### Integration
- HTTP Call
- Kafka
- Artemis Queue
- ActiveMQ

### Validation
- Required Fields
- Schema Check
- Business Rule

### Transformation
- JSON Mapper
- Expression

### Notification
- Log
- Email
- Slack (adapter ready)
- Teams (adapter ready)
- WhatsApp (adapter ready)

### Control Flow
- If
- Switch
- Loop
- Delay

## Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/workflowdb
server:
  port: 8080
```

## License

This project is for learning purposes.

