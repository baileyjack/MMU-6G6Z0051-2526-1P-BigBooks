package jbailey.stu.mmu.bigbook.commons.dao;

import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.model.modelbuilder.BookBuilder;
import jbailey.stu.mmu.bigbook.commons.model.Genre;
import jbailey.stu.mmu.bigbook.commons.model.Character;
import jbailey.stu.mmu.bigbook.commons.util.BookUtility;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Concrete implementation for mapping a {@link ResultSet} row to a {@link Book} object.
 * This strategy is an implementation of the {@link DatabaseRowMapper} and uses the
 * {@link BookBuilder} internally to construct {@link Book} objects step-by-step.
 * This class keeps mapping logic in one place, so if the database schema changes,
 * only this class and {@link BookBuilder} need to be updated.
 */
public class BookRowMapper implements DatabaseRowMapper<Book> {

    /**
     * Maps current row of the {@link ResultSet} to a constructed {@link Book} object.
     * {@link Genre}s and {@link Character}s are built from the comma-separated database
     * entries, with the {@link BookUtility}.
     *
     * @param currentResultSet which is the {@link ResultSet} positioned on the current row.
     * @return A {@link Book} object built via {@link BookBuilder}.
     * @throws SQLException if a database issue occurs.
     */
    @Override
    public Book mapRow(ResultSet currentResultSet) throws SQLException {
        int id = currentResultSet.getInt("id");
        String title = currentResultSet.getString("title");
        String author = currentResultSet.getString("author");
        String date = currentResultSet.getString("date");
        String genresStr = currentResultSet.getString("genres");
        String charactersStr = currentResultSet.getString("characters");
        String synopsis = currentResultSet.getString("synopsis");
        List<Genre> genres = BookUtility.parseGenreString(genresStr);
        List<Character> characters = BookUtility.parseCharacterString(charactersStr);

        return new BookBuilder()
                .id(id)
                .title(title)
                .author(author)
                .date(date)
                .genres(genres)
                .characters(characters)
                .synopsis(synopsis)
                .build();
    }
}
