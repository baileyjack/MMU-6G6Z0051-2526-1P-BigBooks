package jbailey.stu.mmu.bigbook.commons.dao;

// Internal Imports
import jbailey.stu.mmu.bigbook.commons.dao.query.QueryPort;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.util.BookUtility;

// External Imports
import lombok.extern.slf4j.Slf4j;

// Java Imports
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data Access Object for managing {@link Book} entities in the DB.
 * Uses the {@link QueryPort} for generic query execution.
 * Uses constructor injection for frameworks, allowing use in manual instantiation and DI such as Spring.
 */
@Slf4j
public class BookDAO {

    // Query Definitions for reusability
    private static final String BOOK_COLUMNS = "id, title, author, date, genres, characters, synopsis"; // easy to add columns
    private static final String BOOK_SELECT_STATEMENT = "SELECT " + BOOK_COLUMNS + " FROM books";

    // Rows and Summary Rows for REST are different. MVC gets full rows all the time, REST uses summaries as it is client facing and should use the DB less as more traffic would use this in a real-world application / deployment
    private final DatabaseRowMapper<Book> rowMapper = new BookRowMapper();
    private final DatabaseRowMapper<Book> summaryRowMapper = new BookSummaryRowMapper();
    private final QueryPort queryPort;

    /**
     * Constructor requiring {@link QueryPort} which makes callers explicit about which query adapter to use.
     * This prevents hidden dependencies and allows flexibility in data access strategies.
     *
     * @param queryPort is a required {@link QueryPort} as the data access port implementation
     */
    public BookDAO(QueryPort queryPort) {
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort cannot be null");
    }

    public List<Book> getAllBooks() {
        String query = BOOK_SELECT_STATEMENT + " ORDER BY id ASC";
        try {
            return queryPort.queryList(query, rowMapper);
        } catch (SQLException sqlException) {
            log.error("Error fetching all books: {}", sqlException.getMessage(), sqlException);
            return new ArrayList<>();
        }
    }

    public Book getBookById(int id) {
        String query = BOOK_SELECT_STATEMENT + " WHERE id = ?";
        try {
            return queryPort.queryOne(query, statement -> statement.setInt(1, id), rowMapper);
        } catch (SQLException sqlException) {
            log.error("Error fetching book by ID: {}", sqlException.getMessage(), sqlException);
            return null;
        }
    }

