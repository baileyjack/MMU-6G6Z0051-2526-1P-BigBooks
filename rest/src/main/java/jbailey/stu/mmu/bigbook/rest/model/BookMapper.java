package jbailey.stu.mmu.bigbook.rest.model;

import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.model.Character;
import jbailey.stu.mmu.bigbook.commons.model.Genre;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Convert between Book objects and REST API Book DTOs.
 * Mapping is necessary to separate API contracts from domain models.
 * This class does this transformation, including converting objects
 * like {@code List<Genre>} to API-friendly formats {@code List<String>}
 */

public class BookMapper {

    /**
     * Converts a Book to a BookDTO for API responses.
     *
     * @param book is the Book object to be converted
     * @return a BookDTO
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
     * Converts a BookDTO from requests to a Book object.
     *
     * @param dto is the BookDTO to convert
     * @return a Book object
     */
    public static Book fromDTO(BookDTO dto) {
        if (dto == null) {
            return null;
        }
        return new Book(
            dto.getId(),
            dto.getTitle(),
            dto.getAuthor(),
            dto.getDate(),
            dto.getGenres() != null ? dto.getGenres().stream().map(name -> new Genre(name)).collect(Collectors.toList()) : null,
            dto.getCharacters() != null ? dto.getCharacters().stream().map(name -> new Character(name)).collect(Collectors.toList()) : null,
            dto.getSynopsis()
        );
    }

    /**
     * Converts a list of Books to a list of BookDTOs.
     *
     * @param books is the List of Books
     * @return a list of BookDTO objects
     */
    public static List<BookDTO> toDTOList(List<Book> books) {
        if (books == null) {
            return null;
        }
        return books.stream().map(BookMapper::toDTO).collect(Collectors.toList());
    }
}
