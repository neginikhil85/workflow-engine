package dev.base.workflow.exception;

import dev.base.workflow.util.StringUtils;
import org.springframework.http.HttpStatus;

public class RunNotFoundException extends ApplicationException {
    public RunNotFoundException(String id) {
        super(StringUtils.concat("Workflow run not found: ", id), HttpStatus.NOT_FOUND, "RUN_NOT_FOUND");
    }
}
