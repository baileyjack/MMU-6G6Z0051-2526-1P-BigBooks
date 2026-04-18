package jbailey.stu.mmu.bigbook.commons.dao;

import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.model.Genre;
import jbailey.stu.mmu.bigbook.commons.model.modelbuilder.BookBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Row mapper for book summaries for the API service, selecting only essential fields (id, title, author, date)
 * in the DB to improve query performance and reduce data transfer for views.
 * This mapper creates {@link Book} objects with only some fields populated,
 * {@link Genre}s, {@link Character}s, and synopsis as empty.
 * Used for efficient REST API responses where full details are not needed.
 */
public class BookSummaryRowMapper implements DatabaseRowMapper<Book> {

    /**
     * Maps the current {@link ResultSet} row to a {@link Book} with summary fields.
     * @param currentResultSet is the {@link ResultSet} on the current row of the DB.
     * @return a {@link Book} with id, title, author, date
     * @throws SQLException if a DB access error occurs.
     */
    @Override
    public Book mapRow(ResultSet currentResultSet) throws SQLException {
        int id = currentResultSet.getInt("id");
        String title = currentResultSet.getString("title");
        String author = currentResultSet.getString("author");
        String date = currentResultSet.getString("date");

        return new BookBuilder()
                .id(id)
                .title(title)
                .author(author)
                .date(date)
                .genres(null) // Not selected for summaries
                .characters(null) // Not selected for summaries
                .synopsis(null) // Not selected for summaries
                .build();
    }
}
