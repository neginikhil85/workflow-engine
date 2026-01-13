package dev.base.workflow.util;

/**
 * Utility class for String operations.
 * Provides generic concatenation and formatting methods.
 */
public final class StringUtils {

    private StringUtils() {
        // Prevent instantiation
    }

    /**
     * Concatenates multiple objects into a single String using StringBuilder.
     * Null objects are appended as "null".
     *
     * @param parts variable arguments to concatenate
     * @return concatenated string
     */
    public static String concat(Object... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

    /**
     * Formats a string by replacing occurrences of "{}" with arguments.
     * Arguments are substituted in order.
     *
     * @param template string template with "{}" placeholders
     * @param args     arguments to substitute
     * @return formatted string
     */
    public static String format(String template, Object... args) {
        if (template == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }

        StringBuilder sb = new StringBuilder();
        int argsIndex = 0;
        int lastIndex = 0;

        while (argsIndex < args.length) {
            int placeholderIndex = template.indexOf("{}", lastIndex);
            if (placeholderIndex == -1) {
                break;
            }
            sb.append(template, lastIndex, placeholderIndex);
            sb.append(args[argsIndex++]);
            lastIndex = placeholderIndex + 2;
        }

        sb.append(template.substring(lastIndex));
        return sb.toString();
    }
}
