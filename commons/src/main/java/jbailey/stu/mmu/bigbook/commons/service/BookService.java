package jbailey.stu.mmu.bigbook.commons.service;

// Internal Imports
import jbailey.stu.mmu.bigbook.commons.dao.BookDAO;
import jbailey.stu.mmu.bigbook.commons.dao.query.QueryPort;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.model.modelbuilder.BookBuilder;
import jbailey.stu.mmu.bigbook.commons.model.Character;
import jbailey.stu.mmu.bigbook.commons.model.Genre;
import jbailey.stu.mmu.bigbook.commons.util.BookUtility;
import jbailey.stu.mmu.bigbook.commons.util.InputSanitiser;
import jbailey.stu.mmu.bigbook.commons.model.vaidation.BookValidator;

// Java Imports
import java.util.List;
import java.util.Objects;

/**
 * Facade for book operations using {@link BookDAO}.
 * This class required explicit injection of the {@link BookDAO} with a given {@link QueryPort} to ensure
 * no hidden dependencies and to support testing via other adapters.
 * Uses constructor injection, allowing manual instantiation and implementation with any DI framework like Spring.
 */
public class BookService {

    private final BookDAO bookDAO;

    /**
     * Constructor for the service with {@link BookDAO} injection.
     * @param bookDAO the data access object
     */
    public BookService(BookDAO bookDAO) {
        this.bookDAO = Objects.requireNonNull(bookDAO, "bookDAO cannot be null");
    }

    /**
     * Delegates to {@link BookValidator} for validation of book attributes
     * @param title as the book title
     * @param author as the author string of the book
     * @param date as the ISO standard date string
     * @param synopsis as the book synopsis
     * @return a {@code List<String>} of validation errors
     */
    public List<String> validateBook(String title, String author, String date, String synopsis) {
        return BookValidator.validateBook(title, author, date, synopsis);
    }

    public void addBook(String title, String author, String date,
                        String genresString, String charactersString, String synopsis) {
        // Sanitsze inputs to prevent XSS and injection because we llike to be properly 'enterprise' :)
        title = InputSanitiser.sanitiseString(title);
        author = InputSanitiser.sanitiseString(author);
        date = InputSanitiser.sanitiseString(date);
        genresString = InputSanitiser.sanitiseString(genresString);
        charactersString = InputSanitiser.sanitiseString(charactersString);
        synopsis = InputSanitiser.sanitiseString(synopsis);

        assertValidBookData(title, author, date, synopsis);
        bookDAO.insertBook(buildBook(0, title, author, date, genresString, charactersString, synopsis));
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public Book getBookById(int id) {
        return bookDAO.getBookById(id);
    }

    public List<Book> getAllBooksPaginated(int page, int pageSize) {
        return bookDAO.getAllBooksPaginated(page, pageSize);
    }

    public int getTotalBookCount() {
        return bookDAO.getTotalBookCount();
    }

    public void updateBook(int id, String title, String author, String date,
                           String genresString, String charactersString, String synopsis) {
        // Sanitise with OWASP as already explained in addBook
        title = InputSanitiser.sanitiseString(title);
        author = InputSanitiser.sanitiseString(author);
        date = InputSanitiser.sanitiseString(date);
        genresString = InputSanitiser.sanitiseString(genresString);
        charactersString = InputSanitiser.sanitiseString(charactersString);
        synopsis = InputSanitiser.sanitiseString(synopsis);

        assertValidBookData(title, author, date, synopsis);
        bookDAO.updateBook(buildBook(id, title, author, date, genresString, charactersString, synopsis));
    }

    public void deleteBook(int id) {
        bookDAO.deleteBook(id);
    }

    public List<Book> searchPaginatedBooksList(String searchString, int page, int pageSize) {
        return bookDAO.searchBookPaginated(searchString, page, pageSize);
    }

    public int getSearchBookCount(String searchString) {
        return bookDAO.getSearchBookCount(searchString);
    }

    public String serialiseGenres(List<Genre> genres) {
        return BookUtility.serialiseGenreString(genres);
    }

    public String serialiseCharacters(List<Character> characters) {
        return BookUtility.serialiseCharacterString(characters);
    }

    /**
     * Retrieves paginated book summaries for a more efficient REST API list retrieval.
     * Summaries include only an id, title, author, date to reduce data transfer.
     *
     * @param page is the page number (0-based because software engineers like starting at 0).
     * @param pageSize represents the number of summaries to display per page.
     * @return a List of Book objects with summary fields only.
     */
    public List<Book> getPaginatedBookSummaries(int page, int pageSize) {
        return bookDAO.getPaginatedBookSummariesList(page, pageSize);
    }

    /**
     * Searches books and returns paginated summaries for efficient REST API searching.
     *
     * @param searchString as the full search term.
     * @param page is trhe page number
     * @param pageSize is the number of summaries to display per page.
     * @return a list of Book objects with summary fields.
     */
    public List<Book> searchBookSummariesPaginated(String searchString, int page, int pageSize) {
        return bookDAO.getPaginatedBookSummarySearchResults(searchString, page, pageSize);
    }

    private Book buildBook(int id, String title, String author, String date,
                           String genresString, String charactersString, String synopsis) {
        return new BookBuilder()
                .id(id)
                .title(title)
                .author(author)
                .date(date)
                .genres(BookUtility.parseGenreString(genresString))
                .characters(BookUtility.parseCharacterString(charactersString))
                .synopsis(synopsis)
                .build();
    }

    private void assertValidBookData(String title, String author, String date, String synopsis) {
        List<String> errors = validateBook(title, author, date, synopsis);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Book validation failed: " + String.join("; ", errors));
        }
    }
}
