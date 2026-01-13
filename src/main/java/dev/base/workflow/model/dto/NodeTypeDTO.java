package dev.base.workflow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeTypeDTO {
    private String type;
    private String name;
    private String description;
    private String category;
    private Map<String, Object> defaultConfig;
}
