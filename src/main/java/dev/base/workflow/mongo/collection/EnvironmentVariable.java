package dev.base.workflow.mongo.collection;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "environment_variables")
public class EnvironmentVariable {

    @Id
    private String id; // The key (e.g., "API_KEY")

    private String value; // The secret value
    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
