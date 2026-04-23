package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.commons.service.BookService;

/**
 * Shared superclass for MVC controller commands and utilities that work with the books DB.
 * Abstract as this shouldn't be instantiated directly.
 */
public abstract class
AbstractBaseBookCommand {

    protected final BookService bookService;

    protected AbstractBaseBookCommand(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Parses an integer that can be zero or greater.
     * Returns the defaultValue parameter if the input String is invalid.
     * Also forces all negative values to 0.
     * @param stringToParse is the String to parse as an integer.
     * @param defaultValue is the stringToParse to return if the input String is invalid.
     */
    protected int parseNonNegativeInt(String stringToParse, int defaultValue) {
        if (stringToParse == null || stringToParse.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Math.max(Integer.parseInt(stringToParse.trim()), 0);
        } catch (NumberFormatException ex) {
            // Where an exception occurs, always return default
            return defaultValue;
        }
    }

    /**
     * Parses an integer that must always be a positive integer
     * Returns the defaultValue parameter if the input String is missing, invalid, zero, or negative.
     * Also forces all negative values to 0.
     * @param stringToParse is the String to parse as an integer.
     * @param defaultValue is the stringToParse to return if the input String is invalid.
     */
    protected int parsePositiveInt(String stringToParse, int defaultValue) {
        if (stringToParse == null || stringToParse.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(stringToParse.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /**
     * Parses and an ID from the database, which should always be a number.
     * @param stringToParse is the String to parse as an ID.
     * @return the parsed ID as an integer.
     * @throws IllegalArgumentException if the stringToParse is missing or invalid.
     * */
    protected int requireId(String stringToParse) {
        if (stringToParse == null || stringToParse.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing ID");
        }
        try {
            return Integer.parseInt(stringToParse.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid ID", ex);
        }
    }

    /**
     * Creates a redirect CommandResult for controller dispatch.
     * @param path is the path to redirect to.
     * @return a CommandResult representing the redirect.
     */
    protected CommandResult redirect(String path) {
        return CommandResult.redirect(path);
    }

    /**
     * Creates a forward CommandResult for controller dispatch.
     * @param path is the path to forward to.
     * @return a CommandResult representing the forward request.
     */
    protected CommandResult forward(String path) {
        return CommandResult.forward(path);
    }
}
