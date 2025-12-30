package com.learning.workflow.engine;

import com.learning.workflow.model.core.ExecutionContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Evaluates runtime conditions using Spring Expression Language.
 */
@Component
public class ExpressionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private static final String INPUT_VAR = "input";
    private static final String CONTEXT_VAR = "ctx";

    public boolean evaluate(String expr, Object input, ExecutionContext ctx) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(new org.springframework.context.expression.MapAccessor());
        context.setVariable(INPUT_VAR, input);
        context.setVariable(CONTEXT_VAR, ctx);
        Boolean result = parser.parseExpression(expr).getValue(context, Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public String parseTemplate(String template, Object input, ExecutionContext ctx) {
        if (template == null)
            return null;
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.addPropertyAccessor(new org.springframework.context.expression.MapAccessor());
        context.setVariable(INPUT_VAR, input);
        context.setVariable(CONTEXT_VAR, ctx);

        // Use TemplateParserContext for #{...} style or similar
        // Since SpEL supports templating with ParserContext, we define one.
        // Default ParserContext looks for #{...} but we can use ${...} if we want.
        // Let's stick to standard Spring #{...} or customized.
        // Actually, SpEL default template is #{...}. Let's support that or simple regex
        // replacement.
        // Simple regex might be safer/easier for user: ${input.field}

        // Let's implement robust SpEL templating:
        return parser
                .parseExpression(template, new org.springframework.expression.common.TemplateParserContext("${", "}"))
                .getValue(context, String.class);
    }
}
