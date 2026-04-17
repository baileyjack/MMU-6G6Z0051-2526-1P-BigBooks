package jbailey.stu.mmu.bigbook.rest.format;

import jbailey.stu.mmu.bigbook.rest.serializer.*;
import lombok.extern.slf4j.Slf4j;

// todo: we could move this to a more DI focussed class. Should we really have a case at the end, for example?

/**
 * Content negotiation class for REST API responses.
 * It analyzes the HTTP Accept header from client requests and determines
 * the serialiser which is to be used in the response.
 * Supported formats currently include application/json (default), application/xml, text/plain (output only)
 * The olny things that would need to be extended to add a new content type are this class and the serialiser to use
 */
@Slf4j
public class RESTFormatNegotiator {

    /**
     * Supported response format types.
     */
    public enum ResponseFormat {
        JSON("application/json"),
        XML("application/xml"),
        TEXT("text/plain");

        private final String mimeType;

        ResponseFormat(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return mimeType;
        }
    }

    /**
     * Determine the format from the Accept header.
     * The header specifies the formats the client can understand.
     * For example: "Accept: application/json" says the client wants JSON.
     * If multiple formats are requested, the first supported format is used.
     * If no Accept header is provided, JSON is used as the default.
     * @param acceptHeader is the HTTP Accept header value
     * @return The ResponseFormat to use for this response.
     */
    public static ResponseFormat negotiateFormat(String acceptHeader) {
        // Default to JSON if no header provided.
        if (acceptHeader == null || acceptHeader.isEmpty()) {
            log.debug("No Accept header provided. Defaulting to JSON.");
            return ResponseFormat.JSON;
        }

        // Parse Accept header
        String[] acceptedTypes = acceptHeader.split(",");

        for (String acceptedType : acceptedTypes) {
            String cleanType = acceptedType.trim().toLowerCase();

            if (cleanType.contains("application/xml") || cleanType.contains("text/xml")) {
                log.debug("Client accepts XML format.");
                return ResponseFormat.XML;
            } else if (cleanType.contains("text/plain")) {
                log.debug("Client accepts TEXT format.");
                return ResponseFormat.TEXT;
            } else if (cleanType.contains("application/json")) {
                log.debug("Client accepts JSON format.");
                return ResponseFormat.JSON;
            }
        }

        // If no recognized format, default JSON.
        log.debug("No recognized format in Accept header. Defaulting to JSON.");
        return ResponseFormat.JSON;
    }

    /**
     * Get a serializer instance for the specified format.
     * 
     * @param <T> is a generic which defines the data type to be serialized.
     * @param format as the ResponseFormat object to get a serializer for.
     * @return A ResponseSerializer instance for the specified format.
     */
    @SuppressWarnings("unchecked")
    public static <T> ResponseSerializer<T> getSerializer(ResponseFormat format) {
        return switch (format) {
            case JSON -> (ResponseSerializer<T>) new JsonSerializer<>();
            case XML -> (ResponseSerializer<T>) new XmlSerializer<>();
            case TEXT -> (ResponseSerializer<T>) new TextSerializer<>();
        };
    }
}

