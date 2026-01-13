package dev.base.workflow.domain.executor.notification;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.model.nodetype.NodeType;
import dev.base.workflow.model.nodetype.NotificationNodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import dev.base.workflow.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.base.workflow.constant.WorkflowConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationExecutor implements NodeExecutor {

    private final JavaMailSender mailSender;
    private final dev.base.workflow.domain.engine.ExpressionEvaluator evaluator;

    @Override
    public NodeType getSupportedNodeType() {
        return NotificationNodeType.EMAIL;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        Map<String, Object> config = node.getConfig();

        log.info("DEBUG: Email Node Config: {}", config);

        String to = (String) config.get(CFG_TO);
        String subjectRaw = (String) config.get(CFG_SUBJECT);
        String bodyRaw = (String) config.get(CFG_BODY);

        // Basic validation
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException(StringUtils.concat("Email '", CFG_TO, "' address is required"));
        }

        try {
            // Parse templates
            String subject = evaluator.parseTemplate(subjectRaw != null ? subjectRaw : "Workflow Notification", input,
                    ctx);
            String body = evaluator.parseTemplate(bodyRaw != null ? bodyRaw : "No content", input, ctx);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

            return NodeExecutionResult.success(node.getId(), Map.of(KEY_STATUS, "sent", CFG_TO, to));
        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new RuntimeException(StringUtils.concat("Failed to send email: ", e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                CFG_TO, "lihkinnegi@gmail.com",
                CFG_SUBJECT, "Workflow Alert",
                CFG_BODY, "Hello from Workflow Engine!");
    }
}