    public void insertBook(Book book) {
        String query = "INSERT INTO books (title, author, date, genres, characters, synopsis) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            queryPort.update(query, statement -> {
                statement.setString(1, book.getTitle());
                statement.setString(2, book.getAuthor());
                statement.setString(3, book.getDate());
                statement.setString(4, BookUtility.serialiseGenreString(book.getGenres()));
                statement.setString(5, BookUtility.serialiseCharacterString(book.getCharacters()));
                statement.setString(6, book.getSynopsis());
            });
        } catch (SQLException sqlException) {
            log.error("Error inserting book: {}", sqlException.getMessage(), sqlException);
        }
    }

    public void updateBook(Book book) {
        String query = "UPDATE books SET title = ?, author = ?, date = ?, genres = ?, characters = ?, synopsis = ? WHERE id = ?";
        try {
            queryPort.update(query, statement -> {
                statement.setString(1, book.getTitle());
                statement.setString(2, book.getAuthor());
                statement.setString(3, book.getDate());
                statement.setString(4, BookUtility.serialiseGenreString(book.getGenres()));
                statement.setString(5, BookUtility.serialiseCharacterString(book.getCharacters()));
                statement.setString(6, book.getSynopsis());
                statement.setInt(7, book.getId());
            });
        } catch (SQLException sqlException) {
            log.error("Error updating book: {}", sqlException.getMessage(), sqlException);
        }
    }

    public void deleteBook(int id) {
        String query = "DELETE FROM books WHERE id = ?";
        try {
            queryPort.update(query, statement -> statement.setInt(1, id));
        } catch (SQLException sqlException) {
            log.error("Error deleting book: {}", sqlException.getMessage(), sqlException);
        }
    }

    public List<Book> searchBook(String searchStr) {
        String query = BOOK_SELECT_STATEMENT + " WHERE title LIKE ? OR author LIKE ?";
        String likeStr = "%" + searchStr + "%";
        try {
            return queryPort.queryList(query, statement -> {
                statement.setString(1, likeStr);
                statement.setString(2, likeStr);
            }, rowMapper);
        } catch (SQLException sqlException) {
            log.error("Error searching books: {}", sqlException.getMessage(), sqlException);
            return new ArrayList<>();
        }
    }

    public List<Book> getRecentBooks(int limit) {
        String query = BOOK_SELECT_STATEMENT + " ORDER BY date DESC LIMIT ?";
        try {
            return queryPort.queryList(query, statement -> statement.setInt(1, limit), rowMapper);
        } catch (SQLException sqlException) {
            log.error("Error fetching recent books", sqlException);
            throw new RuntimeException("Failed to fetch recent books", sqlException);
        }
    }

    public List<Book> getAllBooksPaginated(int page, int pageSize) {
        String query = BOOK_SELECT_STATEMENT + " ORDER BY id ASC LIMIT ? OFFSET ?";
        try {
            return queryPort.queryList(query, statement -> {
                statement.setInt(1, pageSize);
                statement.setInt(2, page * pageSize);
            }, rowMapper);
        } catch (SQLException sqlException) {
            log.error("Error fetching paginated books: {}", sqlException.getMessage(), sqlException);
            return new ArrayList<>();
        }
    }

    public int getTotalBookCount() {
        String query = "SELECT COUNT(*) FROM books";
        try {
            return queryPort.queryForInt(query);
        } catch (SQLException sqlException) {
            log.error("Error getting book count: {}", sqlException.getMessage(), sqlException);
            return 0;
        }
    }

    public List<Book> searchBookPaginated(String searchStr, int page, int pageSize) {
        String query = BOOK_SELECT_STATEMENT + " WHERE title LIKE ? OR author LIKE ? ORDER BY id ASC LIMIT ? OFFSET ?";
        String likeStr = "%" + searchStr + "%";
        try {
            return queryPort.queryList(query, statement -> {
                statement.setString(1, likeStr);
                statement.setString(2, likeStr);
                statement.setInt(3, pageSize);
                statement.setInt(4, page * pageSize);
            }, rowMapper);
        } catch (SQLException sqlException) {
            log.error("Error searching books paginated: {}", sqlException.getMessage(), sqlException);
            return new ArrayList<>();
        }
    }

    public int getSearchBookCount(String searchStr) {
        String query = "SELECT COUNT(*) FROM books WHERE title LIKE ? OR author LIKE ?";
        String likeStr = "%" + searchStr + "%";
        try {
            return queryPort.queryForInt(query, statement -> {
                statement.setString(1, likeStr);
                statement.setString(2, likeStr);
            });
        } catch (SQLException sqlException) {
            log.error("Error getting search book count: {}", sqlException.getMessage(), sqlException);
            return 0;
        }
    }

    /**
     * Retrieves a paginated list of book summaries (id, title, author, date)
     * for efficient views in REST, reducing data transfer and query time.
     *
     * @param page is the page number starting from 0
     * @param pageSize is the total number of summaries to show per page.
     * @return is a {@code List} of {@link Book} objects with only summary fields populated.
     */
    public List<Book> getPaginatedBookSummariesList(int page, int pageSize) {
        String summaryColumns = "id, title, author, date";
        String query = "SELECT " + summaryColumns + " FROM books ORDER BY id ASC LIMIT ? OFFSET ?";
        try {
            return queryPort.queryList(query, statement -> {
                statement.setInt(1, pageSize);
                statement.setInt(2, page * pageSize);
            }, summaryRowMapper);
        } catch (SQLException sqlException) {
            log.error("Error fetching paginated book summaries: {}", sqlException.getMessage(), sqlException);
            return new ArrayList<>();
        }
    }

    /**
     * Applies searching with pagination for books and returns summaries
     * for efficient search results in REST API.
     *
     * @param searchStr the search term for title or author.
     * @param page the page number (0-based).
     * @param pageSize the number of summaries per page.
     * @return {@code List} of {@link Book} objects with only summary fields populated.
     */
    public List<Book> getPaginatedBookSummarySearchResults(String searchStr, int page, int pageSize) {
        String summaryColumns = "id, title, author, date";
        String query = "SELECT " + summaryColumns + " FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY id ASC LIMIT ? OFFSET ?";
        String likeStr = "%" + searchStr + "%";
        try {
            return queryPort.queryList(query, statement -> {
                statement.setString(1, likeStr);
                statement.setString(2, likeStr);
                statement.setInt(3, pageSize);
                statement.setInt(4, page * pageSize);
            }, summaryRowMapper);
        } catch (SQLException sqlException) {
            log.error("Error searching book summaries paginated: {}", sqlException.getMessage(), sqlException);
            return new ArrayList<>();
        }
    }
}
