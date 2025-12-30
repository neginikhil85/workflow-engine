package com.learning.workflow.executor.constants;

public interface NodeConstants {

    // Input / Output map keys
    String KEY_URL = "url";
    String KEY_BODY = "body";
    String KEY_TO = "to";
    String KEY_SUBJECT = "subject";

    // Camel endpoints
    String LOG_ENDPOINT = "log:workflow.notification";
    String JSON_TRANSFORM_ROUTE = "direct:jsonTransform";

    // Email SMTP placeholder
    String SMTP_URI = "smtp://smtp.example.com?username=yourUser&password=yourPass";
}
