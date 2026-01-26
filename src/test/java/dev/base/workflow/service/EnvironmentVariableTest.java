package dev.base.workflow.service;

import dev.base.workflow.domain.engine.ExpressionEvaluator;
import dev.base.workflow.model.core.ExecutionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentVariableTest {

    private ExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ExpressionEvaluator();
    }

    @Test
    void testEnvVariableInjection() {
        // 1. Simulate Global Context
        Map<String, String> globalEnv = new HashMap<>();
        globalEnv.put("API_KEY", "secret_123");
        globalEnv.put("BASE_URL", "https://api.example.com");

        // 2. Simulate Execution Context injection (done by WorkflowEngine)
        ExecutionContext ctx = new ExecutionContext();
        ctx.put("env", globalEnv);

        // 3. Test Expression Evaluation
        String expr = "env.API_KEY == 'secret_123'";
        boolean result = evaluator.evaluate(expr, new Object(), ctx);

        Assertions.assertTrue(result, "Expression should evaluate to true accessing env.API_KEY");
    }

    @Test
    void testTemplateParsing() {
        // 1. Simulate Global Context
        Map<String, String> globalEnv = new HashMap<>();
        globalEnv.put("USER", "admin");

        // 2. Simulate Execution Context
        ExecutionContext ctx = new ExecutionContext();
        ctx.put("env", globalEnv);

        // 3. Test Template
        String template = "Hello ${env.USER}";
        String result = evaluator.parseTemplate(template, new Object(), ctx);

        Assertions.assertEquals("Hello admin", result);
    }
}
