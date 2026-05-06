package jbailey.stu.mmu.bigbook.rest.model;

// internal Imports
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.model.Character;
import jbailey.stu.mmu.bigbook.commons.model.Genre;

// External Imports
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convert between {@link Book} objects and REST API {@link BookDTO}s.
 * Mapping is necessary to separate API contracts from domain models.
 * This class does this transformation, including converting objects
 * like {@code List<Genre>} to API-friendly formats {@code List<String>}
 */

public class BookMapper {

    /**
     * Converts a {@link Book} to a {@link BookDTO} for API responses.
     *
     * @param book is the {@link Book} object to be converted
     * @return a {@link BookDTO}
     */
    public static BookDTO toDTO(Book book) {
        if (book == null) {
            return null;
        }
        return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getDate(),
            book.getGenres() != null ? book.getGenres().stream().map(Genre::getName).collect(Collectors.toList()) : null,
            book.getCharacters() != null ? book.getCharacters().stream().map(Character::getName).collect(Collectors.toList()) : null,
            book.getSynopsis()
        );
    }

    /**
     * Converts a {@link BookDTO} from requests to a {@link Book} object.
     *
     * @param bookDTO is the {@link BookDTO} to convert
     * @return a {@link Book} object
     */
    public static Book fromDTO(BookDTO bookDTO) {
        if (bookDTO == null) {
            return null;
        }
        return new Book(
            bookDTO.getId(),
            bookDTO.getTitle(),
            bookDTO.getAuthor(),
            bookDTO.getDate(),
            bookDTO.getGenres() != null ? bookDTO.getGenres().stream().map(name -> new Genre(name)).collect(Collectors.toList()) : null,
            bookDTO.getCharacters() != null ? bookDTO.getCharacters().stream().map(name -> new Character(name)).collect(Collectors.toList()) : null,
            bookDTO.getSynopsis()
        );
    }

    /**
     * Converts a {@code List} of {@link Book}s to a {@code List} of {@link BookDTO}s.
     *
     * @param books is the {@code List} of {@link Book}s
     * @return a {@code List} of {@link BookDTO} objects
     */
    public static List<BookDTO> toDTOList(List<Book> books) {
        if (books == null) {
            return null;
        }
        return books.stream().map(BookMapper::toDTO).collect(Collectors.toList());
    }
}
