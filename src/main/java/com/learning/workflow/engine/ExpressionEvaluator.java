package com.learning.workflow.engine;

import com.learning.workflow.model.core.ExecutionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Evaluates runtime conditions and templates using Spring Expression Language (SpEL).
 *
 * Supports:
 * - Boolean condition evaluation
 * - Template parsing with ${...}
 * - Any return type (Map, List, Object, primitives)
 * - Safe stringification
 */
@Component
public class ExpressionEvaluator {

    private static final String INPUT_VAR = "input";
    private static final String CONTEXT_VAR = "ctx";

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Evaluates a boolean SpEL expression.
     *
     * Example:
     *   expr: "input.amount > 100 && ctx.status == 'ACTIVE'"
     */
    public boolean evaluate(String expr, Object input, ExecutionContext ctx) {
        if (expr == null || expr.isBlank()) {
            return false;
        }

        StandardEvaluationContext context = buildContext(input, ctx);
        Boolean result = parser.parseExpression(expr).getValue(context, Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Parses a template expression supporting ${...}
     *
     * Example:
     *   "Order ${input.id} created with data ${input}"
     */
    public String parseTemplate(String template, Object input, ExecutionContext ctx) {
        if (template == null) {
            return null;
        }

        StandardEvaluationContext context = buildContext(input, ctx);

        Object value = parser
                .parseExpression(template, new TemplateParserContext("${", "}"))
                .getValue(context, Object.class);   // IMPORTANT: Object, not String

        return stringify(value);
    }

    /**
     * Builds SpEL evaluation context
     */
    private StandardEvaluationContext buildContext(Object input, ExecutionContext ctx) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(new MapAccessor());
        context.setVariable(INPUT_VAR, input);
        context.setVariable(CONTEXT_VAR, ctx);
        return context;
    }

    /**
     * Converts any object to String safely
     */
    private String stringify(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String s) {
            return s;
        }

        try {
            // JSON for Map / List / POJO
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            // Fallback to toString()
            return String.valueOf(value);
        }
    }
}