package jbailey.stu.mmu.bigbook.commons.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all date operations with the required ISO 8601 standard (YYYY-MM-DD).
 *
 * All dates displayed to users must be in ISO 8601 format (YYYY-MM-DD).
 * All dates stored in the database must be updated to ISO 8601 format (YYYY-MM-DD).
 * User input MUST be in ISO 8601 format (YYYY-MM-DD).
 * Legacy database formats are parsed and converted internally.
 * When a book with a legacy date format is updated, the date is standardised in the DB.
 * Incomplete dates are normalised: YYYY is formatted to YYYY-01-01, YYYY-MM is formatted to YYYY-MM-01.
 */
@Slf4j
public class DateUtility {

    // Formatter for storing and display
    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Legacy database formatters used for reading existing data
    private static final DateTimeFormatter[] LEGACY_FORMATS = {
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),  // MM/DD/YYYY
        DateTimeFormatter.ofPattern("MM/dd/yy"),    // MM/DD/YY
        DateTimeFormatter.ofPattern("M/d/yyyy"),    // M/D/YYYY
        DateTimeFormatter.ofPattern("M/d/yy"),      // M/D/YY
        DateTimeFormatter.ofPattern("MMM-yyyy"),    // Dec-2002
        DateTimeFormatter.ofPattern("MMM-yy"),      // Dec-02
        DateTimeFormatter.ofPattern("MMM yyyy"),    // Dec 2002
        DateTimeFormatter.ofPattern("MMM yy")       // Dec 02
    };

    /**
     * Parses a {@link String} from the DB and returns it in the ISO 8601 format.
     * It also manages handling of both ISO 8601 formatted dates and legacy DB formats.
     *
     * @param inputDate is the date string from the database
     * @return the ISO 8601 formatted date string or null if parsing fails
     */
    public static String normaliseDateToISO(String inputDate) {
        if (inputDate == null || inputDate.trim().isEmpty()) {
            return null;
        }

        // Trim whitespace
        String trimmedDate = inputDate.trim();

        // Check if the input is in the correct format already
        if (trimmedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                LocalDate.parse(trimmedDate, ISO_DATE_FORMAT);
                return trimmedDate;
            } catch (DateTimeParseException e) {
                log.warn("Date appears to be ISO format but couldn't be parsed: {}", trimmedDate);
                return null;
            }
        }

        LocalDate parsedDate = null;

        // Iterate over legacy formats
        for (DateTimeFormatter formatter : LEGACY_FORMATS) {
            try {
                parsedDate = LocalDate.parse(trimmedDate, formatter);
                break;
            } catch (DateTimeParseException e) {
                // Fail quietly and try next format
            }
        }

        // If legacy formatters failed, try cases for incomplete dates
        if (parsedDate == null) {
            // year only
            if (trimmedDate.matches("\\d{4}")) {
                try {
                    int year = Integer.parseInt(trimmedDate);
                    parsedDate = LocalDate.of(year, 1, 1);
                    log.info("Parsed year-only date: {} mapped to {}", trimmedDate, parsedDate);
                } catch (Exception e) {
                    log.warn("Failed to parse year-only date: {}", trimmedDate, e);
                }
            }
            // Try month-year format (MMM-YY)
            else if (trimmedDate.matches("[A-Za-z]+-\\d{2}")) {
                try {
                    parsedDate = LocalDate.parse(trimmedDate + "-01", DateTimeFormatter.ofPattern("MMM-yy-dd"));
                    log.info("Parsed month and year date: {}", trimmedDate);
                } catch (Exception e) {
                    log.warn("Failed to parse month and year date: {}", trimmedDate, e);
                }
            }
        }

        if (parsedDate == null) {
            log.warn("Could not parse date: {}", trimmedDate);
            return null;
        }

        // Convert to ISO 8601 and return
        return parsedDate.format(ISO_DATE_FORMAT);
    }

    /**
     * Format a date string to ISO 8601 for date display purposes
     * @param inputDate as the date string to format
     * @return ISO 8601 formatted date string or an empty string if it's invalid
     */
    public static String displayISODate(String inputDate) {
        String normaisedDate = normaliseDateToISO(inputDate);
        return normaisedDate != null ? normaisedDate : "";
    }

    /**
     * Validate a date string for input.
     * @param dateInput as the date string to validate (must be YYYY-MM-DD)
     * @return A list of validation errors
     */
    public static List<String> validateDateInput(String dateInput) {
        List<String> dateErrorlist = new ArrayList<>();
        if (dateInput == null || dateInput.trim().isEmpty()) {
            dateErrorlist.add("Input date is required");
            return dateErrorlist;
        }
        String trimmedDate = dateInput.trim();
        // must be in ISO 8601 format
        if (!trimmedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            dateErrorlist.add("Date must be in YYYY-MM-DD format (e.g., 2000-01-01)");
            return dateErrorlist;
        }

        try {
            LocalDate date = LocalDate.parse(trimmedDate, ISO_DATE_FORMAT);
            
            // is date in the future?
            if (date.isAfter(LocalDate.now())) {
                dateErrorlist.add("Date cannot be in the future");
            }
        } catch (DateTimeParseException e) {
            dateErrorlist.add("Invalid date value provided");
        }
        return dateErrorlist;
    }

    /**
     * Defined the message shown when a date is invalid or in the future. This is used in UI.
     */
    public static final String INVALID_DATE_MESSAGE = "No valid date found for this entry. The entry should be updated";

    /**
     * Return a {@code boolean} value for whether a given date is valid for display.
     * A date is valid if it can be normalised to ISO and isn't in the future.
     * @param inputDate is the raw date string from DB
     * @return {@code true} if valid & not in the future
     */
    public static boolean isDateValidForDisplay(String inputDate) {
        String normalisedDate = normaliseDateToISO(inputDate);
        if (normalisedDate == null || normalisedDate.isEmpty()) return false;
        try {
            LocalDate dateToValidate = LocalDate.parse(normalisedDate, ISO_DATE_FORMAT);
            return !dateToValidate.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns a string for date display purposes. The ISO date if valid, otherwise the invalid-date message is shown.
     * @param inputDate as a raw date string
     * @return the formatted ISO date or {@code INVALID_DATE_MESSAGE}
     */
    public static String displayOrInvalidMessage(String inputDate) {
        if (inputDate == null) return INVALID_DATE_MESSAGE;
        String normalisedDate = normaliseDateToISO(inputDate);
        if (normalisedDate == null || normalisedDate.isEmpty()) return INVALID_DATE_MESSAGE;
        try {
            LocalDate validateDate = LocalDate.parse(normalisedDate, ISO_DATE_FORMAT);
            if (validateDate.isAfter(LocalDate.now())) return INVALID_DATE_MESSAGE;
            return normalisedDate;
        } catch (DateTimeParseException e) {
            return INVALID_DATE_MESSAGE;
        }
    }
}
