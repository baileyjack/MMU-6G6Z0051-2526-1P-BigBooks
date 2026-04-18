package jbailey.stu.mmu.bigbook.commons.util;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sanitisation class used for input to prevent security holes
 * such as Cross-Site Scripting and SQL injection attacks.
 * The sanitisation here involves cleansing user input by removing or escaping
 * harmful content while preserving valid data integrity. This is an important layer in
 * an enterprise application to protect against malicious input.
 * OWASP's {@link AntiSamy} is used for robust HTML sanitisation.
 */
public class InputSanitiser {

    private static final Logger logger = LoggerFactory.getLogger(InputSanitiser.class);
    private static final AntiSamy owasp;

    static {
        try {
            // Load default AntiSamy policy (antisamy slashdot)
            Policy policy = Policy.getInstance();
            owasp = new AntiSamy(policy);
        } catch (PolicyException e) {
            throw new RuntimeException("Failed to initialize AntiSamy sanitation policy", e);
        }
    }

    /**
     * Sanitises a {@code String} by trimming whitespace and using {@link AntiSamy} for HTML.
     * This prevents Cross Site Scripting by stripping malicious HTML/script content while allowing safe HTML.
     * @param valueToSanitise is the raw input {@code String}
     * @return sanitised {@code String}, or {@code null} if the input is {@code null}
     */
    public static String sanitiseString(String valueToSanitise) {
        if (valueToSanitise == null) {
            return null;
        }
        String trimmedString = valueToSanitise.trim();
        try {
            CleanResults cleanResults = owasp.scan(trimmedString);
            String cleanHtml = cleanResults.getCleanHTML();
            if (cleanResults.getErrorMessages().size() > 0) {
                logger.warn("AntiSamy cleaned input with errors: {}", cleanResults.getErrorMessages());
            }
            return cleanHtml;
        } catch (ScanException | PolicyException e) {
            logger.error("Error sanitizing input with AntiSamy", e);
            // Fallback: return trimmed input if sanitisation fails
            return trimmedString;
        }
    }

    /**
     * Sanitises an array of {@code String}s by applying sanitisation policies to each element.
     * This is especially useful for {@code List}s like genres or characters.
     * @param inputArray is the array of raw {@code String}s
     * @return an array of sanitised {@code String}s
     */
    public static String[] sanitiseStringArray(String[] inputArray) {
        if (inputArray == null) {
            return null;
        }
        String[] sanitisedStrings = new String[inputArray.length];
        for (int i = 0; i < inputArray.length; i++) {
            sanitisedStrings[i] = sanitiseString(inputArray[i]);
        }
        return sanitisedStrings;
    }
}
