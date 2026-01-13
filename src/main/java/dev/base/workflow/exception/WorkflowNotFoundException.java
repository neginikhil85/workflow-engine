package dev.base.workflow.exception;

import dev.base.workflow.util.StringUtils;
import org.springframework.http.HttpStatus;

public class WorkflowNotFoundException extends ApplicationException {
    public WorkflowNotFoundException(String id) {
        super(StringUtils.concat("Workflow not found: ", id), HttpStatus.NOT_FOUND, "WORKFLOW_NOT_FOUND");
    }
}
