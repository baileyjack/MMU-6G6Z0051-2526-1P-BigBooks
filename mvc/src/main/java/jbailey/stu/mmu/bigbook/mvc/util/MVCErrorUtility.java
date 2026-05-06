package jbailey.stu.mmu.bigbook.mvc.util;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Does consistent server error handling. Manages logs seperately
 * and sends a reasonable error message to the client
 */
public final class MVCErrorUtility {
    private MVCErrorUtility() {
    }

    /**
     * Handle a server error. Logs exceptions and sends a 'safe' message to the client.
     */
    public static void handleServerError(HttpServletResponse response, Logger log, Exception e, String userMessage) {
        if (log != null) {
            log.error("Unexpected server error: {}", sanitizeExceptionMessage(e), e);
        }
        String message = (userMessage == null || userMessage.isBlank()) ? "An unexpected error occurred" : userMessage;
        // SC Internal Server Error Below is from Jakarta
        sendSanitizedError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Handle a client error (4xx). Logs a warning message to the server and sends the provided message to the client.
     */
    public static void handleClientError(HttpServletResponse response, Logger log, int statusCode, String userMessage) {
        if (log != null) {
            log.warn("Client error (status={}): {}", statusCode, sanitizeString(userMessage));
        }
        String message = (userMessage == null || userMessage.isBlank()) ? "Invalid request" : userMessage;
        sendSanitizedError(response, statusCode, message);
    }

    /**
     * Helper to send an error response while ensuring I avoid leaking scary exception details to the client.
     * Any IOException while writing the response is logged.
     */
    private static void sendSanitizedError(HttpServletResponse response, int statusCode, String message) {
        try {
            // Send the error to the client in plain text
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/plain; charset=UTF-8");
            response.sendError(statusCode, sanitizeString(message));
        } catch (IOException ioException) {
            // If sending the error fails avoid throwing further exceptions from the error handler.
            // Can't actually do anything else at this point. Maybe cry.
            try {
                response.setStatus(statusCode); // Set response status as a last attempt for an error, otherwise ignore
            } catch (Exception ignoredException) {
                // Ignore any non-handled exceptions
            }
        }
    }

    /**
     * Produce a short, non-null safe string for logging or sending to the client.
     */
    private static String sanitizeString(String s) {
        return s == null ? "" : s;
    }

    /**
     * Produce a message from an exception for logging context without exposing stack traces to clients.
     */
    private static String sanitizeExceptionMessage(Exception e) {
        if (e == null) return "";
        String msg = e.getMessage();
        return (msg == null || msg.isBlank()) ? e.getClass().getSimpleName() : msg;
    }
}