package dev.base.workflow.model.dto.response.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterDTO {
    private String id;
    private String name;
    private String type;
}
