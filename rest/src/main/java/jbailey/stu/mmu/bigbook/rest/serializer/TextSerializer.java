package jbailey.stu.mmu.bigbook.rest.serializer;

import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;

import java.util.List;

/**
 * Plain text serializer for human-readable output.
 * 
 * Converts RestResponse objects to a simple, readable text format.
 * This is useful for debugging and simple API clients that need plain text output.
 * 
 * @param <T> The data type being serialized.
 */
public class TextSerializer<T> implements ResponseSerializer<T> {

    /**
     * Serialize a RestResponse object to plain text string.
     * 
     * For lists of books, displays each book on a new line with details.
     * For single books, displays book details in a readable format.
     * 
     * @param response The RestResponse to serialize.
     * @return Plain text representation.
     */
    @Override
    public String serialize(RestResponse<T> response) {
        StringBuilder sb = new StringBuilder();
        
        // Add status and message.
        sb.append("Status: ").append(response.getStatus()).append("\n");
        sb.append("Message: ").append(response.getMessage()).append("\n");
        sb.append("---\n");
        
        // Handle different data types.
        if (response.getData() == null) {
            sb.append("No data available.\n");
        } else if (response.getData() instanceof List<?>) {
            // Handle list of books.
            List<?> dataList = (List<?>) response.getData();
            if (dataList.isEmpty()) {
                sb.append("No books found.\n");
            } else {
                for (Object item : dataList) {
                    if (item instanceof Book) {
                        sb.append(formatBook((Book) item)).append("\n");
                    }
                }
            }
        } else if (response.getData() instanceof Book) {
            // Handle single book.
            sb.append(formatBook((Book) response.getData()));
        } else {
            // Handle other data types.
            sb.append(response.getData().toString());
        }
        
        return sb.toString();
    }

    /**
     * Deserialize text is not supported for TEXT format.
     * 
     * @throws UnsupportedOperationException always, as TEXT format is output-only.
     */
    @Override
    public <U> U deserialize(String text, Class<U> clazz) {
        throw new UnsupportedOperationException("Deserialization from TEXT format is not supported.");
    }

    /**
     * Format a Book object as a readable string.
     * 
     * @param book The Book to format.
     * @return A formatted string representation of the book.
     */
    private String formatBook(Book book) {
        return String.format(
                "ID: %d | Title: %s | Author: %s | Date: %s | Genres: %s | Characters: %s",
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDate() != null ? book.getDate() : "Unknown",
                book.getGenres() != null && !book.getGenres().isEmpty() ? 
                    String.join(", ", book.getGenres().stream().map(g -> g.getName()).toList()) : "None",
                book.getCharacters() != null && !book.getCharacters().isEmpty() ? 
                    String.join(", ", book.getCharacters().stream().map(c -> c.getName()).toList()) : "None"
        );
    }
}

