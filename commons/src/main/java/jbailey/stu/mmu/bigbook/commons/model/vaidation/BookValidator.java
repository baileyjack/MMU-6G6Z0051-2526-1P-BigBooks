package jbailey.stu.mmu.bigbook.commons.model.vaidation;

import jbailey.stu.mmu.bigbook.commons.util.DateUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all book field validation.
 */
public class BookValidator {

    private BookValidator() {
    }

    /**
     * Validate all data fields. Returns Errors
     *
     * @param title as the book title
     * @param author as the author of the book
     * @param date as the date (should be YYYY-MM-DD)
     * @param synopsis as the book synopsis description
     * @return a list of validation errors (or empty if all valid)
     */
    public static List<String> validateBook(String title, String author, String date, String synopsis) {
        List<String> errors = new ArrayList<>();
        errors.addAll(validateTitle(title));
        errors.addAll(validateAuthor(author));
        // Date validation is delegated to the DateUtility as it can be used for other areas of hte application too.
        errors.addAll(DateUtility.validateDateInput(date));
        errors.addAll(validateSynopsis(synopsis));
        return errors;
    }

    /**
     * Validates book titles.
     * @param title as the title to validate
     * @return a list of validation errors
     */
    public static List<String> validateTitle(String title) {
        List<String> errors = new ArrayList<>();

        if (title == null || title.trim().isEmpty()) {
            errors.add("Title is required");
            return errors;
        }
        if (title.trim().length() < 1) {
            errors.add("Title cannot be empty");
        }
        if (title.trim().length() > 255) {
            errors.add("Title shouldn't exceed 255 characters");
        }
        return errors;
    }

    /**
     * Validate authors.
     * @param author as the author to validate
     * @return a list of validation errors
     */
    public static List<String> validateAuthor(String author) {
        List<String> errors = new ArrayList<>();

        if (author == null || author.trim().isEmpty()) {
            errors.add("Author is required");
            return errors;
        }
        if (author.trim().length() < 1) {
            errors.add("Author cannot be empty");
        }
        if (author.trim().length() > 255) {
            errors.add("Author length shouldn't exceed 255 characters");
        }

        return errors;
    }

    /**
     * Validate a book synopsis description.
     * @param synopsis as the synopsis to validate
     * @return A list of validation errors
     */
    public static List<String> validateSynopsis(String synopsis) {
        List<String> errors = new ArrayList<>();
        if (synopsis == null || synopsis.trim().isEmpty()) {
            errors.add("Synopsis is required");
            return errors;
        }
        if (synopsis.trim().length() < 10) {
            errors.add("Synopsis must be at least 10 characters");
        }
        return errors;
    }
}
