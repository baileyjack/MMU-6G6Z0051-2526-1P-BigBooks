package jbailey.stu.mmu.bigbook.mvc.util;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Does consistent server error handling. Manages logs seperately
 * and sends a reasonable error message to the client
 */
public final class MVCErrorUtility {
    private MVCErrorUtility() { }

    public static void handleServerError(HttpServletResponse response, Logger log, Exception e, String userMessage) {
        log.error("An unexpected server error occurred", e);
        String errorMessage = (userMessage == null || userMessage.isEmpty()) ? "An unexpected error occurred" : userMessage;
        try {
            // SC_INTERNAL_SERVER_ERROR is from Jakarta (500 HTTP Error)
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
        } catch (IOException errorException) {
            log.error("Failed to send error response to the client",errorException);
        }
    }
}
