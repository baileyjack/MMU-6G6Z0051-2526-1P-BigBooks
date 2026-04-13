package jbailey.stu.mmu.bigbook.commons.util;

import jbailey.stu.mmu.bigbook.commons.model.Character;
import jbailey.stu.mmu.bigbook.commons.model.Genre;

import java.util.Arrays;
import java.util.List;

/**
 * Utilities for parsing and serialising {@link Genre} and {@link Character} Strings to objects.
 */
public class BookUtility {

    private BookUtility() {}

    /**
     * Parses comma-separated strings into a {@code List<>} of {@link Genre} objects and filters out empty strings.
     * @param inputGenreString as the comma-separated string of genres.
     * @return A {@code List<>} of {@link Genre} objects.
     */
    public static List<Genre> parseGenreString(String inputGenreString) {
        if (inputGenreString == null || inputGenreString.trim().isEmpty()) {
            return List.of(); // EMpty list if input is null or empty
        }
        return Arrays.stream(inputGenreString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Genre::new)
                .toList();
    }

    /**
     * Exactly the same as parseGenreString but for {@link Character} mapping,
     */
    public static List<Character> parseCharacterString(String inputCharacterString) {
        if (inputCharacterString == null || inputCharacterString.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(inputCharacterString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Character::new)
                .toList();
    }

    /**
     * serialises a {@code List<>} of {@link Genre} objects into a comma-separated String.
     *
     * @param genres as a {@code List<>} of {@link Genre} objects.
     * @return A comma-separated string of genres.
     */
    public static String serialiseGenreString(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) return "";
        return genres.stream().map(Genre::getName).reduce((a, b) -> a + ", " + b).orElse("");
    }

    /**
     * Exactly the same as serialiseGenreString but for {@link Character} mapping
     */
    public static String serialiseCharacterString(List<Character> characters) {
        if (characters == null || characters.isEmpty()) return "";
        return characters.stream().map(Character::getName).reduce((a, b) -> a + ", " + b).orElse("");
    }
}
